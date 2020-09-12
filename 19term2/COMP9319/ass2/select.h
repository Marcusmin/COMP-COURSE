#include <string.h>
#include "htools.h"
#include "rfile.h"
int selectOne(PBLIST b, int rankV);
int selectOneF(FILE* bfp, PINDEXOFBLIST idxofB, UINT idxSize, int rank);
int rankCV(char q, char* s, int i);
// int rankCVF(char q, FILE*fp, int ctr);
int rankCVF(char q, FILE *fp,PINDEXOFSTRING index, int idxSize, int ctr);
int rankBV(PBLIST b, int i);
int rankBVF(FILE *fp, PINDEXOFBLIST blIndex, int size, int bi);
char nextChar(int* ctable, char c); 
void strrev(char * string);
char getValClist(FILE *fp, UINT index); 
size_t fgetsize(FILE *fp);
int selectSF(
    FILE *sfp,
    PINDEXOFSTRING idxOfS, UINT idxSSize, char ch,
    int rank
    ); 