#include <stdbool.h>
#include "htools.h"
#ifndef READABLE_ASCII
#define READABLE_ASCII 98
#endif
#ifndef DEFINE_PACKS
#define DEFINE_PACKS
    typedef struct pack_string {
        char *s;
        FILE* fp;
        int size;
    } PACKS, *PPACKS;
#endif

/* read the file whose suffix is b
@param bfile - constant char array, file name to be read
@param b_array - pointer to the char array, where store the b array
@param bsize - pointer a integer where stores the size of b_array
*/
bool read_b_file(const char *bfile, BITLIST* b_array, int *bsize);

/* read the file whose suffix is s
@param sfile - constant char array, filename to be read
@param s_array - pointer to a char array where store the characters in sfile
@param ssize - pointer to a integer where stores the number of chars in sfile
*/
void read_s_file(const char *sfile, BITLIST* s_array, int *ssize);

/* read the file whose suffix is bb, this kind of file is not provided in automarking
@param bbfile - const char array, filename to be read
@param bb_array - a pointer to an array of char in bbfile, a char stands for 8 bits array
@param bbsize - a pointer to the number of byte in bbfile
*/
bool read_bb_file(const char *bbfile, BITLIST* bb_array, int* bbsize);

FILE* readMoreString(FILE *fp, char **s, int *size);

#ifndef INDEXOFS
#define INDEXOFS 0
typedef struct indexOfSUnit {
    UINT row;
    UINT count[READABLE_ASCII];
} INDEXOFSTRING, *PINDEXOFSTRING;
#endif
void buildIndexOfString(FILE *fp, PINDEXOFSTRING* sIndexList, int* size);

#ifndef INDEXOFB
#define INDEXOFB 0
typedef struct indexOfBUnit {
    UINT byte;
    UINT count;
} INDEXOFBLIST, *PINDEXOFBLIST;
#endif

void buildIndexOfBList(FILE *fp, PINDEXOFBLIST*, UINT*);
void readIndexS(const char *idxsf, PINDEXOFSTRING*, UINT*);
void readIndexB(const char *idxsf, PINDEXOFBLIST*, UINT*);
void readIndexCtb(const char *idxctbf, int **cs);
