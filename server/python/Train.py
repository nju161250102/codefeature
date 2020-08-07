from keras.models import Sequential, Model
from keras.layers import Conv1D, MaxPool1D, GlobalAveragePooling1D, Dropout
from keras.layers import Input, Dense, Flatten, LSTM
from keras.callbacks import History
from keras_gcn import GraphConv
from sklearn.metrics import f1_score, recall_score, precision_score
from Generator import *
from Config import get_config
import os
import random
import json
import sys
import itertools
import pandas as pd
import numpy as np


def cnn_model(shape_0=get_config("seq_len"), shape_1=16):
    model = Sequential()
    model.add(Conv1D(32, kernel_size=3, activation='relu', input_shape=(shape_0, shape_1)))
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


def lstm_model(shape_0=get_config("seq_len"), shape_1=16):
    model = Sequential()
    model.add(LSTM(shape_1, input_shape=(shape_0, shape_1)))
    model.add(Dense(32, activation='relu'))
    model.add(Dense(16, activation='relu'))
    model.add(Dense(2, activation='softmax'))
    model.compile(loss='categorical_crossentropy',
                  optimizer='adam',
                  metrics=['accuracy'])
    return model


def gcn_model(size):
    unit_num = 4
    node_input = Input(shape=(get_config("node_len"), size), name='Input-Node')
    edge_input = Input(shape=(get_config("node_len"), get_config("node_len")), dtype='int32', name='Input-Edge')
    conv_layer = GraphConv(units=unit_num, name='GraphConv')([node_input, edge_input])
    x = Flatten()(conv_layer)
    x = Dense(unit_num * get_config("node_len"), activation='relu')(x)
    x = Dense(64, activation='relu')(x)
    output_layer = Dense(2, activation='softmax')(x)
    model = Model(inputs=[node_input, edge_input], outputs=output_layer)
    model.compile(loss='categorical_crossentropy',
                  optimizer='adam',
                  metrics=['accuracy'])
    return model


class Metrics(History):

    def __init__(self, generator, steps):
        History.__init__(self)
        self.generator = generator
        self.steps = steps
        self.val_label = None
        g1, g2= itertools.tee(self.generator, 2)
        self.generator = g1
        for i in range(self.steps):
            label_y = g2.__next__()[1]
            if self.val_label is None:
                self.val_label = np.array(label_y)
            else:
                self.val_label = np.concatenate((self.val_label, np.array(label_y)))

    def on_epoch_end(self, epoch, logs={}):
        g1, g2 = itertools.tee(self.generator, 2)
        self.generator = g1
        val_predict = None
        for i in range(self.steps):
            predict_y = model.predict_generator(g2, steps=1, workers=0, verbose=0)
            if val_predict is None:
                val_predict = np.array(predict_y)
            else:
                val_predict = np.concatenate((val_predict, np.array(predict_y)))
        val_predict = np.where(val_predict > 0.5, 1, 0)
        _val_f1 = f1_score(self.val_label[:, 0], val_predict[:, 0])
        _val_recall = recall_score(self.val_label[:, 0], val_predict[:, 0])
        _val_precision = precision_score(self.val_label[:, 0], val_predict[:, 0])

        # print(_val_f1, _val_precision, _val_recall)
        logs['val_f1'] = _val_f1
        logs['val_recall'] = _val_recall
        logs['val_precision'] = _val_precision


def train_main(data_dir, model_path, model_name, epoch_num, feature_size, flag=True):

    vector_paths = get_feature_files(data_dir)
    random.shuffle(vector_paths)

    deepwalk_files = get_feature_files(data_dir, "DeepWalk")
    random.shuffle(deepwalk_files)
    paragraph_files = get_feature_files(data_dir, "ParagraphVec")
    random.shuffle(paragraph_files)
    # print(vector_size, len(deepwalk_files))

    if len(sys.argv) > 5:
        vector_paths_test = vector_paths[:int(len(vector_paths) * 0.2)]
        vector_paths = vector_paths[int(len(vector_paths) * 0.2):]
        deepwalk_files_test = deepwalk_files[:int(len(deepwalk_files) * 0.2)]
        deepwalk_files = deepwalk_files[int(len(deepwalk_files) * 0.2):]
        paragraph_files_test = paragraph_files[:int(len(paragraph_files) * 0.2)]
        paragraph_files = paragraph_files[int(len(paragraph_files) * 0.2):]
    else:
        vector_paths_test = vector_paths
        deepwalk_files_test = deepwalk_files
        paragraph_files_test = paragraph_files

    model_list = [{
        "name": "cnn",
        "model": cnn_model(feature_size),
        "generator": vector_generate(sys.argv[1], vector_paths, get_config("seq_len")),
        "steps": int(len(vector_paths) / get_config("batch_size")),
        "test_generator": vector_generate(sys.argv[1], vector_paths_test, get_config("seq_len")),
        "test_steps": int(len(vector_paths_test) / get_config("batch_size")),
    },{
        "name": "lstm",
        "model": lstm_model(feature_size),
        "generator": vector_generate(sys.argv[1], vector_paths, get_config("seq_len")),
        "steps": int(len(vector_paths) / get_config("batch_size")),
        "test_generator": vector_generate(sys.argv[1], vector_paths_test, get_config("seq_len")),
        "test_steps": int(len(vector_paths_test) / get_config("batch_size")),
    },{
        "name": "deepwalk",
        "model": gcn_model(feature_size),
        "generator": graph_generate(sys.argv[1], "DeepWalk", deepwalk_files, get_config("node_len")),
        "steps": int(len(deepwalk_files) / get_config("batch_size")),
        "test_generator": graph_generate(sys.argv[1], "DeepWalk", deepwalk_files_test, get_config("node_len")),
        "test_steps": int(len(deepwalk_files_test) / get_config("batch_size")),
    },{
        "name": "para2vec",
        "model": gcn_model(feature_size),
        "generator": graph_generate(sys.argv[1], "ParagraphVec", paragraph_files, get_config("node_len")),
        "steps": int(len(paragraph_files) / get_config("batch_size")),
        "test_generator": graph_generate(sys.argv[1], "ParagraphVec", paragraph_files_test, get_config("node_len")),
        "test_steps": int(len(paragraph_files_test) / get_config("batch_size")),
    }]

    for item in model_list:
        if model_name != item["name"]:
            continue
        model = item["model"]

        if flag:
            g1, g2, g3 = itertools.tee(item["test_generator"], 3)
            history = model.fit_generator(item["generator"], steps_per_epoch=item["steps"],
                                          validation_data=g1, validation_steps=item["test_steps"],
                                          epochs=epoch_num, verbose=0, workers=0, callbacks=[Metrics(g2, item["test_steps"])]).history
            history["name"] = item["name"]
            with open(os.path.join(model_path, "_".join([item["name"], str(feature_size), str(epoch_num)])), 'w') as f:
                f.write(str(history).replace("'", "\""))
        else:
            model.fit_generator(item["generator"], steps_per_epoch=item["steps"], epochs=epoch_num, verbose=(0 if flag else 1), workers=0)
        model.save(os.path.join(model_path, "_".join([item["name"], str(feature_size), str(epoch_num)]) + ".h5"))


if __name__ == "__main__":
    if len(sys.argv) < 2:
            print(json.dumps({"modelNum": 4}))
    else:
        train_main(sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4]), int(sys.argv[5]))
