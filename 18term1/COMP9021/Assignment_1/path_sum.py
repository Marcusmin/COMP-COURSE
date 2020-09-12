import sys

filename = input("")
array = []
try:
    with open(filename) as file:
        line = file.readline()
        while line:
            line = line.strip('\n')
            line = list(line)
            while ' ' in line:
                line.remove(' ')
            array.append(line)
            line = file.readline()
except FileNotFoundError:
    print("Can't find file!")
    sys.exit()
for i in range(len(array)):
    for j in range(len(array[i])):
        array[i][j] = int(array[i][j])

array_copy = array.copy()
path_record = []
row = len(array) - 2
count_same = 0
while row >= 0:
    for col in range(len(array[row])):
        if array[row + 1][col] < array[row + 1][col + 1]:
            array[row][col] += array[row + 1][col + 1]
        elif array[row + 1][col] > array[row + 1][col + 1]:
            array[row][col] += array[row + 1][col]
        else:
            array[row][col] += array[row + 1][col]
    row -= 1

print(array[0][0])
print(array)
print(count_same)
