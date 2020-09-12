.class public test32
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
.var 0 is this Ltest32; from L0 to L1
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
.var 1 is vc$ Ltest32; from L0 to L1
	new test32
	dup
	invokenonvirtual test32/<init>()V
	astore_1
.var 2 is i I from L0 to L1
	aload_1
	invokevirtual test32/f()I
	istore_2
	return
L1:
	return
	
	; set limits used by this method
.limit locals 3
.limit stack 2
.end method
