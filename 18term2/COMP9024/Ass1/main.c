/**
     main.c

     Program supplied as a starting point for
     Assignment 1: Transport card manager

     COMP9024 18s2
**/
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <ctype.h>

#include "cardRecord.h"
#include "cardLL.h"

void printHelp();
void CardLinkedListProcessing();

int main(int argc, char *argv[]) {
   int nbOfRecords;
   int validID;
   float validAmount;
   cardRecordT *curRecords;
   float totalBalance = 0;
   float averageBalance;
   if (argc == 2) {
       nbOfRecords = atoi(argv[1]);
       curRecords = (cardRecordT *)malloc(sizeof(cardRecordT) * nbOfRecords);
       for(int i = 0; i < nbOfRecords; i++){
           printf("Enter card ID: ");
           while(!(validID = readValidID())){
               printf("Enter card ID: ");
           }
           curRecords[i].cardID = validID;
           printf("Enter amount: ");
           while((validAmount = readValidAmount()) == 999.0){
               printf("Enter amount: ");
           }
           curRecords[i].balance = validAmount;
       }
       for(int i = 0; i < nbOfRecords; i ++){
           printCardData(curRecords[i]);
           totalBalance += curRecords[i].balance;
       }
       averageBalance = totalBalance / nbOfRecords;
       printf("Number of cards on file: %d\n", nbOfRecords);
       if(averageBalance < 0.0){
           printf("Average balance: -$%.2f\n", -averageBalance);
       }
       else if(averageBalance == -0.0){
           printf("Average balance: $%.2f\n", averageBalance);
       }
       else{
           printf("Average balance: $%.2f\n", averageBalance);
       }
       free(curRecords);
      /*** Insert your code for stage 1 here ***/
      
   } else {
      CardLinkedListProcessing();
   }
   return 0;
}

/* Code for Stages 2 and 3 starts here */

void CardLinkedListProcessing() {
   int op, ch;
   int cID, *totalMembers = NULL;
   int cAmount;
   float *aveBalance = NULL;
   List list = newLL();   // create a new linked list
   
   while (1) {
      printf("Enter command (a,g,p,q,r, h for Help)> ");

      do {
	 ch = getchar();
      } while (!isalpha(ch) && ch != '\n');  // isalpha() defined in ctype.h
      op = ch;
      // skip the rest of the line until newline is encountered
      while (ch != '\n') {
	 ch = getchar();
      }

      switch (op) {

         case 'a':
         case 'A':
         printf("Enter card ID: ");
         while(!(cID = readValidID())){
            printf("Enter card ID: ");
         }
         printf("Enter amount: ");
         while((cAmount = readValidAmount()) == 999.0){
            printf("Enter amount: ");
         }
         insertLL(list, cID, cAmount);
            /*** Insert your code for adding a card record ***/

	    break;

         case 'g':
         case 'G':
            /*** Insert your code for getting average balance ***/
            getAverageLL(list, totalMembers, aveBalance);
	    break;
	    
         case 'h':
         case 'H':
            printHelp();
	    break;
	    
         case 'p':
         case 'P':
            /*** Insert your code for printing all card records ***/
         showLL(list);
	    break;

         case 'r':
         case 'R':
            /*** Insert your code for removing a card record ***/
         
	    break;

	 case 'q':
         case 'Q':
            dropLL(list);       // destroy linked list before returning
	    printf("Bye.\n");
	    return;
      }
   }
}

void printHelp() {
   printf("\n");
   printf(" a - Add card record\n" );
   printf(" g - Get average balance\n" );
   printf(" h - Help\n");
   printf(" p - Print all records\n" );
   printf(" r - Remove card\n");
   printf(" q - Quit\n");
   printf("\n");
}
