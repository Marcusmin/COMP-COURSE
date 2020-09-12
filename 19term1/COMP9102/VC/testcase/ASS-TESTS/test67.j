.class public test67
.super java/lang/Object
	
.field static i I
.field static x F
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_0
	putstatic test67/i I
	fconst_0
	putstatic test67/x F
	
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
.var 0 is this Ltest67; from L0 to L1
	iconst_1
	ireturn
L1:
	nop
	
	; set limits used by this method
.limit locals 1
.limit stack 1
.end method
.method g()F
L0:
.var 0 is this Ltest67; from L0 to L1
	fconst_1
	freturn
L1:
	nop
	
	; set limits used by this method
.limit locals 1
.limit stack 1
.end method
.method public static main([Ljava/lang/String;)V
L0:
.var 0 is argv [Ljava/lang/String; from L0 to L1
.var 1 is vc$ Ltest67; from L0 to L1
	new test67
	dup
	invokenonvirtual test67/<init>()V
	astore_1
	getstatic test67/i I
	i2f
	getstatic test67/x F
	aload_1
	invokevirtual test67/f()I
	i2f
	fmul
	aload_1
	invokevirtual test67/g()F
	fdiv
	fadd
	putstatic test67/x F
	return
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 3
.end method
