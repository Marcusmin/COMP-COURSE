/*
 * Declarations for file handle and file table management.
 */

#ifndef _FILE_H_
#define _FILE_H_

/*
 * Contains some file-related maximum length constants
 */
#include <limits.h>

// assume that off_t defined

// track file state
enum state {write, read};

// struct for track a open file
// entry in open file table
typedef struct open_file_entry {
    int refcount;                   // count the number of reference to this file
    enum state fstate;              // a flag indicating either write or read
    struct vnode *vdptr;            // a pointer to a vnode
    off_t offset;                   // offset of file pointer(maybe need to include types.h)
                                    // linklist attribute
    struct open_file_entry *next;   // next entry
    struct open_file_entry *prev;   // previous entry
} OPEN_FILE_ENTRY;

// linklist for store the table of open files
typedef struct open_file_table{
    OPEN_FILE_ENTRY *head;
    OPEN_FILE_ENTRY *tail;
} OPEN_FILE_TABLE;

// a pointer to open file entry
typedef OPEN_FILE_ENTRY* fd_entry;

typedef struct fd_table {
    struct proc *proc_entry;
    fd_entry fd_pointers[__OPEN_MAX];   // pointer to a file discriptor
    struct free_table *ft;  //free table record free fd
    int next_fd; // indicating next fd;
} fd_table;

// record free fd to use
struct free_table {
    struct ft_node *tail;
    struct ft_node *head;
};
struct ft_node {
    int element;
    struct ft_node * next;
    struct ft_node * prev;
};
// add an open file entry to open file table
void insert_open_file_entry(OPEN_FILE_ENTRY *entry);

// remove open file entry from open file table
int rm_open_file_entry(OPEN_FILE_ENTRY *entry);

// initialize open file table
int init_open_file_table(void);

// initilaize file discriptor
fd_table *init_fd_table(struct proc *proc_entity);

// remove fd entry
int rm_fd_entry(int fd, fd_entry entry, fd_table *table);

// add fd entry
int add_fd_entry(fd_entry entry, fd_table *table, int *retfd);

//create open file entry
OPEN_FILE_ENTRY *creat_openfile_entry(struct vnode *vdptr);

// map fd to vnode
int fd2vnode(int fd, struct proc *cproc, struct vnode **file);

// map fd to open file entry
// make sure the fd is valid
fd_entry fd2opf(int fd, struct proc *cproc);

// global variable open file table
extern OPEN_FILE_TABLE *open_ftable;
/*
 * Put your function declarations and data types here ...
 */


#endif /* _FILE_H_ */
