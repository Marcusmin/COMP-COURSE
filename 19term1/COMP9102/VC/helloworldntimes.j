.class public helloworldntimes
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
.method helloworld(I)I
L0:
.var 0 is this Lhelloworldntimes; from L0 to L1
.var 1 is n I from L0 to L1
.var 2 is i I from L0 to L1
	iconst_0
	istore_2
L2:
	iload_2
	iload_1
	if_icmplt L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L3
L6:
	ldc "Hello World"
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	iload_2
	iconst_1
	iadd
	istore_2
L7:
	goto L2
L3:
	iconst_0
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 3
.limit stack 2
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Lhelloworldntimes; from L0 to L1
	new helloworldntimes
	dup
	invokenonvirtual helloworldntimes/<init>()V
	astore_1
	aload_1
	iconst_5
	invokevirtual helloworldntimes/helloworld(I)I
	pop
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
