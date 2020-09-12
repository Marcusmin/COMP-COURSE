import numpy as np


def gradient(labels, data, weights):
    var = np.mat(np.dot(weights, data))
    grads = -1 / (1 + np.exp(-var)) + labels   # [g1, g2, .... gn]
    res = np.dot(grads, data.transpose())
    return res

def logistic_regression(data, labels, weights, num_epochs, learning_rate): # do not change the heading of the function
    step = np.array(learning_rate)
    ones = np.ones(len(data))
    new_data = np.c_[ones, data].T
    grad = gradient(labels, new_data, weights)
    weights = step * grad + weights
    times = 0
    while times <= num_epochs:
        grad = gradient(labels, new_data, weights)
        weights = step * grad + weights
        times += 1
    return weights
    
