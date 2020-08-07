import numpy as np
from sklearn import preprocessing
from sklearn.svm import SVC
from sklearn.naive_bayes import GaussianNB
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import f1_score, accuracy_score, recall_score, precision_score
from keras.engine.saving import load_model
from keras_gcn import GraphConv

from Generator import *
from Train import *
from Config import get_config


def evaluate(y_data, y_predict, txt_info):
    print("--------------- " + txt_info + " ---------------")
    print("count: " + str(len(y_data)))
    print("accuracy: " + str(accuracy_score(y_data, y_predict)))
    print("precision: " + str(precision_score(y_data, y_predict)))
    print("recall: " + str(recall_score(y_data, y_predict)))
    print("f1: " + str(f1_score(y_data, y_predict)))


def data_split(data_dir, feature_type="WordVector"):
    file_data = []
    y_data = []
    for file_name, label in get_feature_files(data_dir, feature_type):
        file_data.append(file_name)
        y_data.append(1 if label == "False" else 0)

    print(feature_type + ": " + str(len(file_data)))
    return train_test_split(file_data, y_data, test_size=0.2)


def vector_seq(file_list, y_list, data_dir, feature_type="WordVector", compress=False):
    x_data = []
    for i in range(0, len(file_list)):
        if i % 100 == 0: print(i)
        file_path = os.path.join(data_dir, "False" if y_list[i] == 1 else "Positive", feature_type, file_list[i])
        x_batch = read_feature_file(file_path, feature_type, get_config("seq_len"))
        if compress:
            x_data.append([sum(batch) / len(batch) for batch in x_batch])
        else:
            x_data.append(x_batch)
    if compress:
        return preprocessing.scale(np.array(x_data))
    else:
        return x_data


def vector_combine(file_list, y_list, data_dir, compress=False):
    x_data = []
    y_data = []
    for i in range(0, len(file_list)):
        x_batch = []
        for s in ["TextVector", "WordVector", "ParagraphVec"]:
            file_path = os.path.join(data_dir, "False" if y_list[i] == 1 else "Positive", s, file_list[i])
            if not os.path.isfile(file_path) or os.path.getsize(file_path) == 0:
                continue
            x_batch.extend(read_feature_file(file_path, s, get_config("node_len") if s == "ParagraphVec" else get_config("seq_len")))
        if len(x_batch) < get_config("seq_len")*2 + get_config("node_len"):
            continue
        if compress:
            x_data.append([sum(batch) / len(batch) for batch in x_batch])
        else:
            x_data.append(x_batch)
        y_data.append(y_list[i])
    if compress:
        return preprocessing.scale(np.array(x_data)), y_data
    else:
        return x_data, y_data


def vector_graph(file_list, y_list, data_dir, feature_type):
    edge_data = []
    x_data = []
    y_data = []
    for i in range(0, len(file_list)):
        file_path = os.path.join(data_dir, "False" if y_list[i] == 1 else "Positive", feature_type, file_list[i])
        if os.path.getsize(file_path) == 0:
            continue
        edge_batch = read_feature_file(file_path, "Edge", get_config("node_len"))
        x_batch = read_feature_file(file_path, feature_type, get_config("node_len"))
        edge_data.append(edge_batch)
        x_data.append(x_batch)
        y_data.append(y_list[i])
    return [x_data, edge_data], y_data


def transform_y(y_data):
    result = []
    for label in y_data:
        result.append([1, 0] if label == 1 else [0, 1])
    return result


def transform_predict_y(y_predict):
    result = []
    for index, row in enumerate(y_predict):
        result.append(1 if row[0] > 0.5 else 0)
    return result


def svm(X_train, X_test, y_train, y_test):
    svc = SVC(kernel='rbf')
    svc.fit(X_train, y_train)
    evaluate(y_train, svc.predict(X_train), "SVM的训练集")
    evaluate(y_test, svc.predict(X_test), "SVM的测试集")


def gaussianNB(X_train, X_test, y_train, y_test):
    clf = GaussianNB()
    clf.fit(X_train, y_train)
    evaluate(y_train, clf.predict(X_train), "NB的训练集")
    evaluate(y_test, clf.predict(X_test), "NB的测试集")


def randomForest(X_train, X_test, y_train, y_test):
    rfc = RandomForestClassifier()
    rfc.fit(X_train,y_train)
    evaluate(y_train, rfc.predict(X_train), "RF的训练集")
    evaluate(y_test, rfc.predict(X_test), "RF的测试集")


def nn(model_name, model, X_train, X_test, y_train, y_test):
    model.fit(X_train, [transform_y(y_train)], epochs=10, verbose=1)
    evaluate(y_train, transform_predict_y(model.predict(X_train)), model_name + "的训练集")
    evaluate(y_test, transform_predict_y(model.predict(X_test)), model_name + "的测试集")


def save_data(data_path, feature_type):
    for flag in [True, False]:
        if feature_type == "Combine":
            file_train, file_test, y_train, y_test = data_split(data_path, "ParagraphVec")
            X_train, y_train = vector_combine(file_train, y_train, data_path, compress=flag)
            X_test, y_test = vector_combine(file_test, y_test, data_path, compress=flag)
        else:
            file_train, file_test, y_train, y_test = data_split(sys.argv[1], s)
            X_train = vector_seq(file_train, y_train, data_path, compress=flag)
            X_test = vector_seq(file_test, y_test, data_path, compress=flag)
        save_path = os.path.join(data_path, feature_type + "_" + str(int(flag)) + ".npz")
        np.savez(save_path, X_train=X_train, y_train=y_train, X_test=X_test, y_test=y_test)


def train_data(data_path, feature_type):
    save_path = os.path.join(data_path, feature_type + "_1.npz")
    data = np.load(save_path)
    svm(data["X_train"], data["X_test"], data["y_train"], data["y_test"])
    randomForest(data["X_train"], data["X_test"], data["y_train"], data["y_test"])
    gaussianNB(data["X_train"], data["X_test"], data["y_train"], data["y_test"])

    save_path = os.path.join(data_path, feature_type + "_0.npz")
    data = np.load(save_path)
    if feature_type == "Combine":
        shape_0 = get_config("seq_len")*2 + get_config("node_len")
        nn("cnn", cnn_model(shape_0, 16), data["X_train"], data["X_test"], data["y_train"], data["y_test"])
        nn("lstm", lstm_model(shape_0, 16), data["X_train"], data["X_test"], data["y_train"], data["y_test"])
    else:
        nn("cnn", cnn_model(), data["X_train"], data["X_test"], data["y_train"], data["y_test"])
        nn("lstm", lstm_model(), data["X_train"], data["X_test"], data["y_train"], data["y_test"])


if __name__ == "__main__":
    for s in ["TextVector", "WordVector", "ParagraphVec", "Combine"]:
        print("#  " + s)
        save_data(sys.argv[1], s)
        train_data(sys.argv[1], s)


