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

print(array)
