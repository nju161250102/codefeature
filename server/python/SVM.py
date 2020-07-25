import numpy as np
from sklearn.svm import SVC
from sklearn.naive_bayes import GaussianNB
from sklearn.model_selection import train_test_split
from sklearn.metrics import f1_score, accuracy_score, recall_score, precision_score

from Generator import *
from Config import get_config

if __name__ == "__main__":
    x_data = []
    y_data = []
    for file_name, label in get_feature_files(sys.argv[1]):
        file_path = os.path.join(sys.argv[1], label, "WordVector", file_name)
        x_batch = read_feature_file(file_path, "WordVector", get_config("seq_len"))
        x_data.append([sum(batch) / len(batch) for batch in x_batch])
        y_data.append(1 if label == "False" else 0)

    X_train, X_test, y_train, y_test = train_test_split(x_data, y_data, test_size=0.3)
    svc = SVC(kernel='rbf')
    svc.fit(X_train, y_train)
    y_predict = svc.predict(X_test)

    print(accuracy_score(y_test, y_predict))
    print(precision_score(y_test, y_predict))
    print(recall_score(y_test, y_predict))
    print(f1_score(y_test, y_predict))

    print("----------------------------------")

    clf = GaussianNB()
    clf.fit(X_train, y_train)
    y_predict = clf.predict(X_test)
    print(accuracy_score(y_test, y_predict))
    print(precision_score(y_test, y_predict))
    print(recall_score(y_test, y_predict))
    print(f1_score(y_test, y_predict))


