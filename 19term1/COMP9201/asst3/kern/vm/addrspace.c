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

#include <types.h>
#include <kern/errno.h>
#include <lib.h>
#include <spl.h>
#include <spinlock.h>
#include <current.h>
#include <mips/tlb.h>
#include <addrspace.h>
#include <vm.h>
#include <proc.h>

/*
 * Note! If OPT_DUMBVM is set, as is the case until you start the VM
 * assignment, this file is not compiled or linked or in any way
 * used. The cheesy hack versions in dumbvm.c are used instead.
 *
 * UNSW: If you use ASST3 config as required, then this file forms
 * part of the VM subsystem.
 *
 */

struct addrspace *
as_create(void)
{
	struct addrspace *as;

	as = kmalloc(sizeof(struct addrspace));
	bzero((void *)as, sizeof(struct addrspace));
	if (as == NULL) {
		panic("Create addrspace fail\n");
		return NULL;
	}
	as->head = NULL;
	as->page_table = kmalloc(sizeof(elo_t*)*1024);
	bzero((void *)as->page_table, sizeof(elo_t)*1024);
	/*
	 * Initialize as needed.
	 */

	return as;
}

int
as_copy(struct addrspace *old, struct addrspace **ret)
{
	struct addrspace *newas;
	struct region *head, *as_region;
	struct region *tail = NULL;
	int index = 0;

	newas = as_create();
	if (newas==NULL) {
		return ENOMEM;
	}

	/*
	 * Write this.
	 */
	head = old->head;	// same regions
	newas->head = NULL;
	while (head != NULL) {
		as_region = kmalloc(sizeof(struct region));
		bzero((void *)as_region, sizeof(struct region));
		// copy member of struct
		as_region->load_permissions = head->load_permissions;
		as_region->permissions = head->permissions;
		as_region->region_base = head->region_base;
		as_region->region_size = head->region_size;
		as_region->next = NULL;
		if (index == 0) {
			newas->head = as_region;
			tail = as_region;
		} else {
			tail->next = as_region;
			tail = tail->next;
		}
		head = head->next;
		index++;
	}

	// find mapped page in newas
	for (int i = 0; i < 1024; i++){
		if (old->page_table[i] != NULL) {
			elo_t* leaf_table = old->page_table[i];
			for (int j = 0; j < 1024; j++) {
				if (leaf_table[j] != 0) {	// mapped entry
					elo_t old_entry = leaf_table[j];
					paddr_t old_pfn = old_entry & PAGE_FRAME;	// frame_num to addr
					vaddr_t new_vaddr = alloc_kpages(1);
					bzero((void *)new_vaddr, PAGE_SIZE);
					memcpy(
						(void *) new_vaddr,
						(void *) PADDR_TO_KVADDR(old_pfn),	// args are vaddr
						PAGE_SIZE
					);
					// insert new frame to page table
					elo_t validbits = old_entry & 0x00000fff;
					elo_t new_entry = KVADDR_TO_PADDR(new_vaddr)|validbits;
					if (newas->page_table[i] == NULL) {
						newas->page_table[i] = kmalloc(sizeof(elo_t)*1024);
						bzero(newas->page_table[i], sizeof(elo_t)*1024);
						newas->page_table[i][j] = new_entry;
					} else {
						newas->page_table[i][j] = new_entry;
					}
				}
			}
		}
	}
	*ret = newas;
	return 0;
}

void
as_destroy(struct addrspace *as)
{
	/*
	 * Clean up as needed.
	 */
	struct region* as_region;
	struct region* head;
	// clean page table
	for (int i = 0; i < 1024; i++) {
		if (as->page_table[i] != NULL) {
			for (int j = 0; j < 1024; j++) {
				if (as->page_table[i][j] != 0) {
					elo_t pte = as->page_table[i][j];
					paddr_t paddr = (pte >> 12) << 12;
					vaddr_t vaddr = PADDR_TO_KVADDR(paddr);
					free_kpages(vaddr);
				}
			}
		}
	}
	for (int i = 0; i < 1024; i++) {
		if (as->page_table[i] != NULL) {
			kfree((void *)as->page_table[i]);
		}
	}
	kfree((void *)as->page_table);
	// clean as_regions
	head = as->head;
	while (head != NULL) {
		as_region = head->next;
		kfree(head);
		head = as_region;
	}
	// free as
	kfree(as);
}

void
as_activate(void)
{
	struct addrspace *as;
	int spl;

	as = proc_getas();
	if (as == NULL) {
		/*
		 * Kernel thread without an address space; leave the
		 * prior address space in place.
		 */
		return;
	}
		/*
	 * Write this.
	 */
	spl = splhigh();

	for (int i=0; i<NUM_TLB; i++) {
		tlb_write(TLBHI_INVALID(i), TLBLO_INVALID(), i);
	}

	splx(spl);
}

void
as_deactivate(void)
{
	/*
	 * Write this. For many designs it won't need to actually do
	 * anything. See proc.c for an explanation of why it (might)
	 * be needed.
	 */
	struct addrspace *as;
	int spl;

	as = proc_getas();
	if (as == NULL) {
		/*
		 * Kernel thread without an address space; leave the
		 * prior address space in place.
		 */
		return;
	}
		/*
	 * Write this.
	 */
	spl = splhigh();

	for (int i=0; i<NUM_TLB; i++) {
		tlb_write(TLBHI_INVALID(i), TLBLO_INVALID(), i);
	}

	splx(spl);

}

/*
 * Set up a segment at virtual address VADDR of size MEMSIZE. The
 * segment in memory extends from VADDR up to (but not including)
 * VADDR+MEMSIZE.
 *
 * The READABLE, WRITEABLE, and EXECUTABLE flags are set if read,
 * write, or execute permission should be set on the segment. At the
 * moment, these are ignored. When you write the VM system, you may
 * want to implement them.
 */
int
as_define_region(struct addrspace *as, vaddr_t vaddr, size_t memsize,
		 int readable, int writeable, int executable)
{
	/*
	 * Write this.
	 */

	// create a new region
	struct region *as_region;
	as_region = kmalloc(sizeof(struct region));
	bzero((void *)as_region, sizeof(struct region));
	as_region->next = NULL;	// currently being null
	as_region->region_base = vaddr;	// region's base virtual addr
	as_region->region_size = memsize;
	// validation of region
	as_region->permissions = (readable | writeable | executable);
	as_region->load_permissions = as_region->permissions | 0b010;	// set w bit
	// link to list
	as_region->next = as->head;
	as->head = as_region;
	return 0;
	// return ENOSYS; /* Unimplemented */
}

int
as_prepare_load(struct addrspace *as)
{
	/*
	 * Write this.
	 */
	// for each region, switch load_permissions and permissions
	struct region *head;
	int temp;
	head = as->head;
	while (head != NULL) {
		temp = head->load_permissions;
		head->load_permissions = head->permissions;
		head->permissions = temp;
		head = head->next;
	}
	return 0;
}

int
as_complete_load(struct addrspace *as)
{
	/*
	 * Write this.
	 */
	struct region *head;
	int temp;

	head = as->head;
	while (head != NULL) {
		temp = head->load_permissions;
		head->load_permissions = head->permissions;
		head->permissions = temp;
		head = head->next;
	}

	return 0;
}

int
as_define_stack(struct addrspace *as, vaddr_t *stackptr)
{
	/*
	 * Write this.
	 */
	// stack is fixed size
	
	struct region* stack;
	struct region* head;
	struct region* as_region;
	stack = kmalloc(sizeof(struct region));
	bzero((void *)stack, sizeof(struct region));
	stack->next = NULL;
	stack->region_base = USERSTACK - 16 * PAGE_SIZE - 1;
	stack->region_size = 16 * PAGE_SIZE;
	// assume that size of the region is 16*pagesize
	// traverse the regions to check if there is any overlap
	head = as->head;
	while (head != NULL) {
		as_region = head;
		if (as_region->region_base+as_region->region_size > stack->region_base) {
			stack->region_base = as_region->region_base+as_region->region_size;
			stack->region_size = USERSTACK - stack->region_base - 1;
		}
		head = as_region->next;
	}
	// init property of stack
	stack->permissions = 0b111;	//rwx
	stack->load_permissions = stack->permissions | 0b010;	// set w bit	// link to as linklist
	stack->next = as->head;
	as->head = stack;
	/* Initial user-level stack pointer */
	*stackptr = USERSTACK;

	return 0;
}

