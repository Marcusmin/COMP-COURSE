#include <types.h>
#include <kern/errno.h>
#include <lib.h>
#include <proc.h>
#include <current.h>
#include <thread.h>
#include <addrspace.h>
#include <vm.h>
#include <machine/tlb.h>
#include <spl.h>

// struct spinlock *lock_vm;

/* Place your page table functions here */


void vm_bootstrap(void)
{
    /* Initialise VM sub-system.  You probably want to initialise your 
       frame table here as well.
    */
}

int
vm_fault(int faulttype, vaddr_t faultaddress)
{
    elo_t** page_table;
    int rooti, leafi;   // root table index, leaf table index
	uint32_t ehi, elo;
    struct addrspace *as;
    struct region *head;
    int spl;

    if (faulttype == VM_FAULT_READONLY) {
        return EFAULT;
    }
    // current proc's addrspace
    if (curproc == NULL) {
		return EFAULT;
	}
    as = proc_getas();
    if (as == NULL) {
        return EFAULT;
    }
    page_table = as->page_table;
    // translate faultaddress
    rooti = faultaddress >> 22;
    leafi = (faultaddress << 10) >> 22;
    // look up page table to find fault address
    if (page_table[rooti] != NULL && page_table[rooti][leafi] != 0) {
        // valid translation, load tlb
        spl = splhigh();    //disable interrupt
        ehi = (faultaddress >> 12) << 12;
        elo = page_table[rooti][leafi];
        tlb_random(ehi, elo);
        splx(spl); // enable interrupt
        return 0;
    } else {
        // look up region
        head = as->head;
        while (head != NULL) {
            if (faultaddress >= head->region_base 
                && faultaddress < (head->region_base + head->region_size)) {
                    // allocate frame && insert pte
                    // check validity
                    spl = splhigh();    //disable interrupt
                    vaddr_t newframe = alloc_kpages(1);
                    bzero((void *)newframe, PAGE_SIZE);
                    paddr_t pfn = KVADDR_TO_PADDR(newframe);
                    elo_t pte = pfn & PAGE_FRAME;
                    switch (head->permissions) {
                        case 0b000: // --- == --
                            elo = pte;
                            break;
                        case 0b110: // rw-
                        case 0b111: // rwx
                        case 0b011: // -wx
                        case 0b010: // -w-
                            elo = (pte | TLBLO_DIRTY | TLBLO_VALID);
                            break;
                        case 0b101: // r-x
                        case 0b100: // r--
                        case 0b001: // --x
                            elo = (pte | TLBLO_VALID);
                            break;
                        default:
                            panic("Invalid permission bits\n");
                            break;
                    }
                    ehi = faultaddress & PAGE_FRAME;
                    // insert into page table
                    if (page_table[rooti] == NULL) {
                        page_table[rooti] = kmalloc(sizeof(elo_t)*1024);
                        bzero((void *)page_table[rooti], sizeof(elo_t)*1024);
                    }
                    page_table[rooti][leafi] = elo;
                    tlb_random(ehi, elo);
                    splx(spl); // enable interrupt
                    return 0;
                    // break;
            }
            head = head->next;
        }
    }
    // panic("Nohhhhhhhh!\n");
    return EFAULT;
}

/*
 *
 * SMP-specific functions.  Unused in our configuration.
 */

void
vm_tlbshootdown(const struct tlbshootdown *ts)
{
	(void)ts;
	panic("vm tried to do tlb shootdown?!\n");
}
