#define MAXITEMS 10
#include <stdio.h>
#include <assert.h>
typedef struct queue{
    int item[MAXITEMS];
    int head;
    int tail;
}QueueRep;

static QueueRep QueueObject;

void QueueInit(){
    QueueObject.head = 0;
    QueueObject.tail = -1;
}       // set up empty queue
int  QueueIsEmpty(){
    return QueueObject.tail == -1;
}     // check whether queue is empty

void QueueEnqueue(int num){
    int i;
    assert(QueueObject.tail < MAXITEMS - 1);
    i = QueueObject.tail + 1;
    QueueObject.item[i] = num;
    QueueObject.tail = i;
}  // insert int at end of queue
int  QueueDequeue(){
    assert(!QueueIsEmpty());
    int i, output;
    i = QueueObject.head;
    output = QueueObject.item[i];
    for(; i < QueueObject.tail; i ++){
        QueueObject.item[i] = QueueObject.item[i + 1];
    }
    QueueObject.tail --;
    return output;
}     // remove int from front of queue