.class public test58
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
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest58; from L0 to L1
	new test58
	dup
	invokenonvirtual test58/<init>()V
	astore_1
	iconst_1
	i2f
	fconst_2
	fcmpg
	ifeq L2
	iconst_0
	goto L3
L2:
	iconst_1
L3:
	pop
	return
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
