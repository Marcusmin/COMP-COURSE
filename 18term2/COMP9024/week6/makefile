CC = gcc
CCFLAGS = -Wall -Werror -std=c11
test: Graph.o	graphAnalyser.o
	$(CC) $(CCFLAGS) -o test Graph.o	graphAnalyser.o
graphAnalyser.o:	graphAnalyser.c Graph.h
	$(CC) $(CCFLAGS) -c graphAnalyser.c
Graph.o:	Graph.c Graph.h
	$(CC) $(CCFLAGS) -c Graph.c
