from keras.models import Sequential
from keras.layers import Conv1D, MaxPool1D, GlobalAveragePooling1D, Dropout
from keras.layers import Dense, Flatten, LSTM
import os
import sys
import pandas as pd
import numpy as np

seq_len = 256


def read_csv(path):
    x_data = None
    y_data = []
    for index, s in enumerate(["False", "Positive"]):
        for f in os.listdir(os.path.join(path, s)):
            data = pd.read_csv(os.path.join(path, s, f), header=None)
            line = np.array(data.values)
            # print(f, line.shape)
            line = np.pad(line, ((0, seq_len-line.shape[0]), (0, 0)))
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
    model.add(Dense(32, activation='relu'))
    model.add(Dense(2, activation='softmax'))
    model.compile(loss='categorical_crossentropy',
                  optimizer='adam',
                  metrics=['accuracy'])
    return model


if __name__ == "__main__":
    model_dict = {
        "CNN": "",
        "LSTM": ""
    }

    if len(sys.argv) < 2:
        print(str(len(model_dict)))
    else:
        x_data, y_data = read_csv(sys.argv[1])
        model_path = sys.argv[2]
        epoch_num = int(sys.argv[3])
        feature_size = int(sys.argv[4])
        model_dict["CNN"] = CNN(feature_size)
        model_dict["LSTM"] = LSTM_model(feature_size)

        for name, model in model_dict.items():
            history = model.fit(x_data, y_data, batch_size=8, epochs=epoch_num, verbose=0).history
            history["name"] = name
            model.save(os.path.join(model_path, name + ".h5"))
            print(history)
