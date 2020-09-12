/*
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2008, 2009
 *	The President and Fellows of Harvard College.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE UNIVERSITY OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

/*
 * Sample/test code for running a user program.  You can use this for
 * reference when implementing the execv() system call. Remember though
 * that execv() needs to do more than runprogram() does.
 */

#include <types.h>
#include <kern/errno.h>
#include <kern/fcntl.h>
#include <lib.h>
#include <proc.h>
#include <current.h>
#include <addrspace.h>
#include <vm.h>
#include <vfs.h>
#include <syscall.h>
#include <test.h>

// global open file table
OPEN_FILE_TABLE *open_ftable;

/*
 * Load program "progname" and start running it in usermode.
 * Does not return except on error.
 *
 * Calls vfs_open on progname and thus may destroy it.
 */
int
runprogram(char *progname)
{
	struct addrspace *as;
	struct vnode *v;
	vaddr_t entrypoint, stackptr;
	int result;

	// stdin, stdout, stderr related para
	char dev_stdin[] = "con:";
	char dev_stdout[] = "con:";
	char dev_stderr[] = "con:";
	struct vnode *std_ptr[3];
	OPEN_FILE_ENTRY *std_entity[3];
	int stderrno;
	int std_index[3];
 
	/* Open the file. */
	result = vfs_open(progname, O_RDONLY, 0, &v);
	if (result) {
		return result;
	}

	/* We should be a new process. */
	KASSERT(proc_getas() == NULL);

	/* Create a new address space. */
	as = as_create();
	if (as == NULL) {
		vfs_close(v);
		return ENOMEM;
	}

	/* Switch to it and activate it. */
	proc_setas(as);
	as_activate();

	/* Load the executable. */
	result = load_elf(v, &entrypoint);
	if (result) {
		/* p_addrspace will go away when curproc is destroyed */
		vfs_close(v);
		return result;
	}

	/* Done with the file now. */
	vfs_close(v);

	/* Define the user stack in the address space */
	result = as_define_stack(as, &stackptr);
	if (result) {
		/* p_addrspace will go away when curproc is destroyed */
		return result;
	}

	// system to open stdin, stdout, stderr
	// create stdin
	stderrno = vfs_open(dev_stdin, O_RDONLY, O_CREAT|O_EXCL, &std_ptr[0]);	
	if (stderrno) {
		kprintf("Error: fail to open stdin, vfs_open fail %d\n", stderrno);
	}
	std_entity[0] = creat_openfile_entry(std_ptr[0]);
	insert_open_file_entry(std_entity[0]);
	stderrno = add_fd_entry(std_entity[0], curproc->fd_table_ptr, &std_index[0]);
	// open too many file, could be weird for creating stdin
	// if (stderrno) {
	// 	kprintf("Error: fail to open stdin for too many file\n");
	// } else {
	// 	kprintf("stdin:fd is %d\n", std_index[0]);
	// }
	// create stdout
	stderrno = vfs_open(dev_stdout, O_WRONLY, O_CREAT|O_EXCL, &std_ptr[1]);	//create stdin
	if (stderrno) {
		kprintf("Error: fail to open stdout, vfs_open fail %d\n", stderrno);
	}
	std_entity[1] = creat_openfile_entry(std_ptr[1]);
	insert_open_file_entry(std_entity[1]);
	stderrno = add_fd_entry(std_entity[1], curproc->fd_table_ptr, &std_index[1]);
	// open too many file, could be weird for creating stdin
	// if (stderrno) {
	// 	kprintf("Error: fail to open stdout for too many files\n");
	// } else {
	// 	kprintf("stdout: fd is %d\n", std_index[1]);
	// }
	// create stderr
	stderrno = vfs_open(dev_stderr, O_WRONLY, O_CREAT|O_EXCL, &std_ptr[2]);	//create stdin
	// if (stderrno) {
	// 	kprintf("Error: fail to open stderr,vfs_open fail %d\n", stderrno);
	// }
	std_entity[2] = creat_openfile_entry(std_ptr[2]);
	insert_open_file_entry(std_entity[2]);
	stderrno = add_fd_entry(std_entity[2], curproc->fd_table_ptr, &std_index[2]);
	// open too many file, could be weird for creating stdin
	// if (stderrno) {
	// 	kprintf("Error: fail to open stderr for too many file\n");
	// } else {
	// 	kprintf("stderr: fd is %d\n", std_index[2]);
	// }

	/* Warp to user mode. */
	enter_new_process(0 /*argc*/, NULL /*userspace addr of argv*/,
			  NULL /*userspace addr of environment*/,
			  stackptr, entrypoint);

	/* enter_new_process does not return. */
	panic("enter_new_process returned\n");
	return EINVAL;
}

