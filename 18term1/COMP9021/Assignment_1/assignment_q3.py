import  sys
from collections import defaultdict
filename = input("Which data file do you want to use? ")
rectangle_list = []
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
            rectangle_list.append(line)
            line = file.readline()
except FileNotFoundError:
    print("Can't find file!")
    sys.exit()
for i in range(len(rectangle_list)):
    for j in range(len(rectangle_list[i])):
        rectangle_list[i][j] = int(rectangle_list[i][j])

rectangle_list_idt = []
##check the case that overlapped
##move the independent rectangle to another list
for rectangle in rectangle_list:
    for rectangle_r in rectangle_list:
        if rectangle_r == rectangle:
            continue
        else:
            if (rectangle[0]>rectangle_r[0] and rectangle[2]<rectangle_r[2]) and (rectangle[1]>rectangle_r[1] and rectangle[3]<rectangle_r[3]):
                break
    else:
        rectangle_list_idt.append(rectangle)
print(rectangle_list_idt)
rec_points = defaultdict(list)
##record their corner points
for rec_num in range(len(rectangle_list_idt)):
    for i in range(len(rectangle_list_idt[rec_num])):
        for j in range(len(rectangle_list_idt[rec_num])):
            if i % 2 == 0:
                if not j % 2 == 0:
                    rec_points[rec_num].append((rectangle_list_idt[rec_num][i],rectangle_list_idt[rec_num][j]))
            else:
                continue
#create a funciton to find all points of intersection
def find_inter_point(rectangle,line_begin,line_end):
    new_point_1 = None
    new_point_2 = None
    #if line is vertical
    if line_begin[0] == line_end[0]:
        if line_begin[0]>rectangle[2] or line_end[0]<rectangle[0]:
            return  new_point_2,new_point_1
        if line_begin[1]>rectangle[3] or line_end[1]<rectangle[1]:
            return new_point_2,new_point_1
        #case 1: No intersection points
        if line_begin[0]>=rectangle[0] and line_begin[0]<=rectangle[2]:
            if line_begin[1]>rectangle[1] and line_end[1]<rectangle[3]:
                return new_point_2,new_point_1
            if line_begin[1]>rectangle[1] and line_end[1]>=rectangle[3]:
                new_point_2 = (line_begin[0],rectangle[3])
                return  new_point_2,new_point_1
            if line_begin[1]<=rectangle[1] and line_end[1]<rectangle[3]:
                new_point_2 = (line_begin[0],rectangle[1])
                return  new_point_2,new_point_1
        #case 2: One intersection points
            if line_begin[1]<=rectangle[1] and line_end[1]>=rectangle[3]:
                new_point_2 = (line_begin[0],rectangle[3])
                new_point_1 = (line_begin[0],rectangle[1])
                return  new_point_2,new_point_1
        #case 3: Two intersecction points
    #if line is horizontal
    if line_begin[1] == line_end[1]:
        if line_begin[1] > rectangle[3] or line_end[1] < rectangle[1]:
            return new_point_2, new_point_1
        if line_begin[0] > rectangle[2] or line_end[0] < rectangle[0]:
            return new_point_2, new_point_1
# case 1: No intersection points
        if line_begin[1] >= rectangle[1] and line_begin[1] <= rectangle[3]:
            if line_begin[0] > rectangle[0] and line_end[0] < rectangle[2]:
                return new_point_2,new_point_1
            if line_begin[0] <= rectangle[0] and line_end[0] < rectangle[2]:
                new_point_2 = (rectangle[0], line_begin[1])
                return new_point_2,new_point_1
            if line_end[0]>=rectangle[2] and line_begin[0]>rectangle[0]:
                new_point_2 = (rectangle[2],line_end[1])
                return new_point_2,new_point_1
# case 2: One intersection points
            if line_begin[0] <= rectangle[0] and line_end[0] >= rectangle[2]:
                new_point_2 = (rectangle[0], line_end[1])
                new_point_1 = (rectangle[2], line_end[1])
                return new_point_2, new_point_1
def is_in_rec(pt,rec):
    in_rec = False
    if pt[0] > rec[0] and pt[0] < rec[2] \
            and pt[1] > rec[1] and pt[1] < rec[3]:
        in_rec = True
    return in_rec

#find all points on each rectangle
for rec_num in range(len(rectangle_list_idt)):
    for other_rec_num in range(len(rectangle_list_idt)):
        if rectangle_list_idt[rec_num] == rectangle_list_idt[other_rec_num]:
            pass
        else:
            print(rectangle_list_idt[other_rec_num])
            for point in find_inter_point(rectangle_list_idt[rec_num],
                                          (rectangle_list_idt[other_rec_num][0],rectangle_list_idt[other_rec_num][1]),
                                          (rectangle_list_idt[other_rec_num][0],rectangle_list_idt[other_rec_num][3])):
                if point == None:
                    pass
                else:
                    rec_points[rec_num].append(point)
            for point in find_inter_point(rectangle_list_idt[rec_num],
                                      (rectangle_list_idt[other_rec_num][0],rectangle_list_idt[other_rec_num][1]),
                                      (rectangle_list_idt[other_rec_num][2],rectangle_list_idt[other_rec_num][1])):
                if point == None:
                    pass
                else:
                    rec_points[rec_num].append(point)
            for point in find_inter_point(rectangle_list_idt[rec_num],
                                      (rectangle_list_idt[other_rec_num][0],rectangle_list_idt[other_rec_num][3]),
                                      (rectangle_list_idt[other_rec_num][2],rectangle_list_idt[other_rec_num][3])):
                if point == None:
                    pass
                else:
                    rec_points[rec_num].append(point)
            for point in find_inter_point(rectangle_list_idt[rec_num],
                                      (rectangle_list_idt[other_rec_num][2],rectangle_list_idt[other_rec_num][1]),
                                      (rectangle_list_idt[other_rec_num][2],rectangle_list_idt[other_rec_num][3])):
                if point == None:
                    continue
                else:
                    rec_points[rec_num].append(point)
#then remove invaild points
for rec_num in rec_points:
    for point_num in range(len(rec_points[rec_num])):
        for other_rec in range(len(rectangle_list_idt)):
            if rectangle_list_idt[rec_num] == rectangle_list_idt[other_rec]:
                continue
            if is_in_rec(rec_points[rec_num][point_num],rectangle_list_idt[other_rec]):
                rec_points[rec_num][point_num] = None
                break
#count perimeter
perimeter = []
for rec_num in rec_points:
    each_perimeter = 0
    each_side_points = defaultdict(list)
    for point_num in range(len(rec_points[rec_num])):
        if rec_points[rec_num][point_num] == None:
            continue
        else:
            if rec_points[rec_num][point_num][1] == rectangle_list_idt[rec_num][1]:
                each_side_points['D'].append(rec_points[rec_num][point_num])
            if rec_points[rec_num][point_num][1] == rectangle_list_idt[rec_num][3]:
                each_side_points['U'].append(rec_points[rec_num][point_num])
            if rec_points[rec_num][point_num][0] == rectangle_list_idt[rec_num][0]:
                each_side_points['L'].append(rec_points[rec_num][point_num])
            if rec_points[rec_num][point_num][0] == rectangle_list_idt[rec_num][2]:
                each_side_points['R'].append(rec_points[rec_num][point_num])
    for side in each_side_points:
        if side == 'D' or side == 'U':
            each_side_points[side] = sorted(each_side_points[side],key=lambda item:item[0])
        if side == 'R' or side == 'L':
            each_side_points[side] = sorted(each_side_points[side],key=lambda item:item[1])
    for side in each_side_points:
        point_on_side_num = 0
        while point_on_side_num<len(each_side_points[side]) - 1:
            if side == 'D' or side == 'U':
                each_perimeter += each_side_points[side][point_on_side_num+1][0] - each_side_points[side][point_on_side_num][0]
            if side == 'L' or side == 'R':
                each_perimeter += each_side_points[side][point_on_side_num+1][1] - each_side_points[side][point_on_side_num][1]
            point_on_side_num += 2
    perimeter.append(each_perimeter)
total_perimeter = 0
for i in perimeter:
    total_perimeter+=i



print(f'The perimeter is: {total_perimeter}')
