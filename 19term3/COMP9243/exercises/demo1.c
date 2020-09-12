#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
int main() {
    char **ssh_args = (char**) calloc(3, sizeof(char*));
    ssh_args[0] = "ssh";
    ssh_args[1] = "vina07";
    ssh_args[2] = "/import/cage/3/z5167157/COMP9243/exercises/demo2";
    execv("/usr/bin/ssh", ssh_args);
    printf("hhh\n");
    return 0;
}