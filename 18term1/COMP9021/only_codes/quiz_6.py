# Defines two classes, Point() and Triangle().
# An object for the second class is created by passing named arguments,
# point_1, point_2 and point_3, to its constructor.
# Such an object can be modified by changing one point, two or three points
# thanks to the method change_point_or_points().
# At any stage, the object maintains correct values
# for perimeter and area.
#
# Written by *** and Eric Martin for COMP9021


from math import sqrt


class PointError(Exception):
    def __init__(self, message):
        self.message = message


class Point():
    def __init__(self, x = None, y = None):
        if x is None and y is None:
            self.x = 0
            self.y = 0
        elif x is None or y is None:
            raise PointError('Need two coordinates, point not created.')
        else:
            self.x = x
            self.y = y

    def coordinates(self):
        return self.x, self.y
    # Possibly define other methods


class TriangleError(Exception):
    def __init__(self, message):
        self.message = message


class Triangle:
    def __init__(self, *, point_1, point_2, point_3):
        self.point_1 = point_1.coordinates()
        self.point_2 = point_2.coordinates()
        self.point_3 = point_3.coordinates()
        self.copy_point_1 = point_1.coordinates()
        self.copy_point_2 = point_2.coordinates()
        self.copy_point_3 = point_3.coordinates()
        self.check_if_a_valid_triangle()
        self.perimeter = self.calculate_perimeter()
        self.area = self.calculate_area()
        # Replace pass above with your code

    def calculate_perimeter(self):
        return self.line_1 + self.line_2 + self.line_3

    def calculate_area(self):
        self.semiperimeter = self.perimeter / 2
        return sqrt(self.semiperimeter * (self.semiperimeter - self.line_1) * (self.semiperimeter - self.line_2) * (self.semiperimeter - self.line_3))

    def check_if_a_valid_triangle(self):
        self.line_1 = sqrt((self.point_1[0] - self.point_2[0])**2 + (self.point_1[1] - self.point_2[1])**2)
        self.line_2 = sqrt((self.point_2[0] - self.point_3[0])**2 + (self.point_2[1] - self.point_3[1])**2)
        self.line_3 = sqrt((self.point_3[0] - self.point_1[0])**2 + (self.point_3[1] - self.point_1[1])**2)
        if self.line_1 + self.line_2 > self.line_3 and self.line_1 + self.line_3 > self.line_2\
           and self.line_2 + self.line_3 > self.line_1:
            pass
        else:
            raise TriangleError('Incorrect input, triangle not created.')
    
    def change_point_or_points(self, *, point_1 = None,point_2 = None, point_3 = None):
            try:
                if point_1 != None:
                    self.point_1 = point_1.coordinates()
                if point_2 != None:
                    self.point_2 = point_2.coordinates()
                if point_3 != None:
                    self.point_3 = point_3.coordinates()
                self.check_if_a_valid_triangle()
                self.perimeter = self.calculate_perimeter()
                self.area = self.calculate_area()
            except TriangleError:
                print(TriangleError('Incorrect input, triangle not modified.').message)
                self.point_1 = self.copy_point_1
                self.point_2 = self.copy_point_2
                self.point_3 = self.copy_point_3
        # Replace pass above with your code

    # Possibly define other methods
        

            
            

