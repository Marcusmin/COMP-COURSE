#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include "WGraph.h"
#include "stack.h"

//Find divisors of nums, return a pointer to an array of its divisors
int compfunc(const void * a, const void * b){
	return (*(int *)a - *(int *)b);
}
/*
Time complexity:
first step: split a number into digits, O(n)
second step: sort digits using qsort, O(nlogn)
third step: check if a constain b, using two for loop, O(n**2)
the whole function's time complexity should be O(n ** 2)
*/
int isAContainB(int A, int B){
	int *digitsOfA = (int *)calloc(10, sizeof(int));
	int *digitsOfB = (int *)calloc(10, sizeof(int));
	int i, j;
	for(i = 0; A / 10; i++, A = A / 10){
		digitsOfA[i] = A % 10;
	}
	digitsOfA[i] = A;
	for(j = 0; B / 10; j++, B = B / 10){
		digitsOfB[j] = B % 10;
	}
	digitsOfB[j] = B;
	qsort(digitsOfA, i + 1, sizeof(int), compfunc);
	qsort(digitsOfB, j + 1, sizeof(int), compfunc);
	for(int a = 0, b = 0 ; b <= j; b++){
		while(a <= i && digitsOfA[a] < digitsOfB[b]){
			a ++;
		}
		if(a > i || digitsOfA[a] > digitsOfB[b]){
			free(digitsOfA);
			free(digitsOfB);
			return 0;
		}
	}
	free(digitsOfA);
	free(digitsOfB);
	return 1;
}
/*
find all divisors of a number, sort them before output the result
find divisors using O(n)
quick sort's time complexity is O(nlogn)
time complexity is O(nlogn)
*/
int *findDivisor(int num){
	int i;
	int k = 0;
	int squareRoot = sqrt(num);
	int *divisors = NULL;
	int *nums = (int *)calloc(num + 1, sizeof(int));
	assert(nums != NULL);
	if(num == 1){
		divisors = (int *)calloc(2, sizeof(int));
		assert(divisors != NULL);
		divisors[0] = 1;
		divisors[1] = 0;
		free(nums);
		return divisors;
	}
	for(i = 1; i <= squareRoot; i++){
		if(num % i == 0){	//i is a divisor of num
			if(num / i == i){
				nums[k++] = i;
			}else{
				nums[k++] = i;
				nums[k++] = num/i;
			}
		}
	}
	divisors = (int *)calloc(k + 1, sizeof(int));
	assert(divisors != NULL);
	for(i = 0; nums[i] != 0; i++){
		divisors[i] = nums[i];
	}
	qsort(divisors, i, sizeof(int), compfunc);
	free(nums);
	return divisors;
}
/*
find out the length of an array of divisors which terminated by a 0
time complexity is O(n)
*/
int nbOfDivisor(int *divisorPtr){
	int i;
	for(i = 0; divisorPtr[i] != 0; i++);
	return i;
}
/*
input a number and return a partial order graph
*/
Graph createGraph(int num){
	Graph g;
	int *divisors = findDivisor(num);
	int nV = nbOfDivisor(divisors);
	g = newGraph(nV);
	free(divisors);
	return g;
}
/*
build the graph recursively, the max number of divisors of a number should be no larger than
the square root of that number. Time complexity is (n**(3/2))
*/
void buildGaph(Graph graphOfPartialOrder, int num,  int *dict){
	int *divisors = findDivisor(num);
	int nbOfD = nbOfDivisor(divisors);
	int indexOfNum = 0;
	int indexOfDivisor = 0;
	Edge edge;

	for(int i = 0; i < nbOfDivisor(dict); i++){
		if(dict[i] == num){
			indexOfNum = i;
			break;
		}
	}
	if (nbOfDivisor(divisors) == 1){	//if a number's divisors only have itself
		free(divisors);
		return;
	}else{	//otherwise, build graph by partial order
		for(int i = 0; i < nbOfD; i++){
			if(divisors[i] != num){	//for every divisor of number except itself
				//find out the index in dict
				if(isAContainB(num, divisors[i])){
					for(int j = 0; j < nbOfDivisor(dict); j++){
						if(divisors[i] == dict[j]){
							indexOfDivisor = j;
							break;
						}
					}
					edge.v = indexOfDivisor;
					edge.w = indexOfNum;
					edge.weight = 1;	//Create a direction edge, divisor points to dividend
					insertEdge(graphOfPartialOrder, edge);
					assert(dict[edge.w] % dict[edge.v] == 0);
				}
				buildGaph(graphOfPartialOrder, divisors[i], dict);
			}
		}
	}
	free(divisors);
}

/*There are two loop in this function, outer loop's length is the depth of DFS stack
the inner loop's length is the depth of common stack
the time complexity should be O(n**2)
*/
int main(int argc, char *argv[]){
	int num = atoi(argv[1]);
	Graph graphOfPartialOrder;
	int *divisors = findDivisor(num);
	stack VertexStack;
	DFSstack longestPaths;

	graphOfPartialOrder = createGraph(num);
	buildGaph(graphOfPartialOrder, num, divisors);	//Find divisors of input number
	//showGraph(graphOfPartialOrder);
	showPatialOrder(graphOfPartialOrder, divisors);	//print out the graph
	longestPaths = FloydFindLongestPath(graphOfPartialOrder);
	printf("Longest monotonically increasing sequences:\n");
	stack temp = newStack();
	while(!DFSStackIsEmpty(longestPaths)){
		VertexStack = DFSStackPop(longestPaths); // pop out a path
		//reverse the order
		while(!StackIsEmpty(VertexStack)){
			int node = StackPop(VertexStack);
			StackPush(temp, node);
		}
		dropStack(VertexStack);
		//show the path
		while(!StackIsEmpty(temp)){
			if(heightStack(temp) == 1){
				break;
			}
			printf("%d ", divisors[StackPop(temp)]);
			printf("< ");
		}
		printf("%d\n", divisors[StackPop(temp)]);
		//after stack is empty, free stack
	}
	dropStack(temp);
	//drop DFS's stack
	dropDFSStack(longestPaths);
	//free memory which is in heap
	freeGraph(graphOfPartialOrder);	//free graph
	free(divisors);//free divisors

	return 0;
}
