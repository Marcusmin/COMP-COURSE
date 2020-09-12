// Weighted Directed Graph ADT
// Adjacency Matrix Representation ... COMP9024 18s2
#include "WGraph.h"
#include "stack.h"
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>

typedef struct GraphRep {
   int **edges;  // adjacency matrix storing positive weights
		 // 0 if nodes not adjacent
   int nV;       // #vertices
   int nE;       // #edges
} GraphRep;

Graph newGraph(int V) {
   assert(V >= 0);
   int i;

   Graph g = malloc(sizeof(GraphRep));
   assert(g != NULL);
   g->nV = V;
   g->nE = 0;

   // allocate memory for each row
   g->edges = malloc(V * sizeof(int *));
   assert(g->edges != NULL);
   // allocate memory for each column and initialise with 0
   for (i = 0; i < V; i++) {
      g->edges[i] = calloc(V, sizeof(int));
      assert(g->edges[i] != NULL);
   }

   return g;
}

int numOfVertices(Graph g) {
   return g->nV;
}

// check if vertex is valid in a graph
int validV(Graph g, Vertex v) {
   return (g != NULL && v >= 0 && v < g->nV);
   //delete:
}

void insertEdge(Graph g, Edge e) {
   assert(g != NULL && validV(g,e.v) && validV(g,e.w));

   if (g->edges[e.v][e.w] == 0) {   // edge e not in graph
      g->edges[e.v][e.w] = e.weight;
      g->nE++;
   }
}

void removeEdge(Graph g, Edge e) {
   assert(g != NULL && validV(g,e.v) && validV(g,e.w));

   if (g->edges[e.v][e.w] != 0) {   // edge e in graph
      g->edges[e.v][e.w] = 0;
      g->nE--;
   }
}

int adjacent(Graph g, Vertex v, Vertex w) {
   assert(g != NULL && validV(g,v) && validV(g,w));

   return g->edges[v][w];
}

void showGraph(Graph g) {
    assert(g != NULL);
    int i, j;

    printf("Number of vertices: %d\n", g->nV);
    printf("Number of edges: %d\n", g->nE);
    for (i = 0; i < g->nV; i++)
       for (j = 0; j < g->nV; j++)
	  if (g->edges[i][j] != 0)
	     printf("Edge %d - %d: %d\n", i, j, g->edges[i][j]);
}

void freeGraph(Graph g) {
   assert(g != NULL);

   int i;
   for (i = 0; i < g->nV; i++)
      free(g->edges[i]);
   free(g->edges);
   free(g);
}

void showPatialOrder(Graph g, int *divisors){
  int i, j;
  printf("Partial order:\n");
  for(i = 0; i < g->nV; i++){
    printf("%d: ", divisors[i]);
    for(j = 0; j < g->nV; j++){
      if(g->edges[i][j] != 0){
        printf("%d ", divisors[j]);
      }
    }
    printf("\n");
  }
}

/*
int allHasVisited(int *array, lengthOfArray){
  for(int i = 0; i < lengthOfArray; i++){
    if(array[i] != -1){
      return 0;
    }
  }
  return 1;
}
*/

Vertex findVertex(int *visited, int *dist, int length){
  int largestVertex = 0;
  for(int i = 0; i < length; i++){
    if(visited[i] != -1){
      continue;
    }else{
      if(dist[i] > dist[largestVertex]){
        largestVertex = i;
      }
    }
  }
  visited[largestVertex] = 0;
  //printf("Find vertex with largest weight: %d\n", largestVertex);
  return largestVertex;
}

void relax(Graph g, Vertex s, int *dist, int *pred, int length){
  //printf("Going to relax %d\n", s);
  for(int i = 0; i < length; i++){
    if(g->edges[s][i] != 0 && g->edges[s][i] + dist[s] > dist[i]){
      dist[i] = g->edges[s][i] + dist[s];
      pred[i] = s;
      //printf("%d's precessor is %d\n", i, s);
    }
  }
}

Vertex findMaxVertex(int *dist, int length){
  int max = 0;
  for(int i = 0; i < length; i++){
    if(dist[max] < dist[i]){
      max = i;
    }
  }
  return max;
}

stack findLongestPath(Graph g){
  //const int infinite = g->nV + 1; //the sum of longest path's weight cannot larger than the number of vertex in a graph
  Vertex s; //vertex which has smallest dist
  Vertex maxVertes;
  stack vertexStack = newStack();
  int i;

  if(g->nV == 1){
    StackPush(vertexStack, 0);
    return vertexStack;
  }
  int *dist = (int *)calloc(g->nV, sizeof(int));
  int *pred = (int *)calloc(g->nV, sizeof(int));
  int *visited = (int *)calloc(g->nV, sizeof(int));

  //Initialize dist
  for(i = 1; i < g->nV; i++){
    dist[i] = -1;
  }
  //Initialize visited list, the vertex which is not visited assigned to be -1
  for(i = 0; i < g->nV; i++){
    visited[i] = -1;
  }

  for(i = 0; i < g->nV; i++){
    s = findVertex(visited, dist, g->nV);
    relax(g, s, dist, pred, g->nV);
  }
  //
  for(int i = 0; i < g->nV; i++){
    //printf("%d  ", visited[i]);
  }
  printf("\n");
  maxVertes = findMaxVertex(dist, g->nV);
  for(i = maxVertes; pred[i] != 0; i = pred[i]){
    StackPush(vertexStack, i);
  }

  StackPush(vertexStack, i);
  StackPush(vertexStack, pred[i]);
  return vertexStack;
}