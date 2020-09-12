// Transport card record implementation ... Assignment 1 COMP9024 18s2
#include <stdio.h>
#include "cardRecord.h"

#define LINE_LENGTH 1024
#define NO_NUMBER -999

// scan input line for a positive integer, ignores the rest, returns NO_NUMBER if none
int readInt(void) {
   char line[LINE_LENGTH];
   int  n;

   fgets(line, LINE_LENGTH, stdin);
   if ( (sscanf(line, "%d", &n) != 1) || n <= 0 )
      return NO_NUMBER;
   else
      return n;
}

// scan input for a floating point number, ignores the rest, returns NO_NUMBER if none
float readFloat(void) {
   char  line[LINE_LENGTH];
   float f;

   fgets(line, LINE_LENGTH, stdin);
   if (sscanf(line, "%f", &f) != 1)
      return NO_NUMBER;
   else
      return f;
}

int readValidID(void) {
   int ID;
   ID = readInt();
   if(ID != NO_NUMBER){
       if(ID >= 10000000 && ID <= 999999999){
           return ID;
       }
       else{
           printf("Not valid. ");
           return 0;
       }
   }
   printf("Not valid. ");
   return 0;  /* needs to be replaced */
}

float readValidAmount(void) {
   float amount;
   amount = readFloat();
   if(amount != NO_NUMBER){
       if(amount >= -2.35 && amount <= 250.0){
           return amount;
       }
       printf("Not valid. ");
       return 999.0;
   }
   printf("Not valid. ");
   return 999.0;  /* needs to be replaced */
}

void printCardData(cardRecordT card) {
   printf("-----------------\n");
   printf("Card ID: %d\n", card.cardID);
   if(card.balance < 0.0){
       printf("Balance: -$%.2f\n", -card.balance);
   }
   else if(card.balance == -0.0){
       printf("Balance: $%.2f\n", -card.balance);
   }
   else{
       printf("Balance: $%.2f\n", card.balance);
   }
   if(card.balance < 5.00){
       printf("Low balance\n");
   }
   printf("-----------------\n");
   return;  /* needs to be replaced */
}
