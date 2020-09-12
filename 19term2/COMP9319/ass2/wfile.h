#include <stdbool.h>
#include "htools.h"
#include "rfile.h"
// write the b' to the file
void write_bb_file(const char* bbfile, const char* bb, int bbsize);

// generate the b' in memory
bool generate_bb_array(
    const char* b,
    const char* s,
    char **bb,
    int *bbsize,
    int bsize, 
    int ssize
);
void genBBFile(FILE *sfp, FILE *bfp, const char *bbf, PINDEXOFBLIST idxOfB, UINT idxSize, PINDEXOFSTRING, UINT);

#ifndef OFFSETTBSIZE
#define OFFSETTBSIZE (1024*1024*4)
typedef struct offsetTable {
    char ch;    // stand for a char
    UINT *offsets;  // unsigned int
    UINT offsetcp;  // capability
    UINT offsetp;
} OFFSETTABLE, *POFFSETTABLE;
#endif

void writeIndexB(const char* idxbf, PINDEXOFBLIST, UINT);
void writeIndexS(const char* idxsf, PINDEXOFSTRING, UINT);
void writeIndexCtb(const char * ,int *);