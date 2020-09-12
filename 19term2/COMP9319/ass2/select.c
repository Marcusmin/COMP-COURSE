#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "select.h"
#include "htools.h"
#include "rfile.h"

#define FREAD_JUMP 1024

size_t fgetsize(FILE *fp) {
    fpos_t curPos;
    size_t begin, end, fsize;
    fgetpos(fp, &curPos);
    fseek(fp, 0L, SEEK_END);
    end = ftell(fp);
    fseek(fp, 0L, SEEK_SET);
    begin = ftell(fp);
    fsetpos(fp, &curPos);
    return end - begin;
}
// if the rank value is larger than the bit list
// return neg 1 indicating error
// otherwise return index
// NOTE that the file would padding 1
// int selectOne(PBLIST b, int rankV);
inline int selectOne(PBLIST b, int rankV) {
    int blen = b->bitlen;
    BITLIST bl = b->bits;
    if (rankV >= blen) {
        return -1;
    }
    if (rankV <= 0) {
        return 0;
    }
    int index = 0;
    for(int i = 0; i < blen; i++) {
        if (GETCHARBIT(bl, i) == 1) {
            rankV--;
            if (rankV == 0) {
                index = i;
                break;
            }
        }
    }
    return index;
}

inline int selectOneF(FILE* bfp, PINDEXOFBLIST idxofB, UINT idxSize, int rank) {
    UINT start = 0;
    UINT end = idxSize - 1;
    UINT middle = (start + end) / 2;
    UINT res = 0;
    while(start <= end && middle > 0) {
        if (idxofB[middle].count == rank) {
            res = rank;
            break;
        } else if (rank < idxofB[middle].count) {
            end = middle-1;
        } else if (rank > idxofB[middle].count) {
            start = middle+1;
        }
        middle = (start+end)/2;
    }
    if (idxofB[middle].count == rank) {
        res = rank;
    }
    if (res == rank) {
        // backtrace nearest 1
        UINT offset = 0;
        UINT res = 0;
        while(offset <= idxofB[middle].byte) {
            fseek(bfp, idxofB[middle].byte-offset, SEEK_SET);
            char c = fgetc(bfp);
            for(int j = 7; j >= 0; j--) {
                if (GETBIT(c, j) == 1) {
                    res = (idxofB[middle].byte-offset)*8+j;
                    return res;
                }
            }
            offset++;
        }       
    } else if (idxofB[middle].count < rank) {
        // forward add 1
        UINT bitCtr = idxofB[middle].count;
        UINT offset = idxofB[middle].byte+1;
        UINT pos = (offset)*8-1;
        fseek(bfp, idxofB[middle].byte+1, SEEK_SET);
        char byte = 0;
        while(bitCtr < rank) {
            byte = fgetc(bfp);
            for(int j = 0; j <= 7; j++) {
                pos++;
                if(GETBIT(byte, j) == 1) {
                    bitCtr++;
                }
                if (bitCtr == rank) {
                    return pos;
                }
            }
        }
    } else if (idxofB[middle].count > rank) {
        // backtrace minus 1
        UINT bitCtr = 0;
        while(middle>0 && idxofB[middle].count > rank) {
            middle --;
        }
        if (idxofB[0].count > rank) {
            fseek(bfp, 0L, SEEK_SET);
            char byte = 0;
            UINT pos = 0;
            while(bitCtr < rank) {
                byte = fgetc(bfp);
                for(int j = 0; j <= 7; j++) {
                    if (GETBIT(byte, j) == 1) {
                        bitCtr++;
                    }
                    pos++;
                    if (bitCtr == rank) {
                        return pos-1;
                    }
                }
            }
            return pos-1;
        } else {
            // forward count bit
            //middle's bit position
            UINT pos = (idxofB[middle].byte+1)*8-1;
            fseek(bfp, idxofB[middle].byte+1, SEEK_SET);
            bitCtr = idxofB[middle].count;
            while(bitCtr < rank) {
                char byte = fgetc(bfp);
                for(int j = 0; j <= 7; j++) {
                    if (GETBIT(byte, j) == 1){
                        bitCtr++;
                    }
                    pos++;
                    if (bitCtr == rank) {
                        return pos;
                    }
                }
            }
            return pos;
        }
    }
}


// assume that there is no $
// index start from 0
// count how many characters lower than q
// in the s until i
// int rankCV(char q, char* s, int i); 
inline int rankCV(char q, char* s, int ctr) {
    int counter = 0;
    for(int i = 0; i <= ctr; i++) {
        if (s[i] == q) {
            counter++;
        }
    }
    return counter;
}
inline int rankCVF(char aq, FILE *fp, PINDEXOFSTRING index, int idxSize, int ctr) {
    int idxCounter = ctr / BLOCK_SIZE_STRING; // which block 
    int idxOffset = ctr % BLOCK_SIZE_STRING;  // offset in block
    char q;
    if (aq == 9) {
        q = 95;
    } else if (aq == 10) {
        q = 96;
    } else if (aq == 13) {
        q = 97;
    } else {
        q = aq - 32;
    }
    if (ctr < 0) {
        return 0;
    }
    if (idxCounter == 0 && idxOffset != BLOCK_SIZE_STRING-1) {
        UINT res = 0;
        fseek(fp, 0L, SEEK_SET);
        char* temp = (char *)calloc(BLOCK_SIZE_STRING, sizeof(char));
        fread(temp, sizeof(char), BLOCK_SIZE_STRING, fp);
        for(int i = 0; i <= idxOffset; i++) {
            if (aq == temp[i]) {
                res ++;
            }
        }
        free(temp);
        return res;
    } else if (idxCounter == 0 && idxOffset == BLOCK_SIZE_STRING-1) {
        return index[idxCounter].count[q];
    } else if (idxCounter >= idxSize) {
        UINT res = index[idxSize-1].count[q];
        UINT readrt = 0;
        fseek(fp, index[idxSize-1].row+1, SEEK_SET);
        char *temp = (char*)calloc(BLOCK_SIZE_STRING, sizeof(char));
        UINT restBlock = 0;
        while(restBlock<(idxCounter - idxSize)&&(readrt=fread(temp, sizeof(char), BLOCK_SIZE_STRING,fp))>=BLOCK_SIZE_STRING) {
            for(int i = 0; i < BLOCK_SIZE_STRING; i++) {
                if (aq == temp[i]) {
                    res ++;
                }
            }
            restBlock++;
            memset(temp, 0, BLOCK_SIZE_STRING);
        }
        memset(temp, 0, BLOCK_SIZE_STRING);
        fread(temp, sizeof(char), idxOffset+1, fp);
        for(int i =0; i <= idxOffset; i++) {
            if (aq == temp[i]) {
                res++;
            }
        }
        free(temp);
        return res;
    } else if (idxOffset == ctr) {
        return index[idxCounter].count[q];
    } else {
        fseek(fp, index[idxCounter-1].row+1, SEEK_SET);
        UINT res = index[idxCounter-1].count[q];
        UINT readrt = 0;
        char *temp = (char*) calloc(BLOCK_SIZE_STRING, sizeof(char));
        readrt = fread(temp, sizeof(char), BLOCK_SIZE_STRING, fp);
        for(UINT i = 0; i <= idxOffset; i++ ) {
            if (aq == temp[i]) {
                res ++;
            }
        }
        free(temp);
        return res;
    }
}

inline int rankBVF(FILE *fp, PINDEXOFBLIST blIndex, int size, int bi) {
    UINT by = bi/8; // byte offset
    UINT byOffset = bi%8;   // bit offset
    UINT idxCtr = by/BLOCK_SIZE_BIT;    // block offset
    // not included in index
    if (bi < 0) {
        return 0;
    }
    if (idxCtr >= size) {
        fseek(fp, blIndex[size-1].byte+1, SEEK_SET);
        UINT setBitCtr = blIndex[size-1].count;
        UINT readrt = 0;
        char *temp = calloc(BLOCK_SIZE_BIT, sizeof(char));
        UINT restBlock = 0;
        while ((restBlock < (idxCtr - size))&&(readrt = fread(temp, sizeof(char), BLOCK_SIZE_BIT, fp)) >= BLOCK_SIZE_BIT) {
            for(int i = 0; i < BLOCK_SIZE_BIT; i++) {
                setBitCtr += COUNT_BIT(temp[i]);
            }
            memset(temp, 0, BLOCK_SIZE_BIT);
            restBlock += 1;
        }
        // assumption that file is long enough
        // if (restBlock <(idxCtr - size) &&readrt > 0) {
        //     for(int i = 0; i < readrt; i++) {
        //         setBitCtr += COUNT_BIT(temp[i]);
        //     }
        // }
        UINT idxOffset = by%BLOCK_SIZE_BIT;
        memset(temp, 0, BLOCK_SIZE_BIT);
        // assumption that the file is long enough
        fread(temp, sizeof(char), idxOffset+1, fp);
        for(int i = 0; i < idxOffset; i++) {
            setBitCtr += COUNT_BIT(temp[i]);
        }
        for (int i = 0; i <= byOffset; i++) {
            if (GETBIT(temp[idxOffset], i) == 1) {
                setBitCtr ++;
            }
        }
        free(temp);
        return setBitCtr;
    } else if (idxCtr == 0) {
        fseek(fp, 0L, SEEK_SET);
        UINT setBitCtr = 0;
        char *temp = (char *)calloc(BLOCK_SIZE_BIT, sizeof(char));
        UINT readrt = fread(temp,sizeof(char), BLOCK_SIZE_BIT, fp);
        for(int i = 0; i < by; i++) {
            setBitCtr += COUNT_BIT(temp[i]);
        }
        for(int i = 0; i <= byOffset; i++) {
            if (GETBIT(temp[by], i) == 1) {
                setBitCtr ++;
            }
        }
        free(temp);
        return setBitCtr;
    } else {
        UINT setBitCtr = blIndex[idxCtr-1].count;
        char *temp = (char*) calloc(by%BLOCK_SIZE_BIT+1, sizeof(char));
        UINT tempSize = by%BLOCK_SIZE_BIT+1;
        fseek(fp, blIndex[idxCtr-1].byte+1, SEEK_SET);
        fread(temp, sizeof(char), by%BLOCK_SIZE_BIT+1, fp);
        for(int i = 0; i < by%BLOCK_SIZE_BIT; i++) {
            setBitCtr += COUNT_BIT(temp[i]);
        }
        for(int i = 0; i <= byOffset; i++) {
            if (GETBIT(temp[by%BLOCK_SIZE_BIT], i) == 1) {
                setBitCtr++;
            }
        }
        free(temp);
        return setBitCtr;
    }
}

inline int rankBV(PBLIST b, int i) {
    BITLIST bl = b->bits;
    int blen = b->bitlen;
    int res = 0;
    int bi = 0;
    // printf("\n");
    if (i < 8) {
        for(int j = 0;j <= i%8; j++) {
            if (GETBIT(bl[bi],j)==1) {
                res++;
            }
        }
        return res;
    }
    for(bi = 0; bi < i/8; bi++) {
        res += COUNT_BIT(bl[bi]);
    }
    for(int j = 0;j <= i%8; j++) {
        if (GETBIT(bl[bi],j)==1) {
            res++;
        }
    }
    return res;
}
// next char in s

// char nextChar(int* ctable, char c);
inline char nextChar(int* ctable, char c) {
    for(int i = c+1; i < ASCIILEN; i++) {
        if (ctable[i] != -1) {
            return i;
        }
    }
    return c;
}

inline void strrev(char * string) {
    int len = strlen(string);
    int begin = 0;
    int end = len-1;
    while(begin < end) {
        char temp = string[begin];
        string[begin] = string[end];
        string[end] = temp;
        begin++;
        end--;
    }
}

inline char getValClist(FILE *fp, UINT index) {
    int i = 0;
    char res;
    fseek(fp, index, SEEK_SET);
    res = fgetc(fp);
    return (char)res;
}

int selectSF(
    FILE *sfp,
    PINDEXOFSTRING idxOfS, UINT idxSSize, char ach,
    int rank
    ) {
    UINT start = 0;
    UINT end = idxSSize - 1;
    UINT middle = (start + end) / 2;
    UINT pos = -1;
    char ch;
    if (ach == 9) {
        ch = 95;
    } else if (ach == 10) {
        ch = 96;
    } else if (ach == 13) {
        ch = 97;
    } else {
        ch = ach - 32;
    }
    while(start <= end && middle != 0) {
        if (idxOfS[middle].count[ch] == rank) {
            pos = rank;
            break;
        } else if (idxOfS[middle].count[ch] > rank) {
            end = middle-1; 
        } else if (idxOfS[middle].count[ch] < rank) {
            start = middle+1;
        }
        middle = (start + end) / 2;
    }
    if (idxOfS[middle].count[ch] == rank) {
        pos = rank;
    }
    if (pos == rank) {
        // backward search, first come across
        int offset = idxOfS[middle].row - BLOCK_SIZE_STRING;
        char *temp = calloc(BLOCK_SIZE_STRING, sizeof(char));
        bool found = false;
        while(offset >= 0) {
            fseek(sfp, offset+1, SEEK_SET);
            fread(temp, sizeof(char), BLOCK_SIZE_STRING, sfp);
            for(int i = BLOCK_SIZE_STRING-1; i >= 0; i--) {
                if (temp[i] == ach) {
                    // offsetTable[ch][0] = offset + 1 + i ;
                    // found = true;
                    return offset + 1 + i;
                    // break;
                }
            }
            if (found) {
                break;
            }
            memset(temp, 0, BLOCK_SIZE_STRING);
            offset -= BLOCK_SIZE_STRING;
        }
        if (!found && offset < 0) {
                fseek(sfp, 0, SEEK_SET);
                UINT size = offset+BLOCK_SIZE_STRING+1;
                UINT tempSize = fread(temp, sizeof(char), size, sfp);
                for(int i = size-1; i > 0; i--) {
                if (temp[i] == ach) {
                    // offsetTable[ch][0] = i;
                    // break;
                    return i;
                    }
                }
        }
    } else if (idxOfS[middle].count[ch] < rank) {
        // forward search
        UINT preCount = idxOfS[middle].count[ch];
        UINT countByte = idxOfS[middle].row+1;
        fseek(sfp, idxOfS[middle].row+1, SEEK_SET);
        char *temp = (char *) calloc(FREAD_JUMP, sizeof(char));
        while (preCount < rank) {
            UINT tempSize = fread(temp, sizeof(char), FREAD_JUMP, sfp);
            for(UINT i = 0; i < tempSize; i++) {
                if (temp[i] == ach) {
                    preCount++;
                }
                if (preCount == rank) {
                    break;
                }
                countByte++;
            }
            memset(temp, 0, FREAD_JUMP);
        }
        free(temp);
        // offsetTable[ch][0] = countByte;
        return countByte;
    } else if (idxOfS[middle].count[ch] > rank) {
        // backward search
        while (middle > 0 && idxOfS[middle].count[ch] > rank) {
            middle --;
        }
        if (middle == 0) {
            fseek(sfp, 0L, SEEK_SET);
            char *temp = (char *)calloc(BLOCK_SIZE_STRING*2, sizeof(char));
            UINT recount = 0;
            UINT recountByte = 0;
            UINT tempSize = fread(temp, sizeof(char), BLOCK_SIZE_STRING*2, sfp);
            for(UINT i = 0; i < tempSize; i++) {
                if (temp[i] == ach) {
                    recount++;
                }
                if (recount == rank) {
                    // offsetTable[ch][0] = i;
                    return i;
                    // break;
                }
            }
            free(temp);
        } else {
            fseek(sfp, idxOfS[middle].row+1, SEEK_SET);
            UINT preCount = idxOfS[middle].count[ch];
            UINT countByte = idxOfS[middle].row;
            char *temp = (char *)calloc(BLOCK_SIZE_STRING, sizeof(char));
            while (preCount < rank) {
                UINT tempSize = fread(temp, sizeof(char), BLOCK_SIZE_STRING, sfp);
                for(UINT i = 0; i < tempSize; i++) {
                    if (temp[i] == ach) {
                        preCount++;
                    }
                    if (preCount == rank) {
                        break;
                    }
                    countByte++;
                }
                memset(temp, 0, BLOCK_SIZE_STRING);
            }
            // offsetTable[ch][0] = countByte;
            free(temp);
            return countByte;
        }
    }
}