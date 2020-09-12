#include <stdio.h>

void swap(char*, int i, int j);
void permutation(char* string, int n);

int count;

int main(){
    char string[] = "acdgot";
    
    permutation(string, 6);
    printf("%d\n",count);
    
    return 0;
    
}

void permutation(char* string, int n){
    int i;

    if(n == 1){
        printf("%s\n", string);
        ++count;
    }
    else{
        for(i = 0; i < n; i++){
            permutation(string, n - 1);
            if(n % 2 != 0){
                swap(string, 0, n - 1);
            }else{
                swap(string, i, n - 1);
            }
        }
    }
}

void swap(char s[], int i, int j){
    char temp;
    
    temp = s[i];
    s[i] = s[j];
    s[j] = temp;
}
