import numpy as np
from sklearn.svm import SVC
from sklearn.naive_bayes import GaussianNB
from sklearn.model_selection import train_test_split
from sklearn.metrics import f1_score, accuracy_score, recall_score, precision_score
from keras.engine.saving import load_model
from keras_gcn import GraphConv

from Generator import *
from Train import *
from Predict import get_predict_results
from Config import get_config


def evaluate(y_data, y_predict, txt_info):
    print("--------------- " + txt_info + " ---------------")
    print("count: " + str(len(y_data)))
    print("accuracy: " + str(accuracy_score(y_data, y_predict)))
    print("precision: " + str(precision_score(y_data, y_predict)))
    print("recall: " + str(recall_score(y_data, y_predict)))
    print("f1: " + str(f1_score(y_data, y_predict)))


def vector_seq(file_list, y_list, data_dir, feature_type="WordVector", compress=False):
    x_data = []
    for i in range(0, len(file_list)):
        file_path = os.path.join(data_dir, "False" if y_list[i] == 1 else "Positive", feature_type, file_list[i])
        x_batch = read_feature_file(file_path, feature_type, get_config("seq_len"))
        if compress:
            x_data.append([sum(batch) / len(batch) for batch in x_batch])
        else:
            x_data.append(x_batch)
    return x_data if compress else [x_data]


def vector_graph(file_list, y_list, data_dir, feature_type):
    edge_data = []
    x_data = []
    for i in range(0, len(file_list)):
        file_path = os.path.join(data_dir, "False" if y_list[i] == 1 else "Positive", feature_type, file_list[i])
        edge_batch = read_feature_file(file_path, "Edge", get_config("node_len"))
        x_batch = read_feature_file(file_path, feature_type, get_config("node_len"))
        edge_data.append(edge_batch)
        x_data.append(x_batch)
    return [x_data, edge_data]


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


def nn(model_name, model, X_train, X_test, y_train, y_test):
    model.fit(X_train, [transform_y(y_train)], epochs=10)
    evaluate(y_train, transform_predict_y(model.predict(X_train)), model_name + "的训练集")
    evaluate(y_test, transform_predict_y(model.predict(X_test)), model_name + "的测试集")


if __name__ == "__main__":
    file_data = []
    y_data = []
    # data_path =
    for file_name, label in get_feature_files(sys.argv[1]):
        file_data.append(file_name)
        y_data.append(1 if label == "False" else 0)
    file_train, file_test, y_train, y_test = train_test_split(file_data, y_data, test_size=0.3)

    # svm and nb
    X_train = vector_seq(file_train, y_train, sys.argv[1], compress=True)
    X_test = vector_seq(file_test, y_test, sys.argv[1], compress=True)
    svm(X_train, X_test, y_train, y_test)
    gaussianNB(X_train, X_test, y_train, y_test)

    # nn
    for model_name in ["cnn", "lstm", "deepwalk", "para2vec"]:
        if model_name == "cnn" or model_name == "lstm":
            X_train = vector_seq(file_train, y_train, sys.argv[1])
            X_test = vector_seq(file_test, y_test, sys.argv[1])
            model = cnn_model(16)
        elif model_name == "lstm":
            X_train = vector_seq(file_train, y_train, sys.argv[1])
            X_test = vector_seq(file_test, y_test, sys.argv[1])
            model = lstm_model(16)
        elif model_name == "deepwalk":
            X_train = vector_graph(file_train, y_train, sys.argv[1], "DeepWalk")
            X_test = vector_graph(file_test, y_test, sys.argv[1], "DeepWalk")
            model = gcn_model(16)
        elif model_name == "para2vec":
            X_train = vector_graph(file_train, y_train, sys.argv[1], "ParagraphVec")
            X_test = vector_graph(file_test, y_test, sys.argv[1], "ParagraphVec")
            model = gcn_model(16)
        else:
            continue

        nn(model_name, model, X_train, X_test, y_train, y_test)


