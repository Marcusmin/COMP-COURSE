#define MAXLEN 100
typedef struct stack{
   int item[MAXLEN];
   int top;
}StackRep;

typedef StackRep* Stack;

Stack InitStack(void);
int StackIsEmpty(Stack stackp);
int StackPop(Stack stackp);
void StackPush(Stack stackp,int newItem);