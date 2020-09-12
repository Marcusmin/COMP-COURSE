// #include <iostream>
// #include <fstream>
// #include <string>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include "select.h"
#include "rfile.h"
#include "htools.h"
// using namespace std;
// bool read_b_file(const char *bfile, BITLIST *bits, int *bsize) {
//     if (bfile == NULL) {
//         return false;
//     }
//     FILE *fp = fopen(bfile, "rb");
//     char b;
//     size_t begin, end, fsize;
//     int i = 0;
//     // get the size of current file
//     fseek(fp, 0L, SEEK_END);
//     end = ftell(fp);
//     fseek(fp, 0L, SEEK_SET);
//     begin = ftell(fp);
//     fsize  = end - begin;
//     *bits = (char *)calloc(fsize, sizeof(char));
//     if (*bits == NULL) {
//         return false;
//     }
//     // init a char array
//     int c;
//     while ((c=fgetc(fp))!=EOF) {
//         (*bits)[i] = (char)c;
//         i++;
//     }
//     *bsize = i*sizeof(char)*8;
//     fclose(fp);
//     return true;
// }

// void read_s_file(const char *sfile, BITLIST* s, int *size) {
//     FILE* fp = fopen(sfile, "rb");
//     char sc;
//     size_t fsize, begin, end;
//     int i = 0;
//     // get the size of the file
//     fseek(fp, 0L, SEEK_END);
//     end = ftell(fp);
//     fseek(fp, 0L, SEEK_SET);
//     begin = ftell(fp);
//     fsize = end - begin;
//     *s = (char *)calloc(fsize, sizeof(char));
//     for(int c = 0;(c=fgetc(fp))!=EOF; i++) {
//         (*s)[i] = (char) c;
//     }
//     *size = i;
// }

// FILE* readMoreString(FILE *fp, char **s, int *size) {
//     int i = 0;
//     // check the left size of the file
//     size_t fsize, begin, end;
//     fpos_t curPos;
//     fgetpos(fp, &curPos);
//     begin = ftell(fp);
//     fseek(fp, 0L, SEEK_END);
//     end = ftell(fp);
//     fsetpos(fp, &curPos);
//     fsize = end - begin;
//     if (fsize < MAX_S_LEN) {
//         (*s) = (char *)realloc(*s, fsize*sizeof(char));
//         for(int j = 0; j < fsize; j++) {
//             (*s)[j] = 0;
//         }
//         *size = fsize;
//     } else {
//         (*s) = (char *)realloc(*s, MAX_S_LEN*sizeof(char));
//         for(int j = 0; j < MAX_S_LEN; j++) {
//             (*s)[j] = 0;
//         }
//         *size = MAX_S_LEN;
//     }
//     for(int c = 0; i < MAX_S_LEN&&(c=fgetc(fp))!=EOF; i++) {
//         (*s)[i] = (char) c;
//     }
//     return fp;
// }

// bool read_bb_file(const char *bbfile, BITLIST* bb, int *size) {
//     FILE* fp = fopen(bbfile, "rb");
//     char b;
//     size_t fsize, begin, end;
//     int i = 0;
//     // get the size of current file
//     fseek(fp, 0L, SEEK_END);
//     end = ftell(fp);
//     fseek(fp, 0L, SEEK_SET);
//     begin = ftell(fp);
//     fsize  = end - begin;
//     // init a char array
//     *bb = (char *)calloc(fsize, sizeof(char));
//     if (*bb == NULL) {
//         return false;
//     }
//     for(int c = 0; (c=fgetc(fp))!=EOF; i++) {
//         (*bb)[i] = (char)c;
//     }
//     *size = i*sizeof(char)*8;
//     fclose(fp);
//     return true;
// }

// assume the gap is 2M
void buildIndexOfString(FILE *fp, PINDEXOFSTRING * sIndexList, int* size) {
    fseek(fp, 0L, SEEK_SET);
    UINT charCounter[READABLE_ASCII];
    UINT readrt;
    UINT indexCtr = 0;
    UINT blockCtr = 0;
    (*sIndexList) = (PINDEXOFSTRING) calloc(INDEX_SIZE_STRING, sizeof(INDEXOFSTRING));
    memset(charCounter, 0, sizeof(UINT)*READABLE_ASCII);
    char *readBlock = (char *)calloc(BLOCK_SIZE_STRING, sizeof(char));
    while((readrt=fread(readBlock, sizeof(char), BLOCK_SIZE_STRING, fp))>=BLOCK_SIZE_STRING && indexCtr<INDEX_SIZE_STRING-1) {
        blockCtr += BLOCK_SIZE_STRING;
        for(UINT i = 0; i < BLOCK_SIZE_STRING; i++) {
            char c = readBlock[i];
            if (c == 9) {
                c = 95;
            } else if (c == 10) {
                c = 96;
            } else if (c == 13) {
                c = 97;
            } else {
                c = c - 32;
            }
            charCounter[c] ++;
        }
        for(int i = 0; i < READABLE_ASCII; i++) {
            (*sIndexList)[indexCtr].count[i] = charCounter[i];            
        }
        (*sIndexList)[indexCtr].row = blockCtr-1;
        indexCtr++;
        memset(readBlock, 0, sizeof(char)*BLOCK_SIZE_STRING);
    }
    if (readrt > 0) {
        blockCtr += readrt;
        for(UINT i = 0; i < readrt; i++) {
            char c = readBlock[i];
            if (c == 9) {
                c = 95;
            } else if (c == 10) {
                c = 96;
            } else if (c == 13) {
                c = 97;
            } else {
                c = c - 32;
            }
            charCounter[c]++;
        }
        for(int i = 0; i < READABLE_ASCII; i++) {
            (*sIndexList)[indexCtr].count[i] = charCounter[i];            
        }
        (*sIndexList)[indexCtr].row = blockCtr - 1;
    }
    (*sIndexList) = (PINDEXOFSTRING) realloc(*sIndexList, (indexCtr+1)*sizeof(INDEXOFSTRING));
    free(readBlock);
    *size = indexCtr+1;
}


void buildIndexOfBList(FILE *fp, PINDEXOFBLIST * bIndexList, UINT* size) {
    fseek(fp, 0L, SEEK_SET);
    (*bIndexList) = (PINDEXOFBLIST) calloc(INDEX_SIZE_BIT, sizeof(INDEXOFBLIST));
    char *bytes = (char *)calloc(BLOCK_SIZE_BIT, sizeof(char));
    UINT readCtr;
    UINT idxCtr = 0;
    UINT blockCtr = 0;
    UINT bitCounter = 0;
    while ((readCtr = fread(bytes, sizeof(char), BLOCK_SIZE_BIT, fp)) >= BLOCK_SIZE_BIT && idxCtr<INDEX_SIZE_BIT-1) {
        blockCtr += BLOCK_SIZE_BIT;
        for(int i = 0; i < BLOCK_SIZE_BIT;i++) {
            char c = bytes[i];
            bitCounter += COUNT_BIT(c);
        }
        (*bIndexList)[idxCtr].byte = blockCtr-1;
        (*bIndexList)[idxCtr].count = bitCounter;
        memset(bytes, 0, BLOCK_SIZE_BIT);
        idxCtr ++;
    }
    if (readCtr > 0) {
        blockCtr += readCtr;
        for(UINT i = 0; i < readCtr; i++) {
            bitCounter += COUNT_BIT(bytes[i]);
        }
        (*bIndexList)[idxCtr].count = bitCounter;
        (*bIndexList)[idxCtr].byte = blockCtr - 1;
    }
    (*bIndexList) = (PINDEXOFBLIST) realloc(*bIndexList, (idxCtr+1)*sizeof(INDEXOFBLIST));
    *size = idxCtr+1;
    free(bytes);
}
void readIndexS(const char *idxsf, PINDEXOFSTRING* idxS, UINT* size) {
    FILE *idxSfp = fopen(idxsf, "rb");
    UINT byteCtr = fgetsize(idxSfp);
    UINT indexSize = byteCtr/(99*4);
    (*idxS) = (PINDEXOFSTRING) calloc(indexSize, sizeof(INDEXOFSTRING));
    fseek(idxSfp, 0L, SEEK_SET);
    for(int i = 0; i < indexSize; i++) {
        UINT row;
        fread(&row, sizeof(UINT), 1, idxSfp);
        (*idxS)[i].row = row;
        UINT count[READABLE_ASCII];
        fread(count, sizeof(UINT), READABLE_ASCII, idxSfp);
        for(int j = 0; j < READABLE_ASCII; j++) {
            (*idxS)[i].count[j] = count[j];
        }
    }
    *size = indexSize;
    fclose(idxSfp);
}

void readIndexB(const char *idxbf, PINDEXOFBLIST* idxB, UINT* size) {
    FILE *idxBfp = fopen(idxbf, "rb");
    UINT byteCtr = fgetsize(idxBfp);
    UINT indexSize = byteCtr / (2*4);
    (*idxB) = (PINDEXOFBLIST) calloc(indexSize, sizeof(INDEXOFBLIST));
    for(int i = 0; i < indexSize; i++) {
        UINT byte;
        fread(&byte, sizeof(UINT), 1, idxBfp);
        (*idxB)[i].byte = byte;
        UINT count;
        fread(&count, sizeof(UINT), 1, idxBfp);
        (*idxB)[i].count = count;
    }
    *size = indexSize;
    fclose(idxBfp);
}

void readIndexCtb(const char *name, int **cs) {
    FILE *fp = fopen(name, "rb");
    (*cs) = (int *) calloc(ASCIILEN, sizeof(int));
    int *temp = calloc(ASCIILEN, sizeof(int));
    fread(temp, sizeof(int), ASCIILEN, fp);
    for(int i = 0; i < ASCIILEN; i++) {
        (*cs)[i] = temp[i];
    }
    free(temp);
    fclose(fp);
}