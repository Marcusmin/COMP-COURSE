.class public test69
.super java/lang/Object
	
.field static i I
.field static x F
.field static b Z
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_0
	putstatic test69/i I
	fconst_0
	putstatic test69/x F
	iconst_0
	putstatic test69/b Z
	
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
.var 0 is this Ltest69; from L0 to L1
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
.var 0 is this Ltest69; from L0 to L1
	fconst_1
	freturn
L1:
	nop
	
	; set limits used by this method
.limit locals 1
.limit stack 1
.end method
.method h()Z
L0:
.var 0 is this Ltest69; from L0 to L1
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
.var 1 is vc$ Ltest69; from L0 to L1
	new test69
	dup
	invokenonvirtual test69/<init>()V
	astore_1
	getstatic test69/i I
	i2f
	aload_1
	invokevirtual test69/g()F
	fcmpg
	ifeq L2
	iconst_0
	goto L3
L2:
	iconst_1
L3:
	ifeq L4
	aload_1
	invokevirtual test69/f()I
	iconst_1
	if_icmpne L6
	iconst_0
	goto L7
L6:
	iconst_1
L7:
	ifne L8
	aload_1
	invokevirtual test69/h()Z
	ifeq L10
	aload_1
	invokevirtual test69/f()I
	i2f
	aload_1
	invokevirtual test69/g()F
	fadd
	aload_1
	invokevirtual test69/f()I
	i2f
	fcmpg
	ifge L12
	iconst_0
	goto L13
L12:
	iconst_1
L13:
	ifeq L10
	iconst_1
	goto L11
L10:
	iconst_0
L11:
	ifne L8
	iconst_0
	goto L9
L8:
	iconst_1
L9:
	ifeq L4
	iconst_1
	goto L5
L4:
	iconst_0
L5:
	pop
	return
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 7
.end method
