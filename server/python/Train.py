from keras.models import Sequential, Model
from keras.layers import Conv1D, MaxPool1D, GlobalAveragePooling1D, Dropout
from keras.layers import Input, Dense, Flatten, LSTM
from keras.callbacks import Callback
from keras_gcn import GraphConv
import os
import json
import sys
import pandas as pd
import numpy as np

seq_len = 256
node_len = 32
batch_size = 8


def word_vector_paths(path):
    result = []
    for index, s in enumerate(["False", "Positive"]):
        for f in os.listdir(os.path.join(path, s, "WordVector")):
            result.append((os.path.join(path, s, "WordVector", f), index))
    return result


def get_graph_files(output_path, node_type):
    result = []
    for s in ["False", "Positive"]:
        for f in os.listdir(os.path.join(output_path, s, "Edge")):
            if os.path.isfile(os.path.join(output_path, s, node_type, f)):
                result.append((f, s))
    return result


def data_generate(file_paths):
    while True:
        i = 0
        x_data = None
        y_data = []
        for path, label in file_paths:
            if i == batch_size:
                yield (x_data, np.array(y_data))
                x_data = None
                y_data = []
                i = 0
                continue

            data = pd.read_csv(path, header=None)
            line = np.array(data.values)

            if seq_len > line.shape[0]:
                line = np.pad(line, ((0, seq_len-line.shape[0]), (0, 0)))
            else:
                line = line[:seq_len]
            if x_data is not None:
                x_data = np.concatenate((x_data, [line]))
            else:
                x_data = np.array([line])
            l = [0, 0]
            l[label] = 1
            y_data.append(l)

            i += 1


def graph_generate(output_path, node_type, graph_files):
    while True:
        num = 0
        edge_data_list = []
        node_data_list = []
        y_data = []

        for file_name, s in graph_files:
            if num == batch_size:
                yield [np.asarray(node_data_list), np.asarray(edge_data_list)], np.asarray(y_data)
                num = 0
                edge_data_list = []
                node_data_list = []
                y_data = []

            edge_data = []
            node_data = []
            df = pd.read_csv(os.path.join(output_path, s, node_type, file_name), header=None)
            node_num = df.shape[0]
            for index, row in df.iterrows():
                if index < node_len:
                    node_data.append(list(row))
            if node_num < node_len:
                for i in range(node_num, node_len):
                    node_data.append([0 for j in range(16)])
            node_data_list.append(node_data)

            for i in range(node_len):
                edge_data.append([0 for j in range(node_len)])
            for index, row in df.iterrows():
                if int(row[0]) < node_len and int(row[1]) < node_len:
                    edge_data[int(row[0])][int(row[1])] = 1
            edge_data_list.append(edge_data)

            y_data.append([1, 0] if s == "False" else [0, 1])

            num += 1


def cnn_model(size):
    model = Sequential()
    model.add(Conv1D(32, kernel_size=3, activation='relu', input_shape=(seq_len, size)))
    model.add(MaxPool1D(2))
    model.add(Conv1D(16, kernel_size=5, activation='relu'))
    model.add(GlobalAveragePooling1D())
    model.add(Dropout(0.5))
    model.add(Dense(64, activation='relu'))
    model.add(Dense(2, activation='softmax'))
    model.compile(optimizer='adam',
              loss='categorical_crossentropy',
              metrics=['accuracy'])
    return model


def LSTM_model(size):
    model = Sequential()
    model.add(LSTM(size, input_shape=(seq_len, size)))
    model.add(Dense(32, activation='relu'))
    model.add(Dense(16, activation='relu'))
    model.add(Dense(2, activation='softmax'))
    model.compile(loss='categorical_crossentropy',
                  optimizer='adam',
                  metrics=['accuracy'])
    return model


def gcn_model(size):
    unit_num = 4
    node_input = Input(shape=(node_len, size), name='Input-Node')
    edge_input = Input(shape=(node_len, node_len), dtype='int32', name='Input-Edge')
    conv_layer = GraphConv(units=unit_num, name='GraphConv')([node_input, edge_input])
    x = Flatten()(conv_layer)
    x = Dense(unit_num * node_len, activation='relu')(x)
    x = Dense(64, activation='relu')(x)
    output_layer = Dense(2, activation='softmax')(x)
    model = Model(inputs=[node_input, edge_input], outputs=output_layer)
    model.compile(loss='categorical_crossentropy',
                  optimizer='adam',
                  metrics=['accuracy'])
    return model


if __name__ == "__main__":

    if len(sys.argv) < 2:
        print(json.dumps({"modelNum": 3}))
    else:
        vector_paths = word_vector_paths(sys.argv[1])
        vector_size = len(vector_paths)

        deepwalk_files = get_graph_files(sys.argv[1], "DeepWalk")
        paragraph_files = get_graph_files(sys.argv[1], "ParagraphVec")
        # print(vector_size, len(deepwalk_files))

        model_path = sys.argv[2]
        epoch_num = int(sys.argv[3])
        feature_size = int(sys.argv[4])

        model_list = [{
            "name": "CNN",
            "model": cnn_model(feature_size),
            "generator": data_generate(vector_paths),
            "steps": int(vector_size / batch_size)
        },{
            "name": "LSTM",
            "model": LSTM_model(feature_size),
            "generator": data_generate(vector_paths),
            "steps": int(vector_size / batch_size)
        },{
            "name": "DeepWalk_GCN",
            "model": gcn_model(feature_size),
            "generator": graph_generate(sys.argv[1], "DeepWalk", deepwalk_files),
            "steps": int(len(deepwalk_files) / batch_size)
        },{
            "name": "Paragraph2Vec_GCN",
            "model": gcn_model(feature_size),
            "generator": graph_generate(sys.argv[1], "ParagraphVec", paragraph_files),
            "steps": int(len(paragraph_files) / batch_size)
        }]

        result = []
        for item in model_list:
            model = item["model"]
            #  callbacks=[LossHistory()]
            history = model.fit_generator(item["generator"], steps_per_epoch=item["steps"], epochs=epoch_num, verbose=0).history
            history["name"] = item["name"]
            model.save(os.path.join(model_path, item["name"] + ".h5"))
            result.append(str(history))

        result = "[" + ",".join(result) + "]"
        print(result.replace("'", "\""))
