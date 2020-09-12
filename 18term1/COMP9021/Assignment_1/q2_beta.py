# Written by *** for COMP9021
import  sys
import math
filename = input("Which data file do you want to use? ")
array = []
try:
    with open(filename) as file:
        line = file.readline()
        while line:
            line = line.strip('\n')
            line = line.split(' ')
            while ' ' in line:
                line.remove(' ')
            while '' in line:
                line.remove('')
            array.append(line)
            line = file.readline()
except FileNotFoundError:
    print("Can't find file!")
    sys.exit()
for i in range(len(array)):
    for j in range(len(array[i])):
        array[i][j] = int(array[i][j])
##check plan
def check_correct_plan(plan,array):
    sum = 0
    for town in array:
        sum+=town[1]
    if sum//len(array) == plan:
        return True
    else:
        return False
##array[][0]stores the distance,array[][1]stores the quantity
def try_plan(plan,array):
    town_list = [[0]*len(array[i]) for i in range(len(array))]
    fish_sum = 0
    fish_average = 0
    for i in range(len(array)):
        for j in range(len(array[i])):
            town_list[i][j] = array[i][j]
    for t_num in range(0,len(town_list)-1):
        if town_list[t_num][1] > plan:
            extra = town_list[t_num][1] - plan
            distance = town_list[t_num+1][0] - town_list[t_num][0]
            town_list[t_num][1] = town_list[t_num][1] - extra
            if extra > distance:
                town_list[t_num+1][1] += extra - distance
        elif town_list[t_num][1] < plan:
            need = plan - town_list[t_num][1]
            distance = town_list[t_num+1][0] - town_list[t_num][0]
            if need + distance < town_list[t_num+1][1]:
                town_list[t_num+1][1] -= need+distance
                town_list[t_num][1] += need
            else:
                town_list[t_num+1][1] = 0
        else:
            pass
    for town in town_list:
        fish_sum+=town[1]
    fish_average = fish_sum // len(town_list)
    is_correct_plan = check_correct_plan(plan,town_list)
    return is_correct_plan, fish_average

origin_average = 0
min_average = array[0][1]
for town in array:
##    find out maximized average
    origin_average += town[1]
    if min_average > town[1]:
##        find out the min_average
        min_average = town[1]
origin_average = origin_average // len(array)
plan = origin_average
result = try_plan(plan,array)
count=0
if not result[0]:
    plan = (plan + min_average) // 2
    result = try_plan(plan,array)
    while not result[0]:
        if result[1] > plan:
            min_average = plan
            plan = (min_average + origin_average) // 2
        elif result[1] < plan:
            origin_average = plan
            plan = (origin_average + min_average) // 2
        else:
            break
        result = try_plan(plan,array)
        count+=1
        if count>=99999:
            break
    print(f'The maximum quantity of fish that each town can have is {result[1]}.')
        
else:
    print(f'The maximum quantity of fish that each town can have is {result[1]}.')


##binary search



# Insert your code here