binary : Stack.o binary.o
	gcc -o binary binary.o Stack.o
binary.o: binary.c Stack.h
	gcc -Wall -Werror -std=c11 -c binary.c
Stack.o : Stack.c Stack.h
	gcc -Wall -Werror -std=c11 -c Stack.c
