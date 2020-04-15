from keras.engine.saving import load_model
import os
import sys
import pandas as pd
import numpy as np

seq_len = 256


if __name__ == "__main__":
    model_path = sys.argv[1]
    result_list = []

    for f in os.listdir(model_path):
        if f.split(".")[1] != "h5":
            continue

        name = f.split(".")[0]
        model = load_model(os.path.join(model_path, f))
        result = 0

        if name == "CNN" or name == "LSTM":
            data = pd.read_csv(os.path.join(model_path, "AST.csv"), header=None)
            data = np.array(data.values)
            data = np.pad(data, ((0, seq_len - data.shape[0]), (0, 0)))
            result = model.predict(np.array([data]))[0][0]
        result_list.append({
            "name": name,
            "p": result
        })
    print(result_list)
