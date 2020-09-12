rlebwt: rlebwt.o search.o rfile.o wfile.o ctable.o select.o
	gcc -std=c11 rlebwt.o search.o rfile.o wfile.o ctable.o select.o -o rlebwt 

rlebwt.o: rlebwt.c search.h 
	gcc -std=c11 -c rlebwt.c -o rlebwt.o 

search.o: search.c search.h select.h wfile.h rfile.h
	gcc -std=c11 -c search.c -o search.o 

select.o: select.c select.h rfile.h htools.h
	gcc -std=c11 -c select.c -o select.o 

ctable.o: ctable.c 
	gcc -std=c11 -c ctable.c -o ctable.o 

rfile.o: rfile.c select.h rfile.h htools.h
	gcc -std=c11 -c rfile.c -o rfile.o 

wfile.o: wfile.c wfile.h select.h htools.h
	gcc -std=c11 -c wfile.c -o wfile.o 

clean:
	rm rlebwt *.o