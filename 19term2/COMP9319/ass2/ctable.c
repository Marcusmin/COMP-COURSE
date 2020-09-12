#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "htools.h"
#include "ctable.h"
#include "rfile.h"
/* if char not exist in cs table, fill with -1
 * @param s - s string read from *.s file
 * @param fs - a pointer point to created c table
 * @param cs - a 2d array
 * @param length - the length of s
 * @return bool - sucess or not
 */
bool createCtable(FILE* fp, int** cs) {
    // a sorted s string
    // cs table
    *cs = (int *)calloc(ASCIILEN, sizeof(int));
    fpos_t curPos;
    fgetpos(fp, &curPos);
    fseek(fp, 0L, SEEK_SET);
    char c = 0;
    // count sort the s char array for only ascii value in there
    int *chars = (int *)calloc(ASCIILEN, sizeof(int));
    for(int i = 0;(c=fgetc(fp))!=EOF; i++) {
        chars[c] ++;
    }
    fsetpos(fp, &curPos);
    int csc = 0;    // counter for build cs array
    // output the count result
    // scan the buckets
    for(int ch = 0; ch < ASCIILEN; ch++) {
        if (chars[ch] != 0) {
            (*cs)[ch] = csc;
            csc += chars[ch];
        } else {
            (*cs)[ch] = -1;
        }
    }
    free(chars);

    return true;
}
