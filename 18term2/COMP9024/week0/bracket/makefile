rbt : bracket.o Stack.o
	gcc -o rbt bracket.o Stack.o

bracket.o : bracket.c Stack.h
	gcc -Wall -Werror -std=c11 -c bracket.c

Stack.o : Stack.c Stack.h
	gcc -Wall -Werror -std=c11 -c Stack.c
