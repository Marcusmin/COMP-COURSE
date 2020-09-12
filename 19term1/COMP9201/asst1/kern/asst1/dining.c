#include "opt-synchprobs.h"
#include <types.h>  /* required by lib.h */
#include <lib.h>    /* for kprintf */
#include <synch.h>  /* for P(), V(), sem_* */
#include <thread.h> /* for thread_fork() */
#include <test.h>

#include "dining_driver.h"


#define THINKING 0
#define HUNGRY 1
#define EATING 2
//============================
#define LEFT(x) (x + NUM_PHILOSOPHERS - 1) % NUM_PHILOSOPHERS
#define RIGHT(x) (x + 1) % NUM_PHILOSOPHERS
/*
 * Declare any data structures you might need to synchronise 
 * your forks here.
 */
struct semaphore *mutex;
struct semaphore *s_phil[NUM_PHILOSOPHERS];  /* semaphore for each phil*/
int state[NUM_PHILOSOPHERS];
void test(unsigned long phil_num);


/*
 * Take forks ensures mutually exclusive access to two forks
 * associated with the philosopher.
 * 
 * The left fork number = phil_num
 * The right fork number = (phil_num + 1) % NUM_PHILOSPHERS
 */

void take_forks(unsigned long phil_num)
{
    // (void)phil_num;
    P(mutex);
    state[phil_num] = HUNGRY; // require to eat
    test(phil_num);
    V(mutex);
    P(s_phil[phil_num]);
}


/*
 * Put forks releases the mutually exclusive access to the
 * philosophers forks.
 */

void put_forks(unsigned long phil_num)
{
    // (void)phil_num;
    P(mutex);
    state[phil_num] = THINKING;
    test(LEFT(phil_num));
    test(RIGHT(phil_num));
    V(mutex);
}


/* 
 * Create gets called before the philosopher threads get started.
 * Insert any initialisation code you require here.
 */

void create_forks()
{
    mutex = sem_create("mutex", 1);
    if (mutex == NULL){
        panic("dining: mutex create fail\n");
    }
    for (int i = 0; i < NUM_PHILOSOPHERS; i++){
        s_phil[i] = sem_create("phil_sem", 0);
        if (s_phil[i] == NULL){
            panic("dining: semaphore create fail\n");
        }
        state[i] = THINKING;
    }
}


/*
 * Destroy gets called when the system is shutting down.
 * You should clean up whatever you allocated in create_forks()
 */

void destroy_forks()
{
    sem_destroy(mutex);
    for (int i = 0; i < NUM_PHILOSOPHERS; i++){
        sem_destroy(s_phil[i]);
    }
}

/*
 * Test gets called when a phil try to get access to a folk
 * It responsible for waking up a phil when folk is avaliable
 */

void test(unsigned long phil_num)
{
    if (state[phil_num] == HUNGRY && state[LEFT(phil_num)] != EATING && state[RIGHT(phil_num)]!=EATING){
        state[phil_num] = EATING; /* phil_num is eat*/
        V(s_phil[phil_num]);  /* Ok to wake up phil_num to eat*/
    }
}