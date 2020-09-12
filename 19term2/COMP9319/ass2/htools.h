// macro for getting bit
#ifndef ICHAR
#define ICHAR(i) (i/8)
#endif

#ifndef IBIT
#define IBIT(i) (i%8)
#endif

#ifndef BITLIST
#define BITLIST char*
#endif

#ifndef GETBIT
// trade a char array as a array of bit
// endian is from left to right
#define GETBIT(b, i) (((b)>>(7-i))&1)
#endif

#ifndef GETCHARBIT
// get the particular bit in a array of bits
#define GETCHARBIT(b, i) ((b[ICHAR(i)]>>(7-IBIT(i)))&1)
#endif


// set a bit in byte
#ifndef SETBIT
#define SETBIT(b, i) ((b)|1<<(7-i))
#endif

#ifndef CLEAR_BIT
#define CLEAR_BIT(b, i) ((b)&(~(1<<(7-i))))
#endif

#ifndef SETCHARBIT
#define SETCHARBIT(b, i) ((b[ICHAR(i)])|(1 << 7-IBIT(i)))
#endif

#ifndef BITLISTDEFINED
#define BITLISTDEFINED
typedef struct bitlist {
    BITLIST bits;
    int bitlen;
} BLIST, *PBLIST;
#endif

#ifndef ASCIILEN
#define ASCIILEN 256
#endif

#ifndef UINT
#define UINT unsigned int
#endif

#ifndef COUNT_BIT
// Dont know the concept...
    #ifndef COUNT_BIT_A
    #define COUNT_BIT_A(x) (((x)&0b01010101)+(((x)>>1)&0b01010101))
    #endif

    #ifndef COUNT_BIT_B
    #define COUNT_BIT_B(x) ((COUNT_BIT_A(x)&0b00110011) + ((COUNT_BIT_A(x)>>2)&0b00110011))
    #endif

    #ifndef COUNT_BIT
    #define COUNT_BIT(x) ((COUNT_BIT_B(x)&0b00001111)+((COUNT_BIT_B(x)>>4)&0b00001111))
    #endif
#endif

#define MAX_S_LEN (1024*1024*1)

#ifndef BLOCK_SIZE_STRING
    // #define BLOCK_SIZE_STRING (44000)
    #define BLOCK_SIZE_STRING (2500)
    // #define BLOCK_SIZE_STRING (2)
#endif

#ifndef BLOCK_SIZE_BIT
    #define BLOCK_SIZE_BIT (100)
    // #define BLOCK_SIZE_BIT (1)
#endif

#ifndef INDEX_SIZE_STRING
    // #define INDEX_SIZE_STRING (4096)
    // #define INDEX_SIZE_STRING (18400)
    // #define INDEX_SIZE_STRING (4)
    #define INDEX_SIZE_STRING (25400)
#endif

#ifndef INDEX_SIZE_BIT
    #define INDEX_SIZE_BIT (200288)
    // #define INDEX_SIZE_BIT (6)
#endif