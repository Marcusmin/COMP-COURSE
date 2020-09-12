/** TODO:
 * 1. set up connection
 * */

// gethostname
#define _DEFAULT_SOURCE
#define DEFAULT_PORT 0
#define DEFAULT_PROTOCAL 0
#define CHAR_BUFFER_SIZE 128
#define PORT_CHAR_SIZE 12
#define NID_CHAR_SIZE 12
#define INTERNAL_ARGC 7 // ssh + exe + host + ip + port + nodes + nid
#define LINT_LEN 11 // 10 + 1
#define MSG_LEN 21
#define PTR_LEN 21
// time out
#define TIMEOUT 400
// signal for dsm && remote process communication

// operation codes
// #include "smc.h"
#define BARRIER 1
#define EXIT 2
#define MALLOC 3
#define BCAST 4
#define CLAIM_OWNER 5
#define ACK 8
#define INIT 6
#define READ 7
#define WRITE 9
#define COLLECT 10
#define DISABLE 11
#define START_ADDR 0x26998000
// operation codes

// directive for sm_malloc
#define PAGE_NUM 0xffff
// required headers
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <getopt.h>
#include <sys/socket.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "sm.h"
#include <sys/mman.h>
void display_usage() {
    printf("Usage: dsm [OPTION]... EXECUTABLE-FILE NODE-OPTION...\n");
    printf("-H HOSTFILE list of host names\n");
    printf("-h          this usage message");
    printf("-l LOGFILE  log each significant allocator action to LOGFILE\n");
    printf("-n N        fork N node processes\n");
    printf("-v          print version information\n");
    printf("\n");
}

void display_version() {
    printf("version: 0.0.1\n");
}

// processor n, page p & q
void log_malloc(FILE* fptr,int n, int p, int q) {
    if (fptr != NULL) {
        fprintf(fptr, "#%d: allocated %d - %d\n", n, p, q);
    }
}

void log_readfault(FILE* fptr, int n, int p) {
    if (fptr != NULL) {
        fprintf(fptr, "#%d: read fault @ %d\n",n, p);
    }
}

void log_writefault(FILE* fptr, int n, int p) {
    if (fptr != NULL) {
        fprintf(fptr, "#%d: write fault @ %d\n",n, p);
    }
}

void log_releasingownership(FILE* fptr, int n, int p) {
    if (fptr != NULL) {
        fprintf(fptr, "#%d: releasing ownership of %d\n", n, p);
    }
}

void log_invalidate(FILE* fptr, int n, int p) {
    if (fptr != NULL) {
        fprintf(fptr, "#%d: invalidate %d\n", n, p);
    }
}

void log_recvRpermission(FILE* fptr, int n, int p) {
    if (fptr != NULL) {
        fprintf(fptr, "#%d: receiving read permission for %d\n", n, p);
    }
}

void log_recvownership(FILE* fptr, int n, int p) {
    if (fptr != NULL) {
        fprintf(fptr, "#%d: receiving ownership of %d\n", n, p);
    }
}

void debug(double* ptr, int len) {
    printf("dsm: first %d elements\n", len);
    for(int i = 0; i < len; i++) {
        printf("%f\t", ptr[i]);
    }
    printf("\n");
    for(int i = 0; i < len; i++) {
        printf("%f\t", ptr[i+len]);
    }
    printf("\n");
    for(int i = 0; i < len; i++) {
        printf("%f\t", ptr[i+2*len]);
    }
    printf("\n");
}

// prepare for select
// assign hosts to hosts arrary and assign the number of hosts to size
void get_hosts_from_file(const char* filename, char*** hosts, int* size, char **str) {
    long read_size = 0;
    FILE *fptr = fopen(filename, "r");
    if (fptr != NULL) {
        // count the number of chars
        fseek(fptr, 0, SEEK_END);
        read_size = ftell(fptr);
        char* buffer = (char*) calloc(read_size+1, sizeof(char));
        rewind(fptr);
        fread(buffer, sizeof(char), read_size, fptr);
        char *host = NULL;
        int hosts_capacity = 1;
        // count the number of hosts
        for(int i = 0; i < read_size; i++) {
            if (buffer[i] == '\n') {
                hosts_capacity++;
            }
        }
        int idx = 0;
        (*hosts) = (char**) calloc(hosts_capacity, sizeof(char*));
        host = strtok(buffer, "\n");
        while(host && idx < hosts_capacity) {
            (*hosts)[idx] = host;
            idx++;
            host = strtok(0, "\n");
        }
        *size = hosts_capacity;
        (*str) = buffer;
        // free(buffer);
        fclose(fptr);
    }
}

typedef struct m_page_t {
    int nid;
    int next;
} m_page;

typedef struct Argument_t {
    int nodes;  // the number of nodes;
    char* hostfile; // where store the hosts
    char* logfile;  // where store the log
    char** node_argv;   // node options
    int node_argc;
    char* exec_file;    // executable files
} Argument;

typedef struct Node_Record_t {
    char node_ip[16];
    int node_port;
    int node_socket;
    int node_async_sock;
    int node_write_sock;
    int nid;
} Node_Record;

void get_fdset(Node_Record* nodes, fd_set *set, int node_len, int*maxfd) {
    // clean
    FD_ZERO(set);
    *maxfd = -1;
    // fill set
    for(int i = 0; i < node_len; i++) {
        if ((*maxfd) <= nodes[i].node_socket) {
            (*maxfd) = nodes[i].node_socket;
        }
        FD_SET(nodes[i].node_socket, set);
    }
}

void init_arguments(Argument* args) {
    args->exec_file = NULL;
    args->node_argv = NULL;
    args->hostfile = NULL;
    args->logfile = NULL;
    args->exec_file = NULL;
    args->nodes = 0;
    args->node_argc = 0;
}

void delete_all_nodes(Node_Record *node_records, int* size) {
    for(int i = 0; i < *size; i++) {
        close(node_records[i].node_socket);
    }
    *size = 0;
    free(node_records);
}

int getpagenum(void* addr, void *start) {
    intptr_t offset = abs(addr - start);
    return offset/getpagesize();    
}

void sent2node(int socket, void* buffer, int len, int flag) {
    int status = send(socket, buffer, len, flag);
    if (status == -1) {
        perror("dsm:send to node");
        exit(1);
    } else if (status == 0) {
        exit(1);
    }
}

void recvfromnode(int socket, void* buffer, int len, int flag) {
    int status = recv(socket, buffer, len, flag);
    if (status == -1) {
        perror("dsm:send to node");
        exit(1);
    } else if (status == 0) {
        exit(1);
    }
}
void int2char(char* buffer, int len, long value) {
    memset(buffer, 0, len);
    sprintf(buffer, "%d", value);
}

// delete the node from node records
void delete_node(Node_Record **node_records, int nidx, int* nodectr) {
    Node_Record *temp = *node_records;
    // close the socket
    int status = close((*node_records)[nidx].node_socket);
    if (status < 0) {
        perror("close socket");
    }
    for(int i = nidx + 1; i < *nodectr; i++) {
        (*node_records)[i-1] = (*node_records)[i];
    }
    // if there is no node running
    if (*nodectr - 1 <= 0) {
        free(*node_records);
    } else {
        (*node_records) = (Node_Record *) calloc(*nodectr-1, sizeof(Node_Record));
        for(int i = 0; i < (*nodectr-1); i++) {
            (*node_records)[i] = temp[i];
        }
        free(temp);
    }
    (*nodectr)--;
}

int talk_to_node(Node_Record node) {
    char buffer[MSG_LEN];
    memset(buffer, 0, MSG_LEN);
    int nbyte = recv(node.node_socket, buffer, MSG_LEN, MSG_WAITALL);
    int code = atoi(buffer);
    if (nbyte > 0) {
        switch (code) {
            case EXIT:
                return EXIT;
            case BARRIER:
                return BARRIER;
            case MALLOC:
                return MALLOC;
            case CLAIM_OWNER:
                return CLAIM_OWNER;
            case BCAST:
                return BCAST;
            case INIT:
                return INIT;
            case READ:
                return READ;
            case WRITE:
                return WRITE;
            default:
                return 0;
        }
    } else {
        // printf("dsm: lost node %d connection\n", node.nid);
        return -1;
    }
}

int main(int argc, char* argv[]) {
    // fd set for store sockets
    fd_set node_socks;    // copy of node sockets
    int fdmax = -1;
    // dsm arguments
    Argument dsm_args;
    // init arguments with 0
    init_arguments(&dsm_args);
    // record the page info
    m_page page_map[PAGE_NUM];
    int pre_page = 0;
    // starting addr
    void *starting_addr;
    // require arguments for ssh
    char** hostnames = NULL;
    int hostcount = 0;
    // holds options
    char opt = '\0';
    while ((opt=getopt(argc, argv, "H:l:n:vh"))!=-1) {
        switch(opt) {
            case 'H':
                dsm_args.hostfile = optarg;
                break;
            case 'l':
                dsm_args.logfile = optarg;
                break;
            case 'n':
                dsm_args.nodes = atoi(optarg);
                break;
            case 'v':
                display_version();
                return 0;
            case 'h':
                display_usage();
                return 0;
            default:
                return -1;
        }
    }
    // rest of arguments should be executable files && node options
    if (optind >= argc) {
        // no executable file
        return -1;
    }
    // assign executable filename
    dsm_args.exec_file = *(argv+optind);
    dsm_args.node_argc = argc - (++optind);
    // malloc memory for node option if there is
    if (dsm_args.node_argc > 0) {
        dsm_args.node_argv = (char**) calloc(dsm_args.node_argc, sizeof(char*));
        for(int i = 0; i < dsm_args.node_argc; i++) {
            dsm_args.node_argv[i] = argv[optind];
            optind++;
        }
    }
    // get hosts
    char *buffer_for_hostfile;
    if (dsm_args.hostfile != NULL) {
        get_hosts_from_file(dsm_args.hostfile, &hostnames, &hostcount, &buffer_for_hostfile);
    } else {
        // use localhost if no host file specified
        hostnames = (char**) calloc(1, sizeof(char));
        hostnames[0] = "localhost";
        hostcount = 1;
    }
    // find ip addr
    char root_host[CHAR_BUFFER_SIZE];
    char *root_ip = (char*) calloc(16, sizeof(char));
    gethostname(root_host, sizeof(root_host));
    struct hostent* he = gethostbyname(root_host);
    if (he == NULL) {
        // get hostname fail
        perror("Root host name:");
        return -1;
    }
    struct in_addr **addr_list;
    addr_list = (struct in_addr **)he->h_addr_list;
    if (addr_list[0]!=NULL) {
        strcpy(root_ip, inet_ntoa(*addr_list[0]));
    } else {
        // get root ip fail
        perror("Root host ip:");
        return -1;
    }
    // buid up tcp socket
    struct addrinfo hints;
    memset(&hints, 0, sizeof(hints));
    int root_sock = socket(AF_INET, SOCK_STREAM, DEFAULT_PROTOCAL);
    if (root_sock == -1) {
        // set up socket fail
        perror("Socket");
        return -1;
    }
    // bind to ip && port
    struct sockaddr_in servaddr;
    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = inet_addr(root_ip);
    servaddr.sin_port = DEFAULT_PORT;
    if (bind(root_sock, (struct sockaddr *)&servaddr, sizeof(servaddr)) == -1) {
        // bind fail
        perror("Bind");
        return -1;
    }
    // listening
    if (listen(root_sock, dsm_args.nodes+10) == -1) {
        perror("Listen");
        return -1;
    }
    // get port number from socket
    // clear structure
    memset(&servaddr, 0, sizeof(servaddr));
    socklen_t len = sizeof(servaddr);
    int status = getsockname(root_sock, (struct sockaddr*)&servaddr, &len);
    if (status) {
        perror("get sockename fail");
        return -1;
    }
    // combine node arguments && port number && ip address
    // ssh + host + exe + ip + port + nodes + nid
    int ssh_argc = dsm_args.node_argc+INTERNAL_ARGC; 
    if (dsm_args.nodes < 1) {
        dsm_args.nodes = 1;
    }
    char **ssh_argv = (char**)calloc(ssh_argc+1, sizeof(char*));
    // root port atoi
    char root_port_str[PORT_CHAR_SIZE];
    memset(root_port_str, 0, PORT_CHAR_SIZE);
    sprintf(root_port_str, "%d", ntohs(servaddr.sin_port));
    char nodes_str[MSG_LEN];
    memset(nodes_str, 0, MSG_LEN);
    sprintf(nodes_str, "%d", dsm_args.nodes);
    // fill ssh 's argument
    for(int i = 0; i < INTERNAL_ARGC; i++) {
        switch (i) {
            case 0: // ssh
                ssh_argv[i] = "/usr/bin/ssh";
                break;
            case 1: // host, assign later
                break;
            case 2: // executable
                ssh_argv[i] = dsm_args.exec_file;
                break;
            case 3: // ip
                ssh_argv[i] = root_ip;
                break;
            case 4: // port
                ssh_argv[i] = root_port_str;
                break;
            case 5: // nodes
                ssh_argv[i] = nodes_str;
                break;
            case 6: // nid
                break;
            default:
                break;
        }
    }
    for(int i = INTERNAL_ARGC; i < ssh_argc; i++) {
        ssh_argv[i] = dsm_args.node_argv[i-INTERNAL_ARGC];
    }
    // ssh init a remote process
    // fork process to execute ssh
    // a string store the nid
    char nid[NID_CHAR_SIZE];
    memset(nid, 0, NID_CHAR_SIZE);
    starting_addr = mmap((void*)START_ADDR, getpagesize()*PAGE_NUM, PROT_NONE, MAP_ANONYMOUS|MAP_PRIVATE, -1, 0);
    // record the tcp connection info
    Node_Record *node_records = (Node_Record *) calloc(dsm_args.nodes, sizeof(Node_Record));
    for(int i = 0; i < dsm_args.nodes; i++) {
        int host_idx = i % hostcount;
        char* the_host = hostnames[host_idx];
        int pid = fork();
        // if in root process
        if (pid == 0) { // child process
            // execute ssh to remote host
            // send node argument | allocator's ip && port | node number && node id
            // works as `ssh host executable ip port node nid options`
            ssh_argv[1] = the_host;
            sprintf(nid, "%d", i);
            ssh_argv[6] = nid;
            execv("/usr/bin/ssh", ssh_argv);
        } else if (pid == -1) {  // deal with fork error
            perror("fork fail");
        } else {    
            // should wait child process return expected result
            // attempt to receive tcp connection
            struct sockaddr_in nodeaddr;
            memset(&nodeaddr, 0, sizeof(struct sockaddr_in));
            int node_addr_size = sizeof(nodeaddr);
            int node_socket = accept(root_sock, (struct sockaddr *)&nodeaddr, &node_addr_size);
            node_records[i].node_socket = node_socket;
            strcpy(node_records[i].node_ip, inet_ntoa(nodeaddr.sin_addr));
            node_records[i].node_port = ntohs(nodeaddr.sin_port);
            node_records[i].nid = i;
            // store the socket for latter steps
            // init mmap config
            char addr[PTR_LEN];
            memset(addr, 0, PTR_LEN);
            sprintf(addr, "%d", starting_addr);
            send(node_socket, addr, PTR_LEN, 0);
            node_records[i].node_async_sock = accept(root_sock, (struct sockaddr *)&nodeaddr, &node_addr_size);
            node_records[i].node_write_sock = accept(root_sock, (struct sockaddr *)&nodeaddr, &node_addr_size);
        }
    }
    // NOTE: didn't handle ssh error && block tcp connection
    int barrier = 0;
    // wait for incoming communication
    struct timeval timeout;
    timeout.tv_sec = TIMEOUT;
    timeout.tv_usec = 0 ;
    // open log file
    intptr_t cast_val = -1;
    FILE *fptr = NULL;
    if (dsm_args.logfile != NULL) {
        fptr = fopen(dsm_args.logfile, "wr");
    }
    if (fptr != NULL) {
        fprintf(fptr, "_= %d node processes =_\n", dsm_args.nodes);
    }
    while (1) {
        get_fdset(node_records, &node_socks, dsm_args.nodes, &fdmax);
        // don't set time interval
        int slct_res = select(fdmax+1, &node_socks, NULL, NULL, &timeout);
        if (slct_res == -1) {
            perror("select");
            return -1;
        } else if (slct_res) {
            for(int i = 0; i < dsm_args.nodes; i++) {
                if (FD_ISSET(node_records[i].node_socket, &node_socks)) {
                    // talk with selected sock
                    int status = talk_to_node(node_records[i]);
                    switch (status) {
                        case EXIT: {
                            // exit the particular node
                            // close the socket
                            // wait other node exit
                            delete_node(&node_records, i, &(dsm_args.nodes));
                            break;
                        }
                        case BARRIER: {
                            barrier++;
                            // suspend until the count
                            if (barrier == dsm_args.nodes) {
                                // all node are barried
                                // let them move on
                                char buffer[MSG_LEN];
                                int2char(buffer, MSG_LEN, MSG_LEN);
                                barrier = 0;
                                for(int j = 0; j < dsm_args.nodes; j++) {
                                    send(node_records[j].node_socket, buffer, MSG_LEN, 0);
                                    recvfromnode(node_records[j].node_socket, buffer, MSG_LEN, MSG_WAITALL);
                                }
                            }
                            break;
                        }
                        case BCAST: {
                            // invalidate or update pagemap
                            // wait valid value 
                            barrier++;
                            if (barrier == dsm_args.nodes) {
                                char page[PTR_LEN];
                                memset(page, 0, PTR_LEN);
                                if (cast_val >= 0) {
                                    sprintf(page, "%d", cast_val);
                                } else {
                                    exit(1);
                                }
                                barrier = 0;
                                for(int j = 0; j < dsm_args.nodes; j++) {
                                    send(node_records[j].node_socket, page, PTR_LEN, 0);
                                }
                            }
                            break;
                        }
                        case MALLOC: {
                            char buffer[MSG_LEN];
                            sprintf(buffer, "%d", ACK);
                            send(node_records[i].node_socket, buffer, MSG_LEN, 0);
                            memset(buffer, 0, MSG_LEN);
                            int nbytes = recv(node_records[i].node_socket, buffer, MSG_LEN, MSG_WAITALL);
                            if (nbytes == 0) {
                                exit(1);
                            } else if (nbytes == -1) {
                                perror("dsm recv fail");
                            }
                            int page_num = atoi(buffer);
                            sprintf(buffer, "%d", pre_page);
                            for(int j = 0; j < page_num; j++) {
                                if (j == 0) {
                                    page_map[pre_page].nid = i;
                                    page_map[pre_page++].next = 1;
                                } else {
                                    page_map[pre_page++].nid = i;
                                }
                            }
                            // NOTE: BUG here// maybe fixed
                            send(node_records[i].node_socket, buffer, MSG_LEN, 0);
                            log_malloc(fptr, i, pre_page - page_num, pre_page - 1);
                            break;
                        }
                        case CLAIM_OWNER: {
                            // assign the ptr to that value
                            char buffer[MSG_LEN];
                            char ptr[PTR_LEN];
                            memset(ptr, 0, PTR_LEN);
                            memset(buffer, 0, MSG_LEN);
                            sprintf(buffer, "%d", ACK);
                            send(node_records[i].node_socket, buffer, PTR_LEN, 0);
                            int nbytes = recv(node_records[i].node_socket, ptr, PTR_LEN, MSG_WAITALL);
                            if (nbytes == 0) {
                                printf("dsm: disconnection during claim owner\n");
                                // TODO: exit properly
                                exit(1);
                            }
                            cast_val = atol(ptr);
                            sprintf(buffer, "%d", ACK);
                            for(int j = 0; j < dsm_args.nodes; j++) {
                                send(node_records[j].node_socket, buffer, MSG_LEN, 0);
                            }
                            break;
                        }
                        case READ : {
                            // allocate the page from owner
                            char page[PTR_LEN];
                            char cmd[MSG_LEN];
                            char *page_content = calloc(getpagesize(), 1);
                            memset(page, 0, PTR_LEN);
                            int2char(cmd, MSG_LEN, ACK);
                            send(node_records[i].node_socket, cmd, MSG_LEN, 0);
                            // receive addr to read
                            recv(node_records[i].node_socket, page, PTR_LEN, MSG_WAITALL);
                            void* ptr = (void*)atol(page);
                            // log read fault
                            log_readfault(fptr, i, getpagenum(ptr, starting_addr));
                            int page_count = getpagenum(ptr, starting_addr);
                            // find owner of the page
                            int owner = page_map[page_count].nid;
                            int2char(cmd, MSG_LEN, COLLECT);
                            // send command collect
                            int status = send(node_records[owner].node_async_sock, page, PTR_LEN, 0);
                            if (status == -1) {
                                perror("rev fail");
                                exit(1);
                            } else if (status == 0) {
                                fprintf(stderr, "dsm: disconnection during sending request to owner\n");
                                exit(1);
                            }
                            status = recv(node_records[owner].node_async_sock, page_content, getpagesize(), MSG_WAITALL);
                            if (status == -1) {
                                perror("recv fail");
                                exit(1);
                            } else if (status == 0) {
                                fprintf(stderr, "dsm: disconnection during reading page from owner\n");
                                exit(1);
                            }
                            // owner release ownership
                            log_releasingownership(fptr, owner, page_count);
                            send(node_records[i].node_socket, page_content, getpagesize(), 0);
                            recvfromnode(node_records[i].node_socket, page, PTR_LEN, MSG_WAITALL);
                            // send page to client
                            log_recvRpermission(fptr, i, page_count);
                            free(page_content);
                            break;
                        }
                        case WRITE: {
                            // change owner of the page
                            // receive the page
                            char page[PTR_LEN];
                            char cmd[MSG_LEN];
                            int2char(cmd, MSG_LEN, ACK);
                            memset(page, 0, PTR_LEN);
                            // send ack to node
                            sent2node(node_records[i].node_socket, cmd, MSG_LEN, 0);
                            int status = recv(node_records[i].node_socket, page, PTR_LEN, MSG_WAITALL);
                            if (status == 0) {
                                fprintf(stderr, "disconnection\n");
                                exit(1);
                            } else if (status == -1) {
                                perror("disconnection");
                                exit(1);
                            }
                            void* ptr = (void*)atol(page);
                            int page_count = (ptr - starting_addr)/getpagesize();
                            // log write fault
                            log_writefault(fptr, i, page_count);
                            // find owner
                            int owner = page_map[page_count].nid;
                            void* page_content = calloc(getpagesize(), 1);
                            void* drop_page_content = calloc(getpagesize(), 1);
                            // change the owner
                            page_map[page_count].nid = i;
                            // note the other node that the page is invalid
                            for(int j = 0; j < dsm_args.nodes; j++) {
                                if (j != i && j != owner) {
                                    sent2node(node_records[j].node_write_sock, page, PTR_LEN, 0);
                                    recvfromnode(node_records[j].node_write_sock, drop_page_content, getpagesize(), MSG_WAITALL);
                                    log_invalidate(fptr, j, page_count);
                                } else if (j == owner) {
                                   sent2node(node_records[j].node_write_sock, page, PTR_LEN, 0);
                                   recvfromnode(node_records[j].node_write_sock, page_content, getpagesize(), MSG_WAITALL);
                                   log_invalidate(fptr, j, page_count);
                                } else {
                                    continue;
                                }
                            }
                            sent2node(node_records[i].node_socket, page_content, getpagesize(), 0);
                            recvfromnode(node_records[i].node_socket, cmd, MSG_LEN, MSG_WAITALL);
                            log_recvownership(fptr, i, page_count);
                            free(page_content);
                            free(drop_page_content);
                            break;
                        }
                        case -1: {
                            // disconnection, tell all nodes to exit
                            delete_all_nodes(node_records, &(dsm_args.nodes));
                            break;
                        }
                        // no valid signal
                        default:
                        break;
                    }
                }
            }
            if (dsm_args.nodes == 0) {
                break;
            }
        } else {
            fprintf(stderr, "TIMEOUT\n");
            break;
        }
    }
    // free all resources
    if (fptr != NULL) {
        fclose(fptr);
    }
    free(ssh_argv);
    free(buffer_for_hostfile);
    if (hostnames != NULL)
        free(hostnames);
    free(root_ip);
    if (dsm_args.node_argv != NULL) {
        free(dsm_args.node_argv);
    }
    if (munmap(starting_addr, getpagesize()*PAGE_NUM) == -1) {
        perror("munmap fail");
        exit(1);
    }
    // act as a allocator
    return 0;
}
