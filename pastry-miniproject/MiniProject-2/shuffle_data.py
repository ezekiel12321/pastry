import pandas as pd
import numpy as np

def calculateTotalWord(word_count_result, path):
    df = pd.read_csv(path, header=None)
    for i in df.values:
        str_value = i[0]
        if(word_count_result.get(str_value)): word_count_result[str_value] += 1
        else: word_count_result[str_value] = 1
    with open('wordCountResult.txt', 'w') as f:
        for k in word_count_result.keys():
            f.writelines("{}: {} \n".format(k, word_count_result[k]))
    # print(word_count_result)


def shaffuleData(path):
    df = pd.read_csv(path, header=None)
    ds = df.sample(frac=1)
    ds.to_csv(path)
    



word_count_result = {}
path = 'data.csv'
calculateTotalWord(word_count_result, path)
# shaffuleData(path)
# print(ds)

