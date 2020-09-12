#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "wfile.h"
#include "select.h"
#include "htools.h"
#define ASCOPE 256
#define FREAD_BLOCK_SIZE (1024)
#define FREAD_JUMP 1024
#define OFFSET_TABLE_SIZE (1024*1024/4)
// write the b' to the file

/*
Input s file generate offset table

*/
void genBBFile(
    FILE *sfp, FILE *bfp, const char *bbf, PINDEXOFBLIST idxOfB, UINT idxSize,
    PINDEXOFSTRING idxOfS, UINT idxSSize
    ) {
    UINT readrt;
    fseek(sfp, 0L, SEEK_SET);
    UINT *offsetTable[ASCOPE];
    UINT *offsetRecord = calloc(OFFSET_TABLE_SIZE, sizeof(UINT));
    for(int i = 0; i < ASCOPE; i++) {
        offsetTable[i] = (UINT *)calloc(2, sizeof(UINT));
    }
    char *rblock = (char *)calloc(FREAD_BLOCK_SIZE, sizeof(char));
    UINT rblockCtr = 0;
    while ((readrt = fread(rblock, sizeof(char), FREAD_BLOCK_SIZE, sfp))>=FREAD_BLOCK_SIZE) {
        for(int i = 0; i < FREAD_BLOCK_SIZE; i++) {
            char c = rblock[i];
            if (offsetTable[c][1] == 0) {
                // [0] is offset
                // [1] is count number
                offsetTable[c][0] = rblockCtr*FREAD_BLOCK_SIZE+i;   // mark offset
            }
            offsetTable[c][1] ++;
        }
        rblockCtr++;
        memset(rblock, 0, FREAD_BLOCK_SIZE);
    }
    if (readrt > 0) {
        for (int i = 0; i < readrt; i++) {
            char c = rblock[i];
            if (offsetTable[c][1] == 0) {
                offsetTable[c][0] = rblockCtr * FREAD_BLOCK_SIZE+i;
            }
            offsetTable[c][1]++;
        }
    }
    memset(rblock, 0, SEEK_SET);
    // printf("Debug: Build Table Done\n");
    // build offset table, build bb file
    FILE *bbfp = fopen(bbf, "wb");
    UINT bitPointer = 0;
    // bb file write buffer
    char *wblock = (char *)calloc(FREAD_BLOCK_SIZE, sizeof(char));
    memset(wblock, 0xff, FREAD_BLOCK_SIZE);
    // count how many block has been write to file
    UINT wblockCtr  = 0;
    // count how many bit has been traversed
    UINT wbitCtr = 0;
    for(int ch = 0; ch < ASCIILEN; ch++) {
        // UINT rank = 1;
        // open a file which used to mark the previous offset
        // FILE *idxfp = fopne("INDEX/offsetIndex", "wb");
        fpos_t preReadPos;
        UINT rbyCtr = 0;
        UINT offsetP = 0;
        rblockCtr = 0;
        bool isFull = false;
        fseek(sfp, 0L, SEEK_SET);
        memset(offsetRecord, 0L, OFFSET_TABLE_SIZE);
        while ((readrt = fread(rblock, sizeof(char), FREAD_BLOCK_SIZE, sfp))>=FREAD_BLOCK_SIZE) {
            for(int i = 0; i < FREAD_BLOCK_SIZE; i++) {
                char c = rblock[i];
                rbyCtr++;
                if (c == ch) {
                    offsetRecord[offsetP] = rbyCtr-1;
                    offsetP++;
                    if (offsetP >= OFFSET_TABLE_SIZE) {
                        fseek(sfp, rbyCtr, SEEK_SET);
                        fgetpos(sfp, &preReadPos);
                        isFull = true;
                        break;
                    }
                }
            }
            if (isFull) {
                break;
            }
            rblockCtr++;
            memset(rblock, 0, FREAD_BLOCK_SIZE);
        }
        if (!isFull && readrt > 0) {
            // printf("hhhh\n");
            for (int i = 0; i < readrt; i++) {
                rbyCtr ++;
                char c = rblock[i];
                if (c == ch) {
                    offsetRecord[offsetP] = rbyCtr-1;
                    offsetP++;
                    if (offsetP >= OFFSET_TABLE_SIZE) {
                        fseek(sfp, rbyCtr, SEEK_SET);
                        fgetpos(sfp, &preReadPos);
                        break;
                    }
                }
            }
        }
        isFull = false;
        UINT recordSize = offsetP;
        // offsetRecord = realloc(offsetRecord, recordSize*sizeof(UINT));
        offsetP = 0;
        while (offsetTable[ch][1] > 0) {
            // printf("No%d %c offset is %d \n",rank, ch, offsetTable[ch][0]);
            // printf("%c offset is %d \n", ch, offsetRecord[offsetP]);
            // write buffer
            UINT first, last;
            // UINT offset = offsetTable[ch][0];
            UINT offset = offsetRecord[offsetP];
            first = selectOneF(bfp, idxOfB, idxSize, offset+1); // bit offset in b
            // printf("%c's first is %d \n", ch, first);
            UINT byOffset = first / 8;  // which byte in b
            UINT biOffset = first % 8;  // in byte's which bit
            // printf("biOffset is %d\n", biOffset);
            UINT zeroCtr = 0;   // count how many zero
            fseek(bfp, byOffset, SEEK_SET);
            //  count how many 0 in the range
            bool firstSearch = true;
            while (feof(bfp) == 0) {
                char b = fgetc(bfp);
                UINT i;
                bool foundSet = false;
                if (firstSearch) {
                    i = biOffset + 1;
                    // printf("biOffset + 1 is %d \n", i);
                    firstSearch = false;
                } else {
                    i = 0;
                }
                while (i < 8) {
                    if (GETBIT(b, i) == 1) {
                        foundSet = true;
                        break;
                    } else {
                        // printf("\n");
                        zeroCtr++;
                    }
                    i++;
                }
                if (foundSet) {
                    break;
                }
            }
            last = first+zeroCtr;
            for(int i = first; i <= last; i++) {
                if (i == first) {
                    // skip
                } else {
                    UINT wbyOffsetInBlk = wbitCtr/8 - wblockCtr*FREAD_BLOCK_SIZE;
                    UINT wbiOffsetInByte = wbitCtr%8;
                    // printf("clear bit is %d\n", wbiOffsetInByte);
                    wblock[wbyOffsetInBlk] = CLEAR_BIT(wblock[wbyOffsetInBlk], wbiOffsetInByte);
                }
                wbitCtr++;
                if ((wbitCtr/8-wblockCtr*FREAD_BLOCK_SIZE)>=FREAD_BLOCK_SIZE) {
                    // wblock = realloc(wblock, (wbitCtr/8-wblockCtr*FREAD_BLOCK_SIZE)+1);
                    fwrite(wblock, sizeof(char), FREAD_BLOCK_SIZE, bbfp);
                    memset(wblock, 0xff, FREAD_BLOCK_SIZE);
                    wblockCtr++;
                }
            }
            // printf("%c's last is %d \n", ch, last);
            offsetTable[ch][1]--;
            if (offsetTable[ch][1] == 0) {
                continue;
            }
            offsetP++;
            // exceed the table size
            if (offsetP >= recordSize) {
                offsetP = 0;
                memset(offsetRecord, 0, OFFSET_TABLE_SIZE);
                fsetpos(sfp, &preReadPos);
                while ((readrt = fread(rblock, sizeof(char), FREAD_BLOCK_SIZE, sfp))>=FREAD_BLOCK_SIZE) {
                    for(int i = 0; i < FREAD_BLOCK_SIZE; i++) {
                        char c = rblock[i];
                        rbyCtr++;
                        if (c == ch) {
                            offsetRecord[offsetP] = rbyCtr-1;
                            offsetP++;
                            if (offsetP >= OFFSET_TABLE_SIZE) {
                                fseek(sfp, rbyCtr, SEEK_SET);
                                fgetpos(sfp, &preReadPos);
                                isFull = true;
                                break;
                            }
                        }
                    }
                    if (isFull) {
                        break;
                    }
                    rblockCtr++;
                    memset(rblock, 0, FREAD_BLOCK_SIZE);
                }
                if (!isFull && readrt > 0) {
                    for (int i = 0; i < readrt; i++) {
                        char c = rblock[i];
                        rbyCtr++;
                        if (c == ch) {
                            offsetRecord[offsetP] = rbyCtr-1;
                            offsetP++;
                            if (offsetP >= OFFSET_TABLE_SIZE) {
                                fseek(sfp, rbyCtr, SEEK_SET);
                                fgetpos(sfp, &preReadPos);
                                break;
                            }
                        }
                    }
                }
                memset(rblock, 0, FREAD_BLOCK_SIZE);
                recordSize = offsetP;
                // offsetRecord = realloc(offsetRecord, recordSize*sizeof(UINT));
                offsetP = 0;
            }
        }
    }
    wblock = realloc(wblock, (wbitCtr/8-wblockCtr*FREAD_BLOCK_SIZE)+1);
    fwrite(wblock, sizeof(char), (wbitCtr/8-wblockCtr*FREAD_BLOCK_SIZE)+1, bbfp);
    for(int i = 0; i < ASCIILEN; i++) {
        free(offsetTable[i]);
    }
    free(offsetRecord);
    free(wblock);
    free(rblock);
    fclose(bbfp);
}

void writeIndexB(const char* idxbf, PINDEXOFBLIST idxB, UINT size) {
    FILE *bifp = fopen(idxbf, "wb");
    for(UINT i = 0; i < size; i++) {
        UINT context[2];
        context[0] = idxB[i].byte;
        context[1] = idxB[i].count;
        fwrite(context, sizeof(UINT), 2, bifp);
    }
    fclose(bifp);
}

void writeIndexS(const char *idxsf, PINDEXOFSTRING idxS, UINT size) {
    FILE *bifp = fopen(idxsf, "wb");
    for(UINT i = 0; i < size; i++) {
        UINT context[99];
        context[0] = idxS[i].row;
        for(int j = 0; j < READABLE_ASCII; j++) {
            context[j+1] = idxS[i].count[j];
        }
        fwrite(context, sizeof(UINT), 99, bifp);
    }
    fclose(bifp);
}

void writeIndexCtb(const char *name, int *cs) {
    FILE *fp = fopen(name, "wb");
    fwrite(cs, sizeof(int), ASCIILEN, fp);
    fclose(fp);
}