#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
typedef struct nodeType{
    int data;
    struct nodeType *next;
}NodeT;
NodeT *makeNode(int v) {
    NodeT *new = malloc(sizeof(NodeT));
    assert(new != NULL);
    new->data = v;       // initialise data
    new->next = NULL;    // initialise link to next node
    return new;          // return pointer to new node
}

void showLL(NodeT *list) {
    NodeT *p;
    if(list == NULL){
        return;
    }
    for (p = list; p->next != NULL; p = p->next)
        printf("%d->", p->data);
    printf("%d\n",p->data);
}

void freeLL(NodeT *list) {
    NodeT *p;
    
    p = list;
    while (p != NULL) {
        NodeT *temp = p->next;
        free(p);
        p = temp;
    }
}

NodeT *joinLL(NodeT *head1, NodeT *head2){
    NodeT *p;
    
    if(head1 == NULL){
        head1 = head2;
        return head1;
    }
    p = head1;
    while(p->next != NULL){
        p = p->next;
    }
    p->next = head2;
    return head1;
}

int is_integer(char* input){
    int i;
    
    if(input[0] == '-'){
        if(input[1] == '\0'){
            return 0;
        }
        for(i = 1; input[i] != '\0'; i++){
            if(input[i] <'0' || input[i] > '9'){
                return 0;
            }
        }
        return 1;
    }
    for(i = 0; input[i] != '\0'; i++){
        if(input[i] <'0' || input[i] > '9'){
            return 0;
        }
    }
    return 1;
}
int str_to_int(char* input){
    int i;
    int output = 0;
    if(input[0] == '-'){
        for(i = 1; input[i] != '\0'; i++){
            output += input[i] - '0';
            output *= 10;
        }
        return -output / 10;
    }
    for(i = 0; input[i] != '\0'; i++){
        output += input[i] - '0';
        output *= 10;
    }
    return output / 10;
}
int main(void){
    NodeT *list = NULL;
    char input[100] = {'\0'};;
    char ch;
    int i = 0;
    
    do{
        for(i = 0;i < 100; i++){
            input[i] = '\0';
        }
        printf("Enter an integer: ");
        i = 0;
        while((ch = getchar())!=EOF && ch != '\n' && i < 100){
            input[i] = ch;
            i ++;
        }
        if(i == 100){
            printf("Number is Too Large");
            return 0;
        }
        if(is_integer(input)){
            list = joinLL(list, makeNode(str_to_int(input)));
        }
        else{
            break;
        }
    } while (is_integer(input));
    
    printf("Finished. \n");
    
    if(list != NULL){
        printf("List is ");
        showLL(list);
    }
        
}