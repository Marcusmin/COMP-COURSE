#include <types.h>
#include <kern/errno.h>
#include <kern/fcntl.h>
#include <kern/limits.h>
#include <kern/stat.h>
#include <kern/seek.h>
#include <lib.h>
#include <uio.h>
#include <thread.h>
#include <current.h>
#include <synch.h>
#include <vfs.h>
#include <vnode.h>
#include <file.h>
#include <syscall.h>
#include <copyinout.h>
#include <endian.h>
// get access to current proc
#include <proc.h>
/*
 * Add your file-related functions here ...
 */

// open file table is global variable
// NOTE: global open file table should be mutual exclusive
OPEN_FILE_TABLE *open_ftable;

// filename is a pointer to userspace
int sys_open(userptr_t filename, int flags, mode_t mode, int32_t *retval){
    char pathname[__OPEN_MAX];
    size_t got = 0;
    int errno;
    struct vnode *vnode_pptr;

    // a pointer to open file entity
    // fd_entry fd = NULL;// error
    // return value from system call
    int fdno;
    // open file entry pointer
    OPEN_FILE_ENTRY *entry_ptr;

    // current process's info
    fd_table *cur_fd_table = curproc->fd_table_ptr;
    // copy filename to pathname
    // NOTE: len yet understanding
    errno = copyinstr(filename, pathname, __OPEN_MAX, &got);
    if (errno) {
        kprintf("copy filename fail\n");
        return errno;
    }
    // if copy success, assign a vnode pointer to vnode_ptr
    errno = vfs_open(pathname, flags, mode, &vnode_pptr);
    if (errno) {
        kprintf("vfs_open fail\n");
        return errno;   // error code handle by vfs sys
    }

    entry_ptr = creat_openfile_entry(vnode_pptr);

    if (entry_ptr == NULL){
        return ENFILE;  // too many file in open file table
    }

    insert_open_file_entry(entry_ptr);
    errno = add_fd_entry(entry_ptr, cur_fd_table, &fdno);
    if (errno) {
        return EMFILE; // too many file in fd table
    }
    *retval = fdno;
    // return fd number to retval
    return 0;   //indicating success
}

// ssize_t read(int fd, void *buf, size_t buflen);
int sys_read(int fd, userptr_t buf, size_t buflen, int32_t *retval){
    int errno;
    int readbytes;
    // find open file entry by fd
    struct vnode *file;
    fd_entry opf_ptr = fd2opf(fd, curproc);
    errno = fd2vnode(fd, curproc, &file);
    if (errno) {
        // fd might be invalid
        return errno;
    }
    struct iovec *iov = kmalloc(sizeof(struct iovec));
    struct uio *u = kmalloc(sizeof(struct uio));
    // parameter of uio init
    userptr_t ubuf = buf;   // provided by user to store content of file
    size_t len = buflen;    // provided by user to confine the length of buffer
    off_t offset = opf_ptr->offset;
    // off_t offset = 0;   // read from beginning of the file
    enum uio_rw rw = UIO_READ;  // read
    // init uio which point to user buffer
    uio_uinit(iov, u, ubuf, len, offset, rw);
    // I assume that read the file content directly to buffer provide by user
    errno = VOP_READ(file, u);
    if (errno) {
        return errno;
    } else {
        readbytes = len - u->uio_resid;
        *retval = readbytes;
        opf_ptr->offset += readbytes;
        return 0;
    }
}

// ssize_t write(int fd, const void *buf, size_t nbytes);
int sys_write(int fd, userptr_t buf, size_t nbytes, int32_t *retval){
    int errno;
    int writebytes;
    struct vnode *file;
    // grab vnode
    fd_entry opf_ptr = fd2opf(fd, curproc);
    errno = fd2vnode(fd, curproc, &file);
    if (errno) {
        // fd might be invalid
        return errno;
    }
    struct iovec *iov = kmalloc(sizeof(struct iovec));
    struct uio *u = kmalloc(sizeof(struct uio));
    // para for uio
    userptr_t ubuf = buf;   // provided by user to store content of file
    size_t len = nbytes;    // provided by user to confine the length of buffer
    off_t offset = opf_ptr->offset;
    enum uio_rw rw = UIO_WRITE;  // write
    // init uio
    uio_uinit(iov, u, ubuf, len, offset, rw);

    errno = VOP_WRITE(file, u);
    if (errno) {
        return errno;
    } else {
        writebytes = len - u->uio_resid;
        opf_ptr->offset += writebytes;
        *retval = writebytes;
        return 0;
    }
}

// int close(int fd);
int sys_close(int fd, int32_t *retval){
    int errno;
    struct vnode *file;
    errno = fd2vnode(fd, curproc, &file);
    if (errno) {
        // fd might be invilid
        return errno;
    }
    vfs_close(file);
    fd_entry opf = curproc->fd_table_ptr->fd_pointers[fd];
    opf->refcount--;
    if (opf->refcount <= 0) {
        rm_fd_entry(fd, opf, curproc->fd_table_ptr);
        rm_open_file_entry(opf);
    } else {
        rm_fd_entry(fd, opf, curproc->fd_table_ptr);
    }
    *retval = 0;
    return 0;
}

// int dup2(int oldfd, int newfd);
int sys_dup2(int oldfd, int newfd, int32_t *retval){ 
    int errno_old;
    int errno_new;
    // int dup2bytes;
    struct vnode *file_old, *file_new;
    // grab two vnodes
    errno_old = fd2vnode(oldfd, curproc, &file_old);
    errno_new = fd2vnode(newfd, curproc, &file_new);
    if (oldfd == newfd) {
        // two files are the same
        return 0;
    }
    if (errno_old) {
        // fd might be invilid
        return errno_old;
    }
    if (errno_new) {
        // fd might be invilid
        return errno_new;
    }
    // if newfd is already opened, close it
    if (curproc->fd_table_ptr->fd_pointers[newfd] != NULL) {
        vfs_close(file_new);
        kfree(curproc->fd_table_ptr->fd_pointers[newfd]);
        curproc->fd_table_ptr->fd_pointers[newfd] = NULL;
    }
	curproc->fd_table_ptr->fd_pointers[newfd] = curproc->fd_table_ptr->fd_pointers[oldfd];
    curproc->fd_table_ptr->fd_pointers[newfd]->refcount ++;
    *retval = 0;
    return 0;
}

// int lseek(int fd, off_t pos, int whence);
// off_t is 64 bits, split into two 32 bits value
// whence is a stack pointer
/*  
    useful given functions:
    join32to64(tf->tf_a2, tf->tf_a3, &offset);
    copyin((userptr_t)tf->tf_sp + 16, &whence, sizeof(int));
    split64to32(retval64, &tf->tf_v0, &tf->tf_v1);
*/
int sys_lseek(int fd, uint32_t x1, uint32_t x2, userptr_t whence, uint32_t *retval, uint32_t *retval2){
    struct vnode *vn;
    fd_entry opf_ptr;
    int errno, mode;
    bool seekable;
    uint64_t offset;
    off_t pos;
    struct stat f_info;

    join32to64(x1, x2, &offset);    // whence
    pos = (off_t)offset;
    if (pos < 0) {
        return EINVAL; //negative offset 
    }

    errno = copyin(whence, &mode, sizeof(int));
    if (errno){ // copy in fail
        return errno;
    }

    errno = fd2vnode(fd, curproc, &vn);
    if (errno) {    //fd might be invalid
        return errno;
    }

    opf_ptr = fd2opf(fd, curproc);
    seekable = VOP_ISSEEKABLE(vn);
    if (!seekable) {
        return ESPIPE;  // not seekable
    }
    errno = VOP_STAT(vn, &f_info);
    if (errno) {
        return errno;
    }
    switch(mode) {
        case SEEK_SET:  //the new position is pos
            opf_ptr->offset = pos;
        break;
        case SEEK_CUR:
            opf_ptr->offset += pos;
        break;
        case SEEK_END:
            opf_ptr->offset = f_info.st_size + pos;
        break;
        default:
            kprintf("Invalid whence\n");
            return EINVAL;  // invalid whence
        break;
    }
    // sucess
    offset = (uint64_t)opf_ptr->offset;
    split64to32(offset, retval, retval2);
    return 0;
}

// create an open file entry
OPEN_FILE_ENTRY *creat_openfile_entry(struct vnode *vdptr) {
    OPEN_FILE_ENTRY *of_entry = kmalloc(sizeof(OPEN_FILE_ENTRY));
    if (of_entry != NULL) {
        of_entry->refcount = 1; // must have one fd referenced to this entry yet
        of_entry->fstate = read;
        of_entry->offset = 0;
        of_entry->vdptr = vdptr;
        of_entry->next = NULL;
        of_entry->prev = NULL;
    }
    return of_entry;    // return a pointer to open file entry
}

// initililaze a open file table
// success return 0
// otherwise return 1
int init_open_file_table(void){
    // table
    open_ftable = (OPEN_FILE_TABLE*)kmalloc(sizeof(OPEN_FILE_TABLE));
    if (open_ftable == NULL){
        return 1;   //indicating fail
    } else {
        open_ftable->head = NULL;
        open_ftable->tail = NULL;
        return 0;
    }
}

void insert_open_file_entry(OPEN_FILE_ENTRY *entry) {
    // define locks during manipulate files in multi-processes cases
    struct lock *lock_ftable;
    lock_ftable = lock_create("lock_ftable");
    lock_acquire(lock_ftable);
    if (open_ftable->head == NULL && open_ftable->tail == NULL) {
        // insert first elment
        open_ftable->head = entry;
        open_ftable->tail = entry;
    } else {
        // insert at tail end
        open_ftable->tail->next = entry;
        entry->prev = open_ftable->tail;
        open_ftable->tail = entry;
    }
    lock_release(lock_ftable);
}
// Initialize fd table in proc.c
fd_table *init_fd_table(struct proc *proc_entity) {
    fd_table *fdt_ptr;
    struct ft_node *node;
    fdt_ptr = kmalloc(sizeof(fd_table));
    // proc entity is a identifier of process
    if (fdt_ptr == NULL) {
        panic("Cannot create file discriptor table for process");
    }
    fdt_ptr->proc_entry = proc_entity;
    // Initially, next fd number is 0
    fdt_ptr->next_fd = 0;
    fdt_ptr->ft = kmalloc(sizeof(struct free_table));   // malloc free table
    for(int i = 0; i < __OPEN_MAX; i++){
        fdt_ptr->fd_pointers[i] = NULL;
    }
    // create first node in free table
    node = kmalloc(sizeof(struct ft_node));
    node->element = 0;
    node->next = NULL;
    node->prev = NULL;
    // init free table
    fdt_ptr->ft->head = node;
    fdt_ptr->ft->tail = node;
    return fdt_ptr;
}

int add_fd_entry(fd_entry entry, fd_table *table, int *retfd) {
    int next_fd;
    struct ft_node *node;
    // define locks during manipulate files in multi-processes cases
    struct lock *lock_ftable;
    lock_ftable = lock_create("lock_ftable");
    lock_acquire(lock_ftable);
    table->next_fd++;
    // create new node
    node = kmalloc(sizeof(struct ft_node));
    node->element = table->next_fd;
    node->next = NULL;
    node->prev = NULL;
    // make sure free table at least has one node
    // add node at tail
    table->ft->tail->next = node;
    node->prev = table->ft->tail;
    table->ft->tail = node;
    next_fd = table->ft->head->element;
    // pop out from free table
    table->ft->head = table->ft->head->next;
    kfree(table->ft->head->prev);
    table->ft->head->prev = NULL;
    
    if (next_fd >= __OPEN_MAX) {
        // too many file opened in this process
        return EMFILE;
    }
    table->fd_pointers[next_fd] = entry;
    // assign fd to retfd
    *retfd = next_fd;
    // return value which indicating success
    lock_release(lock_ftable);
    return 0;
}

int fd2vnode(int fd, struct proc *cproc, struct vnode **file) {
    if (fd > __OPEN_MAX ||
        cproc->fd_table_ptr->fd_pointers[fd] == NULL){
        return EBADF;   // incorrect fd number
    }
    // need improve
    fd_entry of_ptr = cproc->fd_table_ptr->fd_pointers[fd];    // grab open file's pointer from fd table
    *file = of_ptr->vdptr;
    return 0;
}

fd_entry fd2opf(int fd, struct proc *cproc) {
    return cproc->fd_table_ptr->fd_pointers[fd];
}

int rm_open_file_entry(OPEN_FILE_ENTRY *entry) {
    // We assume that this file entry exist
    if (entry == NULL){
        kprintf("file doesn't exist\n");
        return ENOENT;
    }
    if (entry->prev == NULL && entry->next == NULL) {  // which mean entry is the only node in table
        open_ftable->head = NULL;
        open_ftable->tail = NULL;
        kfree(entry);
    } else if (entry->next == NULL) {   //which means entry is the tail of table
        entry->prev->next = NULL;
        open_ftable->tail = entry->prev;
        kfree(entry);
    } else if (entry->prev == NULL) {   // which means entry is the head of table
        entry->next->prev = NULL;
        open_ftable->head = entry->next;
        kfree(entry);
    } else {
        entry->next->prev = entry->prev;
        entry->prev->next = entry->next;
        kfree(entry);
    }
    return 0;
}

int rm_fd_entry(int fd, fd_entry entry, fd_table *table) {
    // which mean fd can be use again
    // insert fd into free table
    struct ft_node *node;
    struct ft_node *pointer = table->ft->head;
    node = kmalloc(sizeof(struct ft_node));
    node->element = fd;
    node->next = NULL;
    node->prev = NULL;

    if (entry == NULL) {
        kprintf("file doesn't exist\n");
        return ENOENT;
    }

    while (pointer != NULL && node->element > pointer->element) {
        pointer = pointer->next;
    }

    if (pointer == NULL){
        if (table->fd_pointers[fd] != NULL){
            table->fd_pointers[fd] = NULL;
            return 0;
        } else {
            return EBADF;   // fd uses lowest number, this won't happen in normal
        }
    } else if (node->element == pointer->element) {
        return EBADF;  // fd incorrect
    } else {
        if (pointer->prev == NULL) {
            // pointer is head
            table->ft->head = node;
            pointer->prev = node;
            node->next = pointer;
        } else {
            pointer->prev->next = node;
            node->prev = pointer->prev;
            pointer->prev = node;
            node->next = pointer;
        }
        return 0;
    }
}