import pandas as pd
import numpy as np

data_file='./asset/a'
raw_data = pd.read_csv(data_file, sep=',')
raw_data.head()

import submission as submission

## Read in the Data...
raw_data = pd.read_csv(data_file, sep=',')
labels=raw_data['Label'].values
data=np.stack((raw_data['Col1'].values,raw_data['Col2'].values), axis=-1)
## Fixed Parameters. Please do not change values of these parameters...
weights = np.zeros(3) # We compute the weight for the intercept as well...
num_epochs = 50000
learning_rate = 50e-5

coefficients=submission.logistic_regression(data, labels, weights, num_epochs, learning_rate)
print(coefficients)