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

// Time complexity: O(1)
// Explanation: There is no loop in this function, the step of initialization is constant.
List newLL() {
   List newList;
   newList = (List)malloc(sizeof(ListRep));
   newList->head = NULL;
   newList->nbOfMembers = 0;
   newList->balance = 0.0;
   return newList;  /* needs to be replaced */
}

// Time complexity: O(n)
// Explanation: There is a loop which travels the whole linked list.
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

// Time complexity: O(n)
// Explanation: There is a loop to travel the whole linked list 
//for looking for the specified card id.
void removeLL(List listp, int cardID) {
   NodeT *temp, *beforeTemp;
   temp = listp->head;
   beforeTemp = temp;
   while(temp != NULL){
      if(temp->data.cardID == cardID){
         if(beforeTemp == temp && temp->next == NULL){
            free(temp);
            listp->head = NULL;
            listp->nbOfMembers--;
            listp->balance -= temp->data.balance;
            printf("Card removed.\n");
            return;
         }
         listp->nbOfMembers--;
         listp->balance -= temp->data.balance;
         beforeTemp->next = temp->next;
         free(temp);
         printf("Card removed.\n");
         return;
      }
      beforeTemp = temp;
      temp = temp->next;
   }
   printf("Card not found.\n");
   return;  /* needs to be replaced */
}

// Time complexity: O(n)
// Explanation: There is a loop to find a proper postion to insert data, the worst case is traveling
// traveling the whole linked list.
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
      else{ //Update new data
         listp->balance -= temp->data.balance;
         temp->data.balance = temp->data.balance + amount;
         listp->balance += temp->data.balance;
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
      if(temp->data.cardID == cardID){ //Update new data
         listp->balance -= temp->data.balance;
         temp->data.balance += amount;
         listp->balance += temp->data.balance;
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
   else if(temp->next != NULL && temp->next->data.cardID == cardID){ //Update new data
      temp = temp->next;
      listp->balance -= temp->data.balance;
      temp->data.balance = temp->data.balance + amount;
      listp->balance += temp->data.balance;
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

// Time complexity: O(1)
// Explanation: There is no loop in this function.
void getAverageLL(List listp, int *n, float *balance) {
   n = (int *)malloc(sizeof(int));
   balance = (float *)malloc(sizeof(float));
   if(listp->nbOfMembers == 0){
      printf("Number of cards on file: %d\n", 0);
      printf("Average balance: $%.2f\n", 0.0);
      return;  /* needs to be replaced */
   }
   *n = listp->nbOfMembers;
   *balance = listp->balance / listp->nbOfMembers;
   printf("Number of cards on file: %d\n", *n);
   printf("Average balance: $%.2f\n", *balance);
   return;  /* needs to be replaced */
}

// Time complexity: O(n)
// Explanation: There is a loop which travel the whole linked list.
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
