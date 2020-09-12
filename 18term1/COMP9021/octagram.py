from turtle import *
from math import *

vertice_oct = []
vertice_octagram = []
poly_angle = 45
edge_length = 80

def draw_octtangle(i,colour):
    color(colour)
    begin_fill()
    goto(vertice_oct[i])
    goto(vertice_oct[i + 1])
    end_fill()

def draw_octagram(i, colour):
    color(colour)
    goto(vertice_oct[i])
    begin_fill()
    goto(vertice_octagram[i])
    goto(vertice_oct[i + 1])
    end_fill()

def get_pos_octangle():
    penup()
    for i in range(8):
        left(i * poly_angle)
        forward(edge_length)
        vertice_oct.append(pos())
        home()
    vertice_oct.append(vertice_oct[0])
    pendown()

def get_pos_octagram():
    penup()
    for i in range(8):
        left(i * poly_angle + 45 / 2)
        forward(sqrt(3) * edge_length)
        vertice_octagram.append(pos())
        home()
    pendown()

get_pos_octangle()
get_pos_octagram()
for i in range(8):
    home()
    draw_octtangle(i,'yellow')
for i in range(8):
    colour = 'red' if i % 2 else 'blue'
    draw_octagram(i, colour)