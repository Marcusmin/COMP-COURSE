.class public test14
.super java/lang/Object
	
	
	; standard class static initializer 
.method static <clinit>()V
	
	
	; set limits used by this method
.limit locals 0
.limit stack 0
	return
.end method
	
	; standard constructor initializer 
.method public <init>()V
.limit stack 1
.limit locals 1
	aload_0
	invokespecial java/lang/Object/<init>()V
	return
.end method
.method f(I[FZ)I
L0:
.var 0 is this Ltest14; from L0 to L1
.var 1 is i I from L0 to L1
.var 2 is f [F from L0 to L1
.var 3 is b Z from L0 to L1
	iconst_1
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 4
.limit stack 1
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest14; from L0 to L1
	new test14
	dup
	invokenonvirtual test14/<init>()V
	astore_1
.var 2 is a [F from L0 to L1
	bipush 10
	newarray float
	astore_2
	aload_1
	iconst_1
	aload_2
	iconst_1
	invokevirtual test14/f(I[FZ)I
	pop
	return
L1:
	return
	
	; set limits used by this method
.limit locals 3
.limit stack 4
.end method
