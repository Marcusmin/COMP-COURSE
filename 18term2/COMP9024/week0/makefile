llbuild: linklist.o llbuild.o
	gcc -o llbuild llbuild.o linklist.o
llbuild.o: llbuild.c linklist.h
	gcc -Wall -Werror -std=c11 -c llbuild.c
linklist.o: linklist.h linklist.c
	gcc -Wall -Werror -std=c11 -c linklist.c
