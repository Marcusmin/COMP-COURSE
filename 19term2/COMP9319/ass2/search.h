#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "select.h"
#include "wfile.h"
#include "ctable.h"
#include "rfile.h"
void search(const char* filename, const char* folder, const char* query, const char option, bool flag);
void lfsearch(const char* filename, const char* folder, const char* query, const char option, bool flag);

void __lsearch(
 FILE* bbfp, PINDEXOFBLIST, UINT,
 FILE* bfp, PINDEXOFBLIST, UINT,
 int* cslist,
 FILE *sfp, PINDEXOFSTRING index, int idxSize,
 const char* query, UINT *fst, UINT *lst
 );

int __lrsearch(
 FILE *bbfp, PINDEXOFBLIST, UINT,
 FILE *bfp, PINDEXOFBLIST, UINT,
 int* cslist, 
 FILE *sfp, PINDEXOFSTRING, UINT,
 const char* query, UINT first, UINT last,
 bool bflag); 

int __lnsearch(
 FILE *bbfp, PINDEXOFBLIST indexBB, UINT indexBBSize,
 FILE *bfp, PINDEXOFBLIST indexB, UINT indexBSize,
 int* cslist,
 FILE *sfp, PINDEXOFSTRING idxS, UINT idxSSize,
 const char* query, UINT first, UINT last
);