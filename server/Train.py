from keras.models import Sequential
from keras.layers import Conv1D, MaxPool1D, GlobalAveragePooling1D, Dropout
from keras.layers import Dense, Flatten, LSTM
from keras.callbacks import Callback
import os
import json
import sys
import pandas as pd
import numpy as np

seq_len = 256
batch_size = 8


def get_file_paths(path, type):
    result = []
    for index, s in enumerate(["False", "Positive"]):
        for f in os.listdir(os.path.join(path, s, type)):
            result.append((os.path.join(path, s, type, f), index))
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


def CNN(size):
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


class LossHistory(Callback):
    def on_train_begin(self, logs={}):
        self.losses = []

    def on_batch_end(self, batch, logs={}):
        self.losses.append(logs.get('loss'))


if __name__ == "__main__":

    if len(sys.argv) < 2:
        print(json.dumps({"modelNum": 2}))
    else:
        file_paths = get_file_paths(sys.argv[1], "WordVector")
        data_size = len(file_paths)
        # print(data_size)

        generator = data_generate(file_paths)
        model_path = sys.argv[2]
        epoch_num = int(sys.argv[3])
        feature_size = int(sys.argv[4])

        model_list = [{
            "name": "CNN",
            "model": CNN(feature_size),
            "generator": generator
        },{
            "name": "LSTM",
            "model": LSTM_model(feature_size),
            "generator": generator
        }]

        result = []
        for item in model_list:
            model = item["model"]
            #  callbacks=[LossHistory()]
            history = model.fit_generator(item["generator"], steps_per_epoch=int(data_size/batch_size), epochs=epoch_num, verbose=0).history
            history["name"] = item["name"]
            model.save(os.path.join(model_path, item["name"] + ".h5"))
            result.append(str(history))

        result = "[" + ",".join(result) + "]"
        print(result.replace("'", "\""))
