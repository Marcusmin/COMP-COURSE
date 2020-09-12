#define DEFAULT_PROTOCAL 0
#define _DEFAULT_SOURCE
#include "sm.h"
#include <netdb.h> 
#include <stdio.h> 
#include <stdlib.h> 
#include <string.h> 
#include <signal.h>
#include <sys/types.h>
#include <errno.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#define SSH_ARGC 4
// shared the same signal code with dsm
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
// page && communication config
#define MSG_LEN 21
#define PAGE_NUM 0xffff
#define PTR_LEN 21

// #define DEBUG
// the tcp socket of dsm should be store global
static int sm_socket;
static void* sm_mem;    // start addr
static void* prev_addr;
static int sm_psize;
static int sm_nid;
static struct sigaction sm_segfault_act;
static struct sigaction sm_sigio_act;
static int sm_sigcount = 0;
static int sm_async_sock;
static int sm_write_sock;
static int page_map[PAGE_NUM];


int get_page_num(void *ptr) {
    return ((intptr_t)ptr - (intptr_t)sm_mem)/getpagesize();
}

void temp_block_signal(int signal) {
    sigset_t set;
    sigemptyset(&set);
    sigaddset(&set, signal);
    sigprocmask(SIG_BLOCK, &set, NULL);
}

void unblock_signal(int signal) {
    sigset_t set;
    sigemptyset(&set);
    sigaddset(&set, signal);
    sigprocmask(SIG_UNBLOCK, &set, NULL);
}


void int2char(char* buffer, int len, long value) {
    memset(buffer, 0, len);
    sprintf(buffer, "%d", value);
}

void sent2dsm(int socket, void* buffer, int len, int flag) {
    int status;
    while (status = send(socket, buffer, len, flag) != len) {
        if (status == -1) {
            perror("send to dsm");
        } else if (status == 0) {
            exit(1);
        }
    }
}

void recv4dsm(int socket, void* buffer, int len, int flag) {
    int status;
    while (status = recv(socket, buffer, len, flag) != len) {
        if (status == -1) {
            perror("recv from dsm");
        } else if (status == 0) {
            exit(1);
        }
    }
}

void sm_sigsevhandler(int signum, siginfo_t *sinfo, void *ctx) {
    // align the page address
    temp_block_signal(SIGSEGV);
    void *fault_addr = (void*)((intptr_t)sinfo->si_addr - (intptr_t)sinfo->si_addr % getpagesize());
    int pn = get_page_num(fault_addr);
    page_map[pn]++;
    if (page_map[pn] == 2) {
        page_map[pn] = 0;
        // owner of this page should change
        char command[MSG_LEN];
        int2char(command, MSG_LEN, WRITE);
        sent2dsm(sm_socket, command, MSG_LEN, 0);
        // wait ack
        recv4dsm(sm_socket, command, MSG_LEN, MSG_WAITALL);


        temp_block_signal(SIGPOLL);
        char ptr[PTR_LEN];
        int2char(ptr, PTR_LEN, (intptr_t)fault_addr);
        if (mprotect(fault_addr, getpagesize(), PROT_WRITE) == -1) {
            perror("handle write fault");
            exit(1);
        }
        void* page_content = calloc(getpagesize(), 1);
        sent2dsm(sm_socket, ptr, PTR_LEN, 0);
        recv4dsm(sm_socket, page_content, getpagesize(), MSG_WAITALL);
        memcpy(fault_addr, page_content, getpagesize());
        // check(fault_addr, 3, sm_nid);
        // send ack to dsm
        sent2dsm(sm_socket, command, MSG_LEN, 0);
        unblock_signal(SIGPOLL);
        // send allocator that the owner of the page should change
    } else {
        // handle read fault
        //request the allocator this page
        char cmd[MSG_LEN];
        char ptr[PTR_LEN];
        int2char(cmd, MSG_LEN, READ);
        int2char(ptr, PTR_LEN, (intptr_t)fault_addr);
        // send read command to dsm
        sent2dsm(sm_socket, cmd, MSG_LEN, 0);
        recv4dsm(sm_socket, cmd, MSG_LEN, MSG_WAITALL);


        // send addr where occur page fault
        sent2dsm(sm_socket, ptr, PTR_LEN, 0);
        void* page_buffer = calloc(getpagesize(), 1);
        recv4dsm(sm_socket, page_buffer, getpagesize(), MSG_WAITALL);
        temp_block_signal(SIGPOLL);
        if (mprotect(fault_addr, getpagesize(), PROT_WRITE) == -1) {
            perror("handle read fault");
            exit(1);
        }
        memcpy(fault_addr, page_buffer, getpagesize());
        if (mprotect(fault_addr, getpagesize(), PROT_READ) == -1) {
            perror("handle read fault");
            exit(1);
        }
        // send ack indicating read success
        sent2dsm(sm_socket, ptr, PTR_LEN, 0);
        unblock_signal(SIGPOLL);
    }
    unblock_signal(SIGSEGV);
}

void sm_sigiohandler(int signum, siginfo_t *sinfo, void *ctx) {
    // temp_block_signal(SIGSEGV);
    // set up socket set
    fd_set sock_set;
    FD_ZERO(&sock_set);
    FD_SET(sm_async_sock, &sock_set);
    FD_SET(sm_write_sock, &sock_set);
    int fdmax = (sm_async_sock > sm_write_sock ? sm_async_sock: sm_write_sock);
    int slc = select(fdmax+1, &sock_set, NULL, NULL, NULL);
    if (slc == -1) {
        perror("select");
        exit(1);
    } else if (slc) {
        if (FD_ISSET(sm_async_sock, &sock_set)) {
            char page_ptr[PTR_LEN];
            memset(page_ptr, 0, PTR_LEN);
            recv4dsm(sm_async_sock, page_ptr, PTR_LEN, MSG_WAITALL);
            // send ack to dsm
            temp_block_signal(SIGPOLL);
            void *ptr = (void*)(intptr_t*)atol(page_ptr);
            void *page_buffer = calloc(getpagesize(), 1);
            memcpy(page_buffer, ptr, getpagesize());
            // send specific page to allocator
            sent2dsm(sm_async_sock, page_buffer, getpagesize(), 0);
            free(page_buffer);
            unblock_signal(SIGPOLL);
        }
        if (FD_ISSET(sm_write_sock, &sock_set)) {
            temp_block_signal(SIGPOLL);
            char page_ptr[PTR_LEN];
            memset(page_ptr, 0, PTR_LEN);
            recv4dsm(sm_write_sock, page_ptr, PTR_LEN, MSG_WAITALL);
            void *ptr = (void*)(intptr_t*)atol(page_ptr);
            if (mprotect(ptr, getpagesize(), PROT_READ) == -1) {
                perror("mprotect");
                exit(1);
            }
            void* page_content = calloc(getpagesize(), 1);
            int pn = get_page_num(ptr);
            page_map[pn] = 0;
            // send ack to indicate invalidate is success
            memcpy(page_content, ptr, getpagesize());
            sent2dsm(sm_write_sock, page_content, getpagesize(), 0);
            if (mprotect(ptr, getpagesize(), PROT_NONE) == -1) {
                perror("mprotect");
                exit(1);
            }
            unblock_signal(SIGPOLL);
        }
    } else {
        printf("%d: select unexpected error", sm_nid);
    }
}


void sm_init_sighandler() {
    // register a handler for signal segmentation fault
    sigemptyset(&sm_segfault_act.sa_mask);
    // sigaddset(&sm_segfault_act.sa_mask, SIGPOLL);
    // sigaddset(&sm_segfault_act.sa_mask, SIGSEGV);
    sm_segfault_act.sa_flags = SA_SIGINFO;
    sm_segfault_act.sa_sigaction = sm_sigsevhandler;
    sigaction(SIGSEGV, &sm_segfault_act, NULL);
    // handler async socket io event
    sigemptyset(&sm_sigio_act.sa_mask);
    sm_segfault_act.sa_flags = SA_SIGINFO;
    sm_sigio_act.sa_sigaction = sm_sigiohandler;
    // mask other signal
    sigaddset(&sm_sigio_act.sa_mask, SIGPOLL);
    // sigaddset(&sm_sigio_act.sa_mask, SIGSEGV);
    sigaction(SIGPOLL, &sm_sigio_act, NULL);
}

int sm_node_init (int *argc, char **argv[], int *nodes, int *nid) {
    // remove redundant arguments
    #ifdef DEBUG
    debug();
    #endif
    int cli_sock;
    struct sockaddr_in myaddr;
    if (*argc < SSH_ARGC+1) {   // may have executable
        // too few arguments
        perror("too few args:");
        return -1;
    }
    // keep useful message
    char* root_ip = (*argv)[1];
    char* root_port_str = (*argv)[2];
    int root_port = atoi((*argv)[2]);
    *nodes = atoi((*argv)[3]);
    *nid = atoi((*argv)[4]);
    int real_argc = (*argc)-SSH_ARGC;
    // remove them
    for(int i = SSH_ARGC+1; i < (*argc); i++) {
        // argv[0] should retain
        (*argv)[i-SSH_ARGC] = (*argv)[i];
    }
    for(int i = real_argc; i < (*argc); i++) {
        (*argv)[i] = NULL;
    }
    (*argc) = real_argc;
    // set up client socket
    cli_sock = socket(AF_INET, SOCK_STREAM, DEFAULT_PROTOCAL);
    // set up connection
    myaddr.sin_family = AF_INET;
    myaddr.sin_addr.s_addr = inet_addr(root_ip);
    myaddr.sin_port = htons(root_port);
    int status = connect(cli_sock, (struct sockaddr*)&myaddr, sizeof(myaddr));
    if (status) {
        perror("Connection fail");
        exit(1);
    } else {
        int pagesize = getpagesize();
        // pre-allocate the vm
        char addr[PTR_LEN];
        memset(addr, 0, PTR_LEN);
        int nbytes = recv(cli_sock, addr, PTR_LEN, MSG_WAITALL);
        if (nbytes == 0) {
            printf("%d: disconnection during init\n", sm_nid);
            exit(1);
        }
        // 
        sm_mem = (void *)atol(addr);
        if (mmap(sm_mem, pagesize*PAGE_NUM, PROT_NONE, MAP_PRIVATE|MAP_ANONYMOUS|MAP_FIXED, -1, 0)==MAP_FAILED) {
            perror("mmap fail");
            exit(1);
        }
        // register a signal handler for page fault
        sm_psize = getpagesize();
        sm_socket = cli_sock;
        sm_nid = (*nid);
        // register a signal handler
        // create async socket recv dsm message
        sm_async_sock = socket(AF_INET, SOCK_STREAM, DEFAULT_PROTOCAL);
        sm_write_sock = socket(AF_INET, SOCK_STREAM, DEFAULT_PROTOCAL);
        status = connect(sm_async_sock, (struct sockaddr*)&myaddr, sizeof(myaddr));
        if (status == -1) {
            perror("async socket set up fail");
            exit(1);
        }
          /* enable SIGPOLL on the socket */
        fcntl (sm_async_sock, F_SETFL, O_ASYNC);
        fcntl (sm_async_sock, F_SETOWN, getpid());
        status = connect(sm_write_sock, (struct sockaddr*)&myaddr, sizeof(myaddr));
        if (status == -1) {
            perror("async socket set up fail");
            exit(1);
        }
          /* enable SIGPOLL on the socket */
        fcntl (sm_write_sock, F_SETFL, O_ASYNC);
        fcntl (sm_write_sock, F_SETOWN, getpid());
        // enable signal handlers
        sm_init_sighandler();
    }
    return 0;
}

void sm_node_exit (void) {
    // NOTE: ADD ACK HERE!!!!
    sm_barrier();
    char option[MSG_LEN];
    // memset(option, 0, MSG_LEN);
    int2char(option, MSG_LEN, EXIT);
    sent2dsm(sm_socket, option, MSG_LEN, 0);
    if (munmap(sm_mem, getpagesize()*PAGE_NUM) == -1){
        perror("munmap fail");
        exit(1);
    }
    // whatever success or not, exit
    exit(0);
}

void sm_barrier (void) {
    char option[MSG_LEN];
    int2char(option, MSG_LEN, BARRIER);
    sent2dsm(sm_socket, option, MSG_LEN, 0);
    memset(option, 0, MSG_LEN);
    recv4dsm(sm_socket, option, MSG_LEN, MSG_WAITALL);
    sent2dsm(sm_socket, option, MSG_LEN, 0);
}

void *sm_malloc (size_t size) {
    // don't allocate new page if required size could on same page
    temp_block_signal(SIGPOLL);
    char* ret_ptr;
    if (prev_addr != 0 && ((intptr_t)prev_addr % getpagesize() + size) < getpagesize()-1) {
        ret_ptr = prev_addr;
        prev_addr += size;
        return ret_ptr;
    }
    // count how many page is required
    int page_num_expect = size/sm_psize;
    if (page_num_expect*sm_psize < size) {
        page_num_expect++;
    }
    char cmd[MSG_LEN];
    int2char(cmd, MSG_LEN, MALLOC);
    // send command
    sent2dsm(sm_socket, cmd, MSG_LEN, 0);
    // receive from dsm ack
    recv4dsm(sm_socket, cmd, MSG_LEN, MSG_WAITALL);
    int2char(cmd, MSG_LEN, page_num_expect);
    sent2dsm(sm_socket, cmd, MSG_LEN, 0);
    memset(cmd, 0, MSG_LEN);
    recv4dsm(sm_socket, cmd, MSG_LEN, MSG_WAITALL);
    int offset = atoi(cmd);
    ret_ptr = offset*getpagesize()+sm_mem;
    // enable read & write
    if (mprotect(ret_ptr, sm_psize*page_num_expect, PROT_READ|PROT_WRITE) == -1) {
        perror("malloc fail");
        exit(1);
    }
    // offset is the number of page allocated
    unblock_signal(SIGPOLL);
    return ret_ptr;
}

void sm_bcast (void **addr, int root_nid) {
    // send to allocator the address
    // allocator then send the value of addr to all other node
    char buffer[MSG_LEN];
    char ptr[PTR_LEN];
    int nbytes = 0;
    if (root_nid == sm_nid) {
        // notify the allocator that to bcast
        memset(buffer, 0, MSG_LEN);
        sprintf(buffer, "%d", CLAIM_OWNER);
        sent2dsm(sm_socket, buffer, MSG_LEN, 0);
        // dsm will block and waiting for reply
        recv4dsm(sm_socket, buffer, MSG_LEN, MSG_WAITALL);
        sprintf(ptr, "%d", *addr);
        // send the address
        sent2dsm(sm_socket, ptr, PTR_LEN, 0);
    }
    // receive msg after claim owner
    recv4dsm(sm_socket, buffer, MSG_LEN, MSG_WAITALL);
    // all node send && waiting for reply
    int2char(buffer, MSG_LEN, BCAST);
    sent2dsm(sm_socket, buffer, MSG_LEN, 0);
    memset(ptr, 0, PTR_LEN);
    recv4dsm(sm_socket, ptr, MSG_LEN, MSG_WAITALL);
    intptr_t cast_addr = atol(ptr);
    *addr = (void *)cast_addr;
}

