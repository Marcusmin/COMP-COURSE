// Weighted Directed Graph ADT
// Adjacency Matrix Representation ... COMP9024 18s2
#include "WGraph.h"
#include "stack.h"
#include "queue.h"
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
/*
input a graph, print out the graph
this function traverse the whole graph and print out the partial orders
which means the time complexity is O(n**2)
*/
void showPatialOrder(Graph g, int *divisors){
  int i, j;
  printf("Partial order:\n");
  for(i = 0; i < g->nV; i++){
    printf("%d: ", divisors[i]);
    for(j = 0; j < g->nV; j++){
      if(g->edges[i][j] == 1){
        printf("%d ", divisors[j]);
      }
    }
    printf("\n");
  }
  printf("\n");
}

/*
In order to find the vertex who has maxinum dist[] value
traverse this array, time complexity is O(n)
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
  return largestVertex;
}

/*
souce->destination
traverse the whole graph to relax, so the time complexity is O(V**2)
*/
void relax(Graph g, Vertex s, int *dist, int *pred, int length){
  for(int i = 0; i < length; i++){
    if(g->edges[s][i] != 0 && g->edges[s][i] + dist[s] > dist[i]){
      dist[i] = g->edges[s][i] + dist[s];
      pred[i] = s;
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

/*
This function was used in stage two, Dijkstra algorithm
Each edge is considered once to be relax
outer loop is V
inner loop to find vertex is also V
so the time complexity is O(V**2)
*/
stack findLongestPath(Graph g){
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
  printf("\n");
  maxVertes = findMaxVertex(dist, g->nV);
  for(i = maxVertes; pred[i] != 0; i = pred[i]){
    StackPush(vertexStack, i);
  }

  StackPush(vertexStack, i);
  StackPush(vertexStack, pred[i]);
  return vertexStack;
}

//Only for debug
int GetParticularEdge(Graph g, int i, int j){
  return g->edges[i][j];
}



//store the path in queue
DFSstack findPath(stack **path, Vertex start, Vertex end, int nV){
  stack **copyOfPath;
  DFSstack pathStack = newDFSStack();  //create a stack which can store a stack
  stack Apath = newStack(); //create a new int of stack store the path
  stack temp = NULL;
  DFSstack result = newDFSStack(); // store the final result of DFS
  int node;
  // DFSstack trashBin = newDFSStack();

  //make a copy of "path"
  copyOfPath = (stack **)malloc(nV * sizeof(stack *)); // this could be a problem
  for(int i = 0; i < nV; i++){
    copyOfPath[i] = (stack *)malloc(nV * sizeof(stack));
  }
  for(int i = 0; i < nV; i++){
    for(int j = 0; j < nV; j++){
      if(path[i][j] != NULL){
      stack temp = CopyStack(path[i][j]);
      copyOfPath[i][j] = temp;
      }else{
        copyOfPath[i][j] = NULL;
      }

    }
  }
  StackPush(Apath, start);
  DFSStackPush(pathStack, Apath);//push this path into stack
  while(!DFSStackIsEmpty(pathStack)){ //pop out every path stored in pathStack
    temp = DFSStackPop(pathStack);
    node = StackPop(temp);  //pop the end of the path which guide to next node
    StackPush(temp, node);  //keep the last node of current path
    stack newCopyOfPath = NULL;
    if(copyOfPath[node][end] != NULL){
      newCopyOfPath = CopyStack(copyOfPath[node][end]);
    }
    while(copyOfPath[node][end] != NULL && !StackIsEmpty(newCopyOfPath)){  //extend the path
      int nextNode = StackPop(newCopyOfPath);
      //create copy of temp
      stack copyOfTemp = CopyStack(temp);  //a copy of original stack
      StackPush(copyOfTemp, nextNode);  //extend the path
      DFSStackPush(pathStack, copyOfTemp); // push the new path to path stack
    }
    if(copyOfPath[node][end] == NULL){
      DFSStackPush(result, temp);
    }
    if(newCopyOfPath != NULL){
      dropStack(newCopyOfPath);
    }
  }
  for(int i = 0; i < nV; i++){
    for(int j = 0;j < nV; j++){
      if(copyOfPath[i][j] != NULL){
        dropStack(copyOfPath[i][j]);
      }
    }
    free(copyOfPath[i]);
  }
  free(copyOfPath);
  dropDFSStack(pathStack);
  dropStack(Apath);
  // dropDFSStack(trashBin);
  return result;
}

/*input a graph, return an array of longest path
Floyd algorithm:O(V**3)
*/
DFSstack FloydFindLongestPath(Graph g){
  int maxNode = 0;
  queue beginner = newQueue();
  queue last = newQueue();
  DFSstack finalResult = newDFSStack();

  const int nV = numOfVertices(g);
  Vertex dist[nV][nV];
  stack **path = (stack **)malloc(nV * sizeof(stack *)); // this could be a problem
  for(int i = 0; i < nV; i++){
    path[i] = (stack *)malloc(nV * sizeof(stack));
  }
  //Initalize dist
  for(int i = 0; i < g->nV; i++){
    for(int j = 0; j < g->nV; j++){
      if(i == j){ //for each dist[i][j] i == j, 
        dist[i][j] = -1;
      }else if(g->edges[i][j] == 1){  //for each i->j belonging to graph
        dist[i][j] = 1;
      }else{
        dist[i][j] = -1;
      }
    }
  }
  //Initialize path
  for(int i = 0; i < g->nV; i++){ //there would be multiple longest path
    for(int j = 0; j < g->nV; j++){
      if(g->edges[i][j] != 0){
        path[i][j] = newStack();
        assert(path[i][j] != NULL);
        StackPush(path[i][j], j);
      }else{
        path[i][j] = NULL;
      }
    }
  }

  for(int i = 0; i < g->nV; i++){
    for(int j = 0; j < g->nV; j++){
      for(int k = 0; k < g->nV; k++){
        if(dist[j][i] != -1 && dist[i][k] != -1 && dist[j][k] != -1 && dist[j][i] + dist[i][k] > dist[j][k]){ //if the distance is longer
          dist[j][k] = dist[j][i] + dist[i][k];
          assert(path[j][k] != NULL);
          assert(path[j][i] != NULL);
          //populate path[j][k] with value in path[j][i]
          dropStack(path[j][k]);
          path[j][k] = CopyStack(path[j][i]);
        }else if(dist[j][i] != -1 && dist[i][k] != -1 && dist[j][k] != -1 && dist[j][i] + dist[i][k] == dist[j][k]){
          assert(path[j][k]!=NULL);
          assert(path[j][i] != NULL);
          stack temp = CopyStack(path[j][i]);
          while(! StackIsEmpty(temp)){
            int node = StackPop(temp);
            if(! isInStack(path[j][k], node)){
              StackPush(path[j][k], node);
            }
          }
          dropStack(temp);
        }
      }
    }
  }
  //find longest path via path array
  //find the start node of the longest path
  for(int i = 0; i < g->nV; i++){
    for(int j = 0; j < g->nV; j++){
      if(maxNode < dist[i][j]){
        maxNode = dist[i][j];
      }
    }
  }
  for(int i = 0; i < g->nV; i++){
    for(int j = 0; j < g->nV; j++){
      if(maxNode == dist[i][j]){
        QueueEnqueue(beginner, i);
        QueueEnqueue(last, j);
      }
    }
  }
  if(maxNode == 0){
    dropQueue(last);
    dropQueue(beginner);
    DFSstack medium = newDFSStack();
    for(int i = 0; i < g->nV; i++){
      stack onlyOneNodePath = newStack();
      StackPush(onlyOneNodePath, i);
      DFSStackPush(medium, onlyOneNodePath);
    }
    while(! DFSStackIsEmpty(medium)){
      DFSStackPush(finalResult, DFSStackPop(medium));
    }
    //free medium
    dropDFSStack(medium);
    //free path
    for(int i = 0; i < nV; i++){
      for(int j = 0; j < nV; j++){
        if (path[i][j] != NULL){
          dropStack(path[i][j]);
        }
      }
      free(path[i]);
    }
    free(path);
    return finalResult;
  }
  while(!QueueIsEmpty(beginner)){
    DFSstack medium = newDFSStack();
    int start = QueueDequeue(beginner);
    int end = QueueDequeue(last);
    DFSstack longestPaths = findPath((stack **)path, start, end, g->nV);// start could be multiple
    while(!DFSStackIsEmpty(longestPaths)){
      stack temp = DFSStackPop(longestPaths);
      DFSStackPush(medium, temp);
    }
    //reverse medium
    while(! DFSStackIsEmpty(medium)){
      DFSStackPush(finalResult, DFSStackPop(medium));
    }
    dropDFSStack(longestPaths);
    dropDFSStack(medium);
  }
  dropQueue(beginner);
  dropQueue(last);
  DFSstack reversor = newDFSStack();
  while(! DFSStackIsEmpty(finalResult)){
    DFSStackPush(reversor, DFSStackPop(finalResult));
  }
  dropDFSStack(finalResult);//drop is not completed!
  finalResult = reversor;
  //free path
  for(int i = 0; i < nV; i++){
    for(int j = 0; j < nV; j++){
      if (path[i][j] != NULL){
        dropStack(path[i][j]);
      }
    }
    free(path[i]);
  }
  free(path);
  return finalResult;
}