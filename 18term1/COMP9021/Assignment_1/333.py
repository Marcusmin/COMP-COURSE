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
            if line_begin[1]>=rectangle[1] and line_end[1]<=rectangle[3]:
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
print(find_inter_point([-5, 8, 20, 25],(-15,10),(5,10)))