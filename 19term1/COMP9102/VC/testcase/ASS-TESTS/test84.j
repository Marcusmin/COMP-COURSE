.class public test84
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
.var 1 is vc$ Ltest84; from L0 to L1
	new test84
	dup
	invokenonvirtual test84/<init>()V
	astore_1
.var 2 is n I from L0 to L1
.var 3 is i I from L0 to L1
.var 4 is current I from L0 to L1
.var 5 is next I from L0 to L1
.var 6 is twoaway I from L0 to L1
	ldc "How many Fibonacci numbers do you want to compute? "
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
	invokestatic VC/lang/System.getInt()I
	istore_2
	iload_2
	iconst_0
	if_icmple L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L2
	ldc "The number should be positive.
"
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
	goto L3
L2:
L6:
	ldc "

	I 	 Fibonacci(I) 
	=====================
"
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
	iconst_1
	dup
	istore 4
	istore 5
	iconst_1
	istore_3
L8:
	iload_3
	iload_2
	if_icmple L10
	iconst_0
	goto L11
L10:
	iconst_1
L11:
	ifeq L9
L12:
	ldc "	"
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
	iload_3
	invokestatic VC/lang/System.putInt(I)V
	ldc "	"
	invokestatic VC/lang/System/putString(Ljava/lang/String;)V
	iload 4
	invokestatic VC/lang/System/putIntLn(I)V
	iload 4
	iload 5
	iadd
	istore 6
	iload 5
	istore 4
	iload 6
	istore 5
L13:
	iload_3
	iconst_1
	iadd
	istore_3
	goto L8
L9:
L7:
L3:
	return
L1:
	return
	
	; set limits used by this method
.limit locals 7
.limit stack 2
.end method
