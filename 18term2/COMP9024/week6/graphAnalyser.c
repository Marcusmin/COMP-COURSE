#include <stdio.h>
#include <stdlib.h>
#include "Graph.h"
#define MAXLENGTH 1000
#define NO_NUMBER -1
/*
 *  0 1
 1 2
 4 2
 1 3
 3 4
 1 5
 5 3
 2 3
 done
 
 */

//We assume the all input is valid
int readInt(void);
int main(void){
   int nbOfVertex;
   Graph graph = NULL;
   Edge e;
   Vertex v;
   Vertex *vertexArray;
   int minDegree = MAXLENGTH;
   int maxDegree = -1;
   int counter = 0;
   int i, j, k;
   //int triangles[10][3];
   //Set up a graph
   //Initialize a graph
   printf("Enter the number of vertices: ");
   graph = newGraph((nbOfVertex = readInt()));
   vertexArray = calloc(sizeof(Vertex), nbOfVertex);
   //Build up edges
   while(1){
      printf("Enter an edge (from): ");
      if((v = readInt()) == NO_NUMBER){
         printf("Finished.\n");
         break;
      }else{
         e.v = v;
      }
      printf("Enter an edge (to): ");
      if((v = readInt()) == NO_NUMBER){
         printf("Finished.\n");
         break;
      }else{
         e.w = v;
      }
      insertEdge(graph,e);
   }
   //Count the min and max degree
   for(i = 0; i < nbOfVertex; i++){
      counter = 0;
      for(j = 0; j < nbOfVertex; j++){
         if(j == i){
            continue;
         }else{
            if(adjacent(graph, i, j)){
               counter++;
            }
         }
      }
      vertexArray[i]  = counter;
      if(maxDegree < counter){
         maxDegree = counter;
      }
      if(minDegree > counter){
         minDegree = counter;
      }
   }
   printf("Minimum degree: %d\n", minDegree);
   printf("Maximum degree: %d\n", maxDegree);
   printf("Nodes of minimum degree:\n");
   //Replace this
   for(i = 0; i < nbOfVertex; i ++){
      if(vertexArray[i] == minDegree){
         printf("%d\n", i);
      }
   }
   //Replace this
   printf("Nodes of maximum degree:\n");
   for(i = 0; i < nbOfVertex; i ++){
      if(vertexArray[i] == maxDegree){
         printf("%d\n", i);
      }
   }
   //Looking for triangle
   printf("Triangles:\n");
   for(i = 0; i < nbOfVertex; i++){
      for(j = i; j < nbOfVertex; j++){
         if(i == j){
            continue;
         }
         for(k = j; k < nbOfVertex; k++){
            if(k == i || k == j){
               continue;
            }
            if(adjacent(graph, i, j) && adjacent(graph, j, k) && adjacent(graph, i, k)){
               printf("%d-%d-%d\n",i,j,k);
            }
         }
      }
   }
}
int readInt(void){
   int n;
   char line[MAXLENGTH];
   
   fgets(line, MAXLENGTH, stdin);
   if(sscanf(line, "%d", &n) == 1 && n >= 0){
      return n;
   }else{
      return NO_NUMBER;
   }
}
