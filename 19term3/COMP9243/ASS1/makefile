all: dsm libsm share matmul 
matmul: matmul.c libsm.a
	gcc matmul.c -o matmul -L. -lsm
share: share.c libsm.a
	gcc share.c -o share -L. -lsm
dsm: sm.h dsm.c
	gcc dsm.c -o dsm -std=c99

libsm: sm.o
	ar cr libsm.a sm.o

sm.o: sm.c sm.h
	gcc -c sm.c -o sm.o -std=c99

clean:
	rm *.o *.a dsm share matmul 
