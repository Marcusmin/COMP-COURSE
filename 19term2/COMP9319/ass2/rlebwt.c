// "access" function for checking existence
#include <unistd.h>
// file system use
#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include "search.h"
struct stat info;
// using namespace std;
#define BBSUFFIXLEN 4
int main(int argc, char* argv[]) {
    //rlebwt -X FILENAME INDEX_FOLDER QUERY_STRING
    char* option;
    char* filename;
    char* index_folder;
    char* query_string;
    int res;
    if (argc == 5) {
        option = argv[1];
        filename = argv[2];
        index_folder = argv[3];
        query_string = argv[4];
        bool is_bb_exist = false;
        char* bbfile = (char*)calloc(strlen(filename)+BBSUFFIXLEN, sizeof(char));
        strcat(strcpy(bbfile,filename), ".bb");
        // strcat(bbfile, ".bb");
        if (stat(bbfile, &info) == 0) {
            // *.bb file exists here
            is_bb_exist = true;
        } else {
            // *.bb file not exist here
        }
        // TODO check if index folder exists
        if (stat(index_folder, &info) != 0) {
            // cannot get access to dir
            // create a dir with same name
            int dirError = mkdir(argv[3], S_IRWXG|S_IRWXO|S_IRWXU);
            if (dirError == -1) {
                exit(1);
            }
        } else if (S_ISDIR(info.st_mode)) {
            // there is a dir with the same name
        } else {
            // create a dir
            int dirError = mkdir(argv[3], S_IRWXG|S_IRWXO|S_IRWXU);
            if (dirError == -1) {
                exit(1);    
            }
        }
        // printf("%c\n", option[1]);
        search(filename, index_folder, query_string, option[1], is_bb_exist);
        free(bbfile);
    }

}