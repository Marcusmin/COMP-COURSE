// Linked list of transport card records implementation ... Assignment 1 COMP9024 18s2
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "cardLL.h"
#include "cardRecord.h"

// linked list node type
// DO NOT CHANGE
typedef struct node {
    cardRecordT data;
    struct node *next;
} NodeT;

// linked list type
typedef struct ListRep {
   NodeT *head;
   int nbOfMembers;
   float balance;
/* Add more fields if you wish */

} ListRep;

/*** Your code for stages 2 & 3 starts here ***/

// Time complexity: 
// Explanation: 
List newLL() {
   List newList;
   newList = (List)malloc(sizeof(ListRep));
   newList->head = NULL;
   newList->nbOfMembers = 0;
   newList->balance = 0.0;
   //printf("Initialized!");
   return newList;  /* needs to be replaced */
}

// Time complexity: 
// Explanation: 
void dropLL(List listp) {
   NodeT *p, *temp;
   p = listp->head;
   while(p != NULL){
      temp = p->next;
      free(p);
      p = temp;
   }
   return;  /* needs to be replaced */
}

// Time complexity: 
// Explanation: 
void removeLL(List listp, int cardID) {

   return;  /* needs to be replaced */
}

// Time complexity: 
// Explanation: 
//Finished Function, waiting for delete printf
void insertLL(List listp, int cardID, float amount) {
   NodeT *temp;
   NodeT *newNode;
   temp = listp->head;
   if(listp->head == NULL){
      newNode = (NodeT *)malloc(sizeof(NodeT));
      (newNode->data).cardID = cardID;
      (newNode->data).balance = amount;
      newNode->next = NULL;
      listp->nbOfMembers ++;
      listp->balance += amount;
      listp->head = newNode;
      printf("Card added.\n");
      return;
   }
   if(temp->data.cardID >= cardID){
      if(temp->data.cardID > cardID){
         newNode = (NodeT *)malloc(sizeof(NodeT));
         (newNode->data).cardID = cardID;
         (newNode->data).balance = amount;
         newNode->next = temp;
         listp->nbOfMembers ++;
         listp->balance += amount;
         listp->head = newNode;
         printf("Card added.\n");
      }
      else{
         listp->balance -= temp->data.balance;
         temp->data.balance = amount;
         listp->balance += amount;
         printCardData(temp->data);
      }
      return;
   }
   while(temp->next != NULL && temp->next->data.cardID < cardID){
      temp = temp->next;
   }
   if(temp->next == NULL && temp != listp->head){
      newNode = (NodeT *)malloc(sizeof(NodeT));
      (newNode->data).cardID = cardID;
      (newNode->data).balance = amount;
      newNode->next = NULL;
      listp->nbOfMembers ++;
      listp->balance += amount;
      temp->next = newNode;
      printf("Card added.\n");
   }
   else if(temp->next == NULL && temp == listp->head){
      if(temp->data.cardID == cardID){
         listp->balance -= temp->data.balance;
         temp->data.balance = amount;
         listp->balance += amount;
         printCardData(temp->data);      
      }
      else if(temp->data.cardID < cardID){
         newNode = (NodeT *)malloc(sizeof(NodeT));
         (newNode->data).cardID = cardID;
         (newNode->data).balance = amount;
         newNode->next = NULL;
         listp->nbOfMembers ++;
         listp->balance += amount;
         temp->next = newNode;
         printf("Card added.\n");
      }
   }
   else if(temp->next != NULL && temp->next->data.cardID == cardID){
      temp = temp->next;
      listp->balance -= temp->data.balance;
      temp->data.balance = amount;
      listp->balance += amount;
      printCardData(temp->data);
   }
   else if(temp->next != NULL && temp->next->data.cardID > cardID){
      newNode = (NodeT *)malloc(sizeof(NodeT));
      (newNode->data).cardID = cardID;
      (newNode->data).balance = amount;
      newNode->next = temp->next;
      temp->next = newNode;
      listp->nbOfMembers ++;
      listp->balance += amount;
      printf("Card added.\n");
   }
   return;  /* needs to be replaced */
}

// Time complexity: 
// Explanation: 
void getAverageLL(List listp, int *n, float *balance) {
   n = (int *)malloc(sizeof(int));
   balance = (float *)malloc(sizeof(float));
   *n = listp->nbOfMembers;
   *balance = listp->balance / listp->nbOfMembers;
   printf("Number of cards on file: %d\n", *n);
   printf("Average balance: $%.2f\n", *balance);
   return;  /* needs to be replaced */
}

// Time complexity: 
// Explanation: 
void showLL(List listp) {
   NodeT *p;
   if(listp == NULL){
      return;
   }
   p = listp->head;
   while(p != NULL){
      printCardData(p->data);
      p = p->next;
   }
   return;  /* needs to be replaced */
}
