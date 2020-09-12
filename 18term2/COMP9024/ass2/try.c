#include <stdio.h>
#include <stdlib.h>
int main(int argc, char const *argv[])
{
	int *a = (int *)malloc(sizeof(int) * 20);
	printf("%d\n", sizeof(a));
	return 0;
}