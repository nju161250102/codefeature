from keras.engine.saving import load_model
import os
import sys
import pandas as pd
import numpy as np

seq_len = 256


def read_wordVector(path, type):
    x_data = None
    file_name = []
    for f in os.listdir(os.path.join(path, type)):
        data = pd.read_csv(os.path.join(path, type, f), header=None)
        line = np.array(data.values)

        line = np.pad(line, ((0, seq_len-line.shape[0]), (0, 0)))
        if x_data is not None:
            x_data = np.concatenate((x_data, [line]))
        else:
            x_data = np.array([line])
        file_name.append(f)

    return x_data, file_name


if __name__ == "__main__":
    model_path = sys.argv[1]
    result_dict = {}

    for f in os.listdir(model_path):
        if len(f.split(".")) != 2 or f.split(".")[1] != "h5":
            continue

        model_name = f.split(".")[0]
        model = load_model(os.path.join(model_path, f))
        result = 0

        if model_name == "CNN" or model_name == "LSTM":
            data, file_names = read_wordVector(model_path, "WordVector")
            predict_y = model.predict(data)
            for index, file_name in enumerate(file_names):
                if file_name not in result_dict:
                    result_dict[file_name] = {}
                    result_dict[file_name]["name"] = file_name.split(".")[0]
                result_dict[file_name][model_name] = predict_y[index][0]

    result = str(list(result_dict.values()))
    print(result.replace("'", "\""))
