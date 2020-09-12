from turtle import  *

edge_length = 80
first_angle = 60
second_angle = 120

def hexagram_part(color1,color2):
        for _ in range(3):
            color(color1)
            forward(edge_length)
            left(120)
            forward(edge_length)
            right(60)
            color(color2)
            forward(edge_length)
            left(120)
            forward(edge_length)
            right(60)

def find_postion():
    left(180)
    penup()
    forward(edge_length // 2)
    pendown()

find_postion()
hexagram_part('blue','red')