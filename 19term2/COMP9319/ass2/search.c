#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include "search.h"
#include "select.h"
#include "wfile.h"
#include "rfile.h"
#define BSUFFIXLEN 3
#define BBSUFFIXLEN 4
#define SSUFFIXLEN 3
#define MAX_ID_LEN 12

void search(const char* filename, const char* indexFolder, const char* query, const char option, bool flag) {
    lfsearch(filename, indexFolder, query, option, flag);
}


// int comp(const void*, const void*);
static inline int comp(const void *ela, const void *elb) {
    int a = *((int *)ela);
    int b = *((int *)elb);
    if (a > b) return 1;
    if (a < b) return -1;
    return 0;
}

//==================================================================================================================================

void lfsearch(const char* filename, const char* indexFolder, const char* query, const char option, bool flag) {
    // char *cp_filename = (char *) calloc(strlen(filename)+1, sizeof(char));
    // char *bname = basename(cp_filename);
    char *bf = (char*)calloc(strlen(filename)+BSUFFIXLEN, sizeof(char));
    char *bbf = (char*)calloc(strlen(filename)+BBSUFFIXLEN, sizeof(char));
    char *sf = (char*) calloc(strlen(filename)+SSUFFIXLEN, sizeof(char));
    strcat(strcpy(bf, filename), ".b");
    strcat(strcpy(bbf, filename), ".bb");
    strcat(strcpy(sf, filename), ".s");
    char *idxbf = (char *)calloc(strlen(indexFolder)+4, sizeof(char));
    char *idxsf = (char *)calloc(strlen(indexFolder)+4, sizeof(char));
    char *idxbbf = (char *)calloc(strlen(indexFolder)+5, sizeof(char));
    char *idxctbf = (char *)calloc(strlen(indexFolder)+4, sizeof(char));
    strcat(strcpy(idxbf, indexFolder), "/bi");
    strcat(strcpy(idxbbf, indexFolder), "/bbi");
    strcat(strcpy(idxsf, indexFolder), "/si");
    strcat(strcpy(idxctbf, indexFolder), "/ctb");
    char* s = NULL;
    int *cs = NULL;
    int result = 0;
    int ssize, bsize, bbsize;
    UINT first, end;
    // create s strin
    FILE *sfp = fopen(sf, "rb");
    FILE *bfp = fopen(bf, "rb");
    FILE *bbfp;
    PINDEXOFSTRING indexOfString;
    PINDEXOFBLIST indexOfB;
    PINDEXOFBLIST indexOfBB;
    int indexSSize, indexBSize, indexBBSize;
    if (access(idxbf, F_OK) != -1) {
        readIndexB(idxbf, &indexOfB, &indexBSize);
    } else {
        buildIndexOfBList(bfp, &indexOfB, &indexBSize);
        writeIndexB(idxbf, indexOfB,indexBSize);
    }
    if (access(idxsf, F_OK) != -1) {
        readIndexS(idxsf, &indexOfString, &indexSSize);
    } else {
        buildIndexOfString(sfp, &indexOfString, &indexSSize);
        writeIndexS(idxsf,indexOfString,indexSSize);
    }
    if (flag) {
        bbfp = fopen(bbf, "rb");
    } else {
        // printf("GEn\n");
        genBBFile(sfp, bfp, bbf, indexOfB, indexBSize,indexOfString, indexSSize);
        bbfp = fopen(bbf, "rb");
    }
    if (access(idxbbf, F_OK) != -1) {
        readIndexB(idxbbf, &indexOfBB, &indexBBSize);
    } else {
        buildIndexOfBList(bbfp, &indexOfBB, &indexBBSize);
        writeIndexB(idxbbf, indexOfBB, indexBBSize);
    }
    if (access(idxctbf, F_OK)!=-1) {
        readIndexCtb(idxctbf, &cs);
    } else {
        createCtable(sfp, &cs);
        writeIndexCtb(idxctbf, cs);
    }
    if (option != 'n') {
        __lsearch(
            bbfp, indexOfBB, indexBBSize,
            bfp, indexOfB, indexBSize,
            cs,
            sfp, indexOfString, indexSSize, 
            query, &first, &end
        );
    }
    if (first > end) {
        result = 0;
    } else {
        result = end - first + 1;
    }
    if (option == 'm') {
        printf("%d\n", result);
    } else if (option == 'r') {
        result = __lrsearch(
         bbfp, indexOfBB, indexBBSize,
         bfp, indexOfB, indexBSize,
         cs, 
         sfp,indexOfString, indexSSize, 
         query,first, end, false);
        printf("%d\n", result);
    } else if (option == 'a') {
         __lrsearch(
         bbfp, indexOfBB, indexBBSize,
         bfp, indexOfB, indexBSize,
         cs, 
         sfp,indexOfString, indexSSize, 
         query,first, end, true);
    } else if (option == 'n') {
        char *new_query = (char*)calloc(strlen(query)+2, sizeof(char));
        strcat(strcpy(new_query, query), "]");
        __lsearch(
            bbfp, indexOfBB, indexBBSize,
            bfp, indexOfB, indexBSize,
            cs,
            sfp, indexOfString, indexSSize, 
            new_query, &first, &end
        );
        // result = __nsearch(bbfp, bfp, cs, sfp, new_query);
        // printf("query is %s \n", new_query);
        __lnsearch(
         bbfp, indexOfBB, indexBBSize,
         bfp, indexOfB, indexBSize,
         cs, 
         sfp,indexOfString, indexSSize, 
         new_query,first, end);
        free(new_query);
        // printf("NOT implementation\n");
    }
    free(s);
    free(cs);
    // filename
    free(bbf);
    free(bf);
    free(sf);
    free(idxbbf);
    free(idxbf);
    free(idxsf);
    free(idxctbf);
    free(indexOfB);
    free(indexOfBB);
    free(indexOfString);
    fclose(sfp);
    fclose(bfp);
    fclose(bbfp);
}

void 
__lsearch (
    FILE* bbfp, PINDEXOFBLIST indexBB, UINT idxSizeBB,
    FILE* bfp, PINDEXOFBLIST indexB, UINT idxSizeB,
    int* cslist, 
    FILE *sfp, PINDEXOFSTRING idxS, int idxSize, 
    const char* query, UINT *fst, UINT *lst
    ){
    int q_idx = strlen(query)-1;    // backward
    UINT first, last;
    // generate ctable
    // init the loop
    char c = query[q_idx];
    char cNext;
    // last char not exist in string
    if (cslist[c] == -1) {
        *fst = 1;
        *lst = 0;
        return;
    }
    first = selectOneF(bbfp, indexBB, idxSizeBB, cslist[c]+1);
    cNext = nextChar(cslist, c);
    if (cNext == c) {
        last = selectOneF(bbfp, indexBB, idxSizeBB, fgetsize(sfp));
    } else {
        last = selectOneF(bbfp,indexBB, idxSizeBB, cslist[cNext]+1) - 1;
    }
    int count = 0;
    while(first <= last && q_idx >= 1) {
        // loop
        c = query[--q_idx];
        if (cslist[c] == -1) {
            *fst = 1;
            *lst = 0;
            return;
        }
        // calculate the "first" value
        if (first != selectOneF(bfp,indexB, idxSizeBB, rankBVF(bfp, indexB, idxSizeB, first))) {
            UINT num1B = rankBVF(bfp, indexB, idxSizeB, first - 1)-1;
            UINT numSB = rankCVF(c, sfp, idxS, idxSize, num1B);
            first = selectOneF(bbfp, indexBB, idxSizeBB, numSB+cslist[c])+
                (first - selectOneF(bfp, indexB, idxSizeB, rankBVF(bfp, indexB, idxSizeB, first)));
        } else {
            UINT num1B = rankBVF(bfp, indexB, idxSizeB,first - 1)-1;
            UINT numSB = rankCVF(c, sfp, idxS, idxSize, num1B);
            first = selectOneF(bbfp, indexBB, idxSizeBB, numSB+cslist[c]+1);
        }
        if (getValClist(sfp, rankBVF(bfp, indexB, idxSizeB, last)-1)!= c) {
            UINT num1B = rankBVF(bfp,indexB, idxSizeB, last)-1;
            UINT numSB = rankCVF(c, sfp, idxS, idxSize, num1B);
            last = selectOneF(bbfp, indexBB, idxSizeBB, numSB+cslist[c]+1)-1;
        } else {
            UINT num1B = rankBVF(bfp,indexB,idxSizeB, last)-1;
            UINT numSB = rankCVF(c, sfp, idxS, idxSize, num1B);
            last = selectOneF(bbfp, indexBB, idxSizeBB, numSB+cslist[c])+
                (last - selectOneF(bfp, indexB, idxSizeB, rankBVF(bfp,indexB, idxSizeB, last)));
        }
    }
    *fst = first;
    *lst = last;
}

int __lrsearch(
 FILE *bbfp, PINDEXOFBLIST indexBB, UINT indexBBSize,
 FILE *bfp, PINDEXOFBLIST indexB, UINT indexBSize,
 int* cslist,
 FILE *sfp, PINDEXOFSTRING idxS, UINT idxSSize,
 const char* query, UINT first, UINT last, bool bflag
 ) {
    UINT res;
    if (first > last) {
        return 0;
    }
    // for each result
    // backward search the []
    UINT *recordID = (UINT*)calloc(last-first+1, sizeof(UINT));
    UINT recordCounter = 0;
    bool skip = false;
    char* numbers = (char*)calloc(MAX_ID_LEN, sizeof(char));
    for(UINT ctr = first; ctr <= last; ctr++) {
        // back ward search
        UINT id = ctr;
        char ch = getValClist(sfp, rankBVF(bfp, indexB, indexBSize, id)-1);
        // printf("%c\n", ch);
        UINT rankV = rankBVF(bbfp,indexBB,indexBBSize, id);
        bool isId = false;
        while(rankV > cslist[nextChar(cslist, ']')] || 
        rankV <= cslist[']']) {
            // if the char encounter '[' before ']'
            // then means the char an id rather than a searchable string
            if (rankV > cslist['['] && rankV <= cslist[']']) {
                isId = true;
                break;
            }
            // find out the first's corresponding char
            char ch = getValClist(sfp, rankBVF(bfp, indexB, indexBSize, id)-1) ;
            // printf("%c \n", ch);
            // map to previous char in B prime
            UINT num1B = rankBVF(bfp,indexB, indexBSize, id);
            UINT numSB = rankCVF(ch, sfp, idxS, idxSSize, num1B);
            UINT prevI = selectOneF(bbfp,indexBB, indexBBSize, numSB+cslist[ch]) 
                + (id - selectOneF(bfp, indexB,indexBSize, rankBVF(bfp,indexB, indexBSize, id)));
            id = prevI;
            // if (ctr != first && id == ctr - 1) {
            //     skip = true;
            //     break;
            // }
            rankV = rankBVF(bbfp,indexBB, indexBBSize, id);
        }
        // if (skip) {
        //     continue;
        // }
        if (isId) {
            continue;
        }
        // id is in the range of "[" to "]"
        rankV = rankBVF(bbfp, indexBB, indexBBSize, id);
        while (rankV <= cslist['[']
        ||rankV > cslist[']']) {
            char ch = getValClist(sfp, rankBVF(bfp, indexB, indexBSize, id)-1);
            // printf("num get %c \n", ch);
            char suffix[2];
            suffix[0] = ch;
            suffix[1] = '\0';
            if (ch != '[') {
                strcat(numbers, suffix);
            }
            UINT num1B = rankBVF(bfp,indexB, indexBSize, id);
            UINT numSB = rankCVF(ch, sfp, idxS, idxSSize, num1B);
            UINT prevI = selectOneF(bbfp,indexBB, indexBBSize, numSB+cslist[ch]) 
                + (id - selectOneF(bfp, indexB, indexBSize,rankBVF(bfp, indexB, indexBSize, id)));
            id = prevI;
            rankV = rankBVF(bbfp, indexBB, indexBBSize, id);
        }
        // printf("%s\n", numbers);
        strrev(numbers);
        // id should map to "[" in bblist
        recordID[recordCounter] = atoi(numbers);
        // printf("%d\n", recordID[recordCounter]);
        recordCounter++;
        // free(numbers);
        memset(numbers, 0, MAX_ID_LEN);
    }
    free(numbers);
    res = 0;
    // use lib sort
    qsort(recordID, recordCounter, sizeof(*recordID), comp);
    for(int i = 0; i < recordCounter; i++) {
        if (i > 0 && recordID[i] == recordID[i-1]) {
            continue;
        } else if (bflag){
            printf("[%d]\n", recordID[i]);
        } else {
            res ++;
        }
    }
    // printf("%d\n", recordCounter);
    free(recordID);
    return res;
}


int __lnsearch(
 FILE *bbfp, PINDEXOFBLIST indexBB, UINT indexBBSize,
 FILE *bfp, PINDEXOFBLIST indexB, UINT indexBSize,
 int* cslist,
 FILE *sfp, PINDEXOFSTRING idxS, UINT idxSSize,
 const char* query, UINT first, UINT last
) {
    // int first, last, res;
    if (first > last) {
        return 0;
    }
    // char *clist = ppacks->s;
    // PLinkedList *sTable;
    // buildSTable(clist, *ssize, &sTable);
    char *string = (char*)calloc(5000, sizeof(char));
    int string_counter = 0;
    // backward search check if previous char is '['
    for (UINT ctr = first; ctr <= last; ctr++) {
        UINT id = ctr;
        if (getValClist(sfp, rankBVF(bfp, indexB, indexBSize, id)-1) != '[') {
            continue;
        }
        // forward search until encounter the '['
        UINT rankV_in_BB = rankBVF(bbfp, indexBB, indexBBSize, id);
        char ch;
        // find start char in cs
        for(UINT cs_i = 0; cs_i < ASCIILEN; cs_i++) {
            if (cslist[cs_i]<rankV_in_BB && cslist[nextChar(cslist, cs_i)]>=rankV_in_BB) {
                ch = cs_i;
                break;
            }
        }
        while (ch != '[') {
            // keep going on looking for next char
            // find which ch in S
            string[string_counter] = ch; 
            rankV_in_BB = rankBVF(bbfp, indexBB,indexBBSize, id);   //7
            UINT count_ch_in_cs = rankV_in_BB - cslist[ch] - 1; // 7-6-1=0
            UINT pos_ch_in_s = selectSF(sfp, idxS, idxSSize, ch, count_ch_in_cs+1); // 6
            UINT count_ch_in_B = selectOneF(bfp,indexB, indexBSize, pos_ch_in_s+1)
                + (id - selectOneF(bbfp, indexBB, indexBBSize, rankV_in_BB));
            rankV_in_BB = rankBVF(bbfp, indexBB, indexBBSize, count_ch_in_B);    // 6
            for (int cs_i = 0; cs_i < ASCIILEN; cs_i++) {
                if (nextChar(cslist, cs_i) != cs_i && cslist[cs_i]<rankV_in_BB
                &&cslist[nextChar(cslist, cs_i)]>=rankV_in_BB) {
                    ch = cs_i;
                    break;
                } else if (nextChar(cslist, cs_i) == cs_i) {
                    ch = cs_i;
                    break;
                }
            }
            id = count_ch_in_B;
            string_counter++;
        }
    }
    string_counter = 0;
    if (strlen(string) == 0) {
        return 0;
    }
    while(string[string_counter] != ']') {
        string_counter++;
    }
    for(int i = string_counter+1; string[i]!='\0'&&i < 5000; i++) {
        printf("%c", string[i]);
    }
    printf("\n");
    return 0;
}