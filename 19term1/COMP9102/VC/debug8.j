.class public debug8
.super java/lang/Object
	
.field static a [I
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_2
	newarray int
	putstatic debug8/a [I
	
	; set limits used by this method
.limit locals 0
.limit stack 1
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
.var 1 is vc$ Ldebug8; from L0 to L1
	new debug8
	dup
	invokenonvirtual debug8/<init>()V
	astore_1
	return
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
.method func()I
L0:
.var 0 is this Ldebug8; from L0 to L1
.var 1 is arr [I from L0 to L1
	iconst_3
	newarray int
	dup
	iconst_0
	iconst_1
	iastore
	dup
	iconst_1
	iconst_2
	iastore
	dup
	iconst_2
	iconst_3
	iastore
	astore_1
.var 2 is num I from L0 to L1
	iconst_2
	istore_2
	aload_1
	iconst_0
	iconst_1
	iadd
	bipush 8
	iastore
	aload_1
	iconst_0
	iload_2
	iadd
	bipush 9
	iastore
	aload_1
	iconst_0
	iaload
	istore_2
	iconst_0
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 3
.limit stack 4
.end method
