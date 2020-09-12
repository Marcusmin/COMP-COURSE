.class public test33
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
.method f()I
L0:
.var 0 is this Ltest33; from L0 to L1
	iconst_1
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 1
.limit stack 1
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest33; from L0 to L1
	new test33
	dup
	invokenonvirtual test33/<init>()V
	astore_1
.var 2 is g F from L0 to L1
	aload_1
	invokevirtual test33/f()I
	i2f
	fstore_2
	return
L1:
	return
	
	; set limits used by this method
.limit locals 3
.limit stack 2
.end method
