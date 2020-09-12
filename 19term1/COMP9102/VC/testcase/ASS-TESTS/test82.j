.class public test82
.super java/lang/Object
	
.field static a [I
.field static b [F
	
	; standard class static initializer 
.method static <clinit>()V
	
	bipush 10
	newarray int
	putstatic test82/a [I
	bipush 10
	newarray float
	putstatic test82/b [F
	
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
.method f()I
L0:
.var 0 is this Ltest82; from L0 to L1
	iconst_1
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 1
.limit stack 1
.end method
.method g([I)F
L0:
.var 0 is this Ltest82; from L0 to L1
.var 1 is x [I from L0 to L1
	fconst_1
	freturn
L1:
	nop
	
	; set limits used by this method
.limit locals 2
.limit stack 1
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest82; from L0 to L1
	new test82
	dup
	invokenonvirtual test82/<init>()V
	astore_1
	aload_1
	invokevirtual test82/f()I
	i2f
	aload_1
	getstatic test82/a [I
	invokevirtual test82/g([I)F
	fdiv
	getstatic test82/a [I
	iconst_1
	iaload
	i2f
	getstatic test82/b [F
	iconst_2
	faload
	fadd
	fcmpg
	ifge L2
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
.limit stack 4
.end method
