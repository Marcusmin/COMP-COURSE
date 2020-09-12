.class public test
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
.method Iamtrue()Z
L0:
.var 0 is this Ltest; from L0 to L1
	iconst_1
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 1
.limit stack 1
.end method
.method Iamfalse()Z
L0:
.var 0 is this Ltest; from L0 to L1
	ldc "Noooooooooh"
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
	iconst_0
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
.var 1 is vc$ Ltest; from L0 to L1
	new test
	dup
	invokenonvirtual test/<init>()V
	astore_1
.var 2 is j I from L0 to L1
	iconst_0
	istore_2
.var 3 is i I from L0 to L1
	iconst_0
	istore_3
L2:
	iload_3
	bipush 10
	if_icmplt L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L3
L6:
	iload_2
	iconst_1
	iadd
	istore_2
	ldc "Hello World"
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
L7:
	iload_3
	iconst_1
	iadd
	istore_3
	goto L2
L3:
	iconst_1
	ifne L10
	aload_1
	invokevirtual test/Iamfalse()Z
	ifne L10
	iconst_0
	goto L11
L10:
	iconst_1
L11:
	ifeq L8
L12:
	ldc "False"
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
L13:
	goto L9
L8:
L14:
	ldc "True"
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
L15:
L9:
	iconst_0
	ifeq L18
	aload_1
	invokevirtual test/Iamfalse()Z
	ifeq L18
	iconst_1
	goto L19
L18:
	iconst_0
L19:
	ifeq L16
L20:
	ldc "Hi"
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
L21:
	goto L17
L16:
L22:
	ldc "Hello"
	invokestatic VC/lang/System/putStringLn(Ljava/lang/String;)V
L23:
L17:
L1:
	return
	
	; set limits used by this method
.limit locals 4
.limit stack 5
.end method
