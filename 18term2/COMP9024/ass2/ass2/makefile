poG : poG.o WGraph.o stack.o queue.o
	gcc -o poG poG.o WGraph.o stack.o queue.o -lm -g
poG.o : poG.c WGraph.h stack.h
	gcc -Wall -Werror -std=c11 -c poG.c
WGraph.o : WGraph.c WGraph.h stack.h queue.h
	gcc -Wall -Werror -std=c11 -c WGraph.c
stack.o : stack.c stack.h
	gcc -Wall -Werror -std=c11 -c stack.c
queue.o : queue.c queue.h
	gcc -Wall -Werror -std=c11 -c queue.c
clean :
	rm -rf poG poG.o WGraph.o stack.o queue.o core
