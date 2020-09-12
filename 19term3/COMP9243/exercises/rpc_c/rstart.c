#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>
#define BUFFER_SIZE 100
//   Usage: rstart [OPTION]... EXECUTABLE-FILE NODE-OPTION...
//   -H HOSTFILE list of host names
//   -h          this usage message
//   -n N        fork N node processes
//   -v          print version information

// Forks N copies (one copy if -n not given) of
// EXECUTABLE-FILE.  The NODE-OPTIONs are passed as arguments to the node
// processes.  The hosts on which node processes are started are given in
// HOSTFILE, which defaults to `hosts'.  If the file does not exist,
// `localhost' is used.

// global arguments recording command line arguments
struct globalArgs_t {
    char *version;
    int node_nums;
    char *exec_file;
    char *host_file;
} globalArgs;

typedef struct process_id_t {
    char* hostname;
    int pid;
}PID, *PPID;

void display_usage() {
    printf("Usage: rstart [OPTION]... EXECUTABLE-FILE NODE-OPTION...\n");
    printf("-h          this usage message\n");
    printf("-n N        fork N node processes\n");
    printf("-v          print version information\n");
}

void create_process(int n, char*, char*, PPID);
void create_rprocess(char*, char*, char *, PID); 
int main(int argc, char* argv[]) {
    // char *version = "0.0.1";
    // record how many ndoe process
    // int node_nums = -1;
    // record the number of hosts running process
    int host_nums = -1;
    char* hosts[BUFFER_SIZE];
    char opt;
    PPID process_table;
    // char *exec_file, *node_args;
    globalArgs.version = "0.0.1";
    globalArgs.node_nums = 1;
    globalArgs.exec_file = NULL;
    globalArgs.host_file = NULL;
    if (argc <= 1) {
        return EXIT_FAILURE;
    }
    // read arguments
    while ((opt = getopt(argc, argv, "H:n:vh")) != -1) {
        switch(opt) {
            case 'H':
                globalArgs.host_file = optarg;
                break;
            case 'n':
                globalArgs.node_nums = atoi(optarg);
                break;
            case 'v':
                printf("version: %s\n",globalArgs.version);
                break;
            case 'h':
                display_usage();
                break;
            default:    // unexpected to be there
                break;
        }
    }
    globalArgs.exec_file = *(argv+optind);
    if (globalArgs.host_file) {
        FILE* fptr = fopen(globalArgs.host_file, "r");
        if (fptr!= NULL) {
            host_nums = 0;
            fseek(fptr, 0, SEEK_END);
            // the number of char in hostfile
            long read_size = ftell(fptr);
            // buffer holds the content in hostfile
            char *buffer = (char *)malloc(sizeof(char)*read_size);
            rewind(fptr);
            fread(buffer, 1, read_size, fptr);
            char *hostname;
            int start_line = 0;
            for(int i = 0; i < read_size; i++) {
                if (buffer[i] == '\n') {
                    hostname = (char*)calloc(sizeof(char)*(i-start_line+1), sizeof(char));
                    for(int j = start_line; j < i;j++) {
                        hostname[j-start_line] = buffer[j];
                    }
                    hosts[host_nums++] = hostname;
                    start_line = i+1;
                    printf("remote host :%s\n", hostname);
                    // free(hostname);
                }
            }
            if (start_line < read_size-1) {
                hostname = (char*)calloc(sizeof(char)*(read_size-start_line), sizeof(char));
                for(int i = start_line; i < read_size; i++) {
                    hostname[i-start_line] = buffer[i];
                }
                hosts[host_nums++] = hostname;
                printf("remote host :%s\n", hostname);
                // free(hostname);
            }
            free(buffer);
        } else {
            printf("cannot find the file\n");
            return EXIT_FAILURE;
        }
        fclose(fptr);
    }
    // call ssh to start remote process
    if (host_nums == -1) {
        // TODO: localhost
        char *args_buffer = (char*) calloc(BUFFER_SIZE, sizeof(char));
        for(int i = argc-optind+1; i < argc; i++) {
            // combine arguments as a string
            strcat(strcat(args_buffer, argv[i])," ");
        }
        process_table = (PPID) calloc(globalArgs.node_nums, sizeof(PID));
        create_process(globalArgs.node_nums, globalArgs.exec_file, args_buffer, process_table);
        free(args_buffer);
    } else {
        char *args_buffer = (char*) calloc(BUFFER_SIZE, sizeof(char));
        for(int i = optind+1; i < argc; i++) {
            // combine arguments as a string
            strcat(strcat(args_buffer, argv[i])," ");
        }
        process_table = (PPID) calloc(globalArgs.node_nums, sizeof(PID));
        for(int i = 0; i < globalArgs.node_nums; i++) {
            printf("create remote process\n");
            create_rprocess(hosts[i%host_nums], globalArgs.exec_file, args_buffer, process_table[i]);
        }
    }
    free(process_table);
    return EXIT_SUCCESS;
}

// create remote process 
void create_rprocess(char* hostname, char* file, char *args, PID process_info) {
    int pid;
    pid = fork();
    if (!pid) {
        char ssh_cmd[BUFFER_SIZE];
        memset(ssh_cmd, 0, BUFFER_SIZE);
        strcat(ssh_cmd, "ssh ");
        strcat(ssh_cmd, hostname);
        // ssh to remote host
        FILE *rprocess = popen(ssh_cmd, "w");
        if (!rprocess)  exit(EXIT_FAILURE);
        memset(ssh_cmd, 0, BUFFER_SIZE);
        strcat(strcat(ssh_cmd, file), " ");
        strcat(ssh_cmd, args);
        strcat(ssh_cmd, " &");
        printf("create a ssh\n");
        printf("%s\n", ssh_cmd);
        fprintf(rprocess, ssh_cmd);
        fclose(rprocess);
        exit(EXIT_SUCCESS);
    } else {
        process_info.hostname = hostname;
        process_info.pid = pid;
    }
}
void create_process(int node_nums, char* filename, char* args, PPID process_info) {
    int pid;
    for(int i = 0 ; i < node_nums; i++) {
        pid = fork();
        if (!pid) {
            // child process
            execlp(filename, filename, args, 0);
        } else {
            process_info[i].hostname = "localhost";
            process_info[i].pid = pid;
        }
    }
}