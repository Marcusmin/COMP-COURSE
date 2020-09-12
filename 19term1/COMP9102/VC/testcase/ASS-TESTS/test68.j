.class public test68
.super java/lang/Object
	
.field static i I
.field static x F
.field static b [Z
	
	; standard class static initializer 
.method static <clinit>()V
	
	iconst_0
	putstatic test68/i I
	fconst_0
	putstatic test68/x F
	iconst_2
	newarray boolean
	putstatic test68/b [Z
	
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
.var 0 is this Ltest68; from L0 to L1
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
.var 0 is this Ltest68; from L0 to L1
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
.var 0 is this Ltest68; from L0 to L1
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
.var 1 is vc$ Ltest68; from L0 to L1
	new test68
	dup
	invokenonvirtual test68/<init>()V
	astore_1
	getstatic test68/i I
	iconst_1
	if_icmpge L2
	iconst_0
	goto L3
L2:
	iconst_1
L3:
	aload_1
	invokevirtual test68/h()Z
	if_icmpne L4
	iconst_0
	goto L5
L4:
	iconst_1
L5:
	ifeq L6
	aload_1
	invokevirtual test68/f()I
	i2f
	aload_1
	invokevirtual test68/g()F
	fcmpg
	ifgt L8
	iconst_0
	goto L9
L8:
	iconst_1
L9:
	ifeq L6
	iconst_1
	goto L7
L6:
	iconst_0
L7:
	ifne L10
	getstatic test68/b [Z
	iconst_1
	baload
	ifne L10
	iconst_0
	goto L11
L10:
	iconst_1
L11:
	pop
	return
L1:
	return
	
	; set limits used by this method
.limit locals 2
.limit stack 5
.end method
