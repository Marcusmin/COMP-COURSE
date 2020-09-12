import  sys
from collections import defaultdict
from collections import deque
filename = input("Which data file do you want to use? ")
str_relations = []
origin_int_relations = []
int_relations = []
d_relations = deque()
node_set = set()
predecessor = defaultdict(list)
try:
    with open(filename) as file:
        line = file.readline()
        while line:
            line = line.strip('\n')
            pair = line[2:-1]
            str_relations.append(pair)
            line = file.readline()
except FileNotFoundError:
    print("Can't find file!")
    sys.exit()
for point in str_relations:
    origin_int_relations.append(point.split(','))
for i in range(len(origin_int_relations)):
    for j in range(len(origin_int_relations[i])):
        origin_int_relations[i][j] = int(origin_int_relations[i][j])
        node_set.add(origin_int_relations[i][j])
for i in origin_int_relations:
    if not i in int_relations:
        int_relations.append(i)
for num in node_set:
    for pair in int_relations:
        if num == pair[1]:
            break
    else:
        d_relations.append(num)
while d_relations:
    element = d_relations.pop()
    for num in node_set:
        predecessor[num]
        if [element,num] in int_relations:
            d_relations.appendleft(num)
            predecessor[num].append(element)
            for pre_node in predecessor[element]:
                predecessor[num].append(pre_node)
for node in predecessor:
    for pre_node in predecessor[node]:
        for i in predecessor[pre_node]:
            for j in predecessor[node]:
                if i == j:
                    if [j,node] in int_relations:
                        int_relations.remove([i,node])
print('The nonredundant facts are:')   
for i,j in int_relations:
    print(f'R({i},{j})')
    
