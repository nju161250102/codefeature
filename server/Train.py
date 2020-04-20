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


def read_wordVector(path, type):
    x_data = None
    y_data = []
    for index, s in enumerate(["False", "Positive"]):
        for f in os.listdir(os.path.join(path, s, type)):
            data = pd.read_csv(os.path.join(path, s, type, f), header=None)
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
            l[index] = 1
            y_data.append(l)
    return x_data, np.array(y_data)


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
    model.add(LSTM(size))
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
        wordVec_x, wordVec_y = read_wordVector(sys.argv[1], "WordVector")
        model_path = sys.argv[2]
        epoch_num = int(sys.argv[3])
        feature_size = int(sys.argv[4])

        model_list = [{
            "name": "CNN",
            "model": CNN(feature_size),
            "x_data": wordVec_x,
            "y_data": wordVec_y,
        },{
            "name": "LSTM",
            "model": LSTM_model(feature_size),
            "x_data": wordVec_x,
            "y_data": wordVec_y,
        }]

        result = []
        for item in model_list:
            model = item["model"]
            #  callbacks=[LossHistory()]
            history = model.fit(item["x_data"], item["y_data"], batch_size=8, epochs=epoch_num, verbose=0).history
            history["name"] = item["name"]
            model.save(os.path.join(model_path, item["name"] + ".h5"))
            result.append(str(history))

        result = "[" + ",".join(result) + "]"
        print(result.replace("'", "\""))
