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

def sum(List):
    sum = 0
    for i in List:
        sum += i
    return sum

def get_elememt(L,source):
    for i in source:
        L.append(i)

list_total = []
#build up path count
path_count = []
for i in range(len(array)):
    if i == len(array) - 1:
        path_count.append([1]*len(array[i]))
    else:
        path_count.append([0]*len(array[i]))
print(path_count)

row = len(array) - 1
while row >= 0:
    list_row = []
    for col in range(len(array[row])):
        if len(array) - 1 - row == 0:
            list_col = []
            list_col.append(array[row][col])
        else:
            if sum(list_total[len(array) - 2 - row][col]) < sum(list_total[len(array) - 2 - row][col + 1]):
                list_col = []
                list_col.append(array[row][col])
                path_count[row][col] = path_count[row + 1][col + 1]
                get_elememt(list_col,list_total[len(array) - 2 - row][col + 1])
            elif sum(list_total[len(array) - 2 - row][col]) > sum(list_total[len(array) - 2 - row][col + 1]):
                list_col = []
                list_col.append(array[row][col])
                path_count[row][col] = path_count[row + 1][col]
                get_elememt(list_col, list_total[len(array) - 2 - row][col])
            else:
                list_col = []
                list_col.append(array[row][col])
                path_count[row][col] = path_count[row+1][col]+path_count[row+1][col+1]
                get_elememt(list_col, list_total[len(array) - 2 - row][col + 1])
        list_row.append(list_col)
    list_total.append(list_row)
    row -= 1
print(list_total)
print(path_count)