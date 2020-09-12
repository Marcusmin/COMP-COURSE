.class public test83
.super java/lang/Object
	
.field static a [I
.field static b [F
	
	; standard class static initializer 
.method static <clinit>()V
	
	bipush 10
	newarray int
	putstatic test83/a [I
	bipush 10
	newarray float
	putstatic test83/b [F
	
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
.var 1 is vc$ Ltest83; from L0 to L1
	new test83
	dup
	invokenonvirtual test83/<init>()V
	astore_1
	getstatic test83/a [I
	iconst_1
	getstatic test83/a [I
	iconst_2
	getstatic test83/a [I
	iconst_3
	iaload
	iadd
	iaload
	iadd
	iaload
	pop
	return
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 6
.end method
