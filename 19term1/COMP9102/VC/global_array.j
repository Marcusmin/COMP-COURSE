.class public global_array
.super java/lang/Object
	
.field static a [I
	
	; standard class static initializer 
.method static <clinit>()V
	
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
	putstatic global_array/a [I
	
	; set limits used by this method
.limit locals 0
.limit stack 4
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
.var 1 is vc$ Lglobal_array; from L0 to L1
	new global_array
	dup
	invokenonvirtual global_array/<init>()V
	astore_1
	return
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 2
.end method
