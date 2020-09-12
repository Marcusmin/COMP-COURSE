/* This file will contain your solution. Modify it as you wish. */
#include <types.h>
#include <lib.h>
#include <synch.h>
#include "producerconsumer_driver.h"

/* Declare any variables you need here to keep track of and
   synchronise your bounded. A sample declaration of a buffer is shown
   below. It is an array of pointers to items.
   
   You can change this if you choose another implementation. 
   However, you should not have a buffer bigger than BUFFER_SIZE 
*/

data_item_t * item_buffer[BUFFER_SIZE];
int producer_count;
int consumer_count;

struct semaphore *empty;
struct semaphore *full;
struct semaphore *mutex;
/* consumer_receive() is called by a consumer to request more data. It
   should block on a sync primitive if no data is available in your
   buffer. */

data_item_t * consumer_receive(void)
{
        data_item_t * item;


        /*****************
         * Remove everything just below when you start.
         * The following code is just to trick the compiler to compile
         * the incomplete initial code 
         ****************/
        P(full);
        P(mutex);       //enter critical region
        item = item_buffer[consumer_count];
        consumer_count = (consumer_count + 1) % BUFFER_SIZE;
        V(mutex);       //leave critical region
        V(empty);

        /******************
         * Remove above here
         */

        return item;
}

/* procucer_send() is called by a producer to store data in your
   bounded buffer. */

void producer_send(data_item_t *item)
{
        KASSERT(item != NULL);
        P(empty);
        P(mutex);
        item_buffer[producer_count] = item;
        producer_count = (producer_count + 1) % BUFFER_SIZE;
        V(mutex);
        V(full);
}




/* Perform any initialisation (e.g. of global data) you need
   here. Note: You can panic if any allocation fails during setup */

void producerconsumer_startup(void)
{
        producer_count = 0;
        consumer_count = 0;
        empty = sem_create("empty", BUFFER_SIZE);
        if (empty == NULL){
                panic("empty: sem create fail\n");
        }
        full = sem_create("full", 0);
        if (full == NULL){
                panic("full: sem create fail\n");
        }
        mutex = sem_create("mutex", 1);
        if(mutex == NULL){
                panic("mutex: sem create fail\n");
        }
}

/* Perform any clean-up you need here */
void producerconsumer_shutdown(void)
{
        sem_destroy(empty);
        sem_destroy(full);
        sem_destroy(mutex);
}

