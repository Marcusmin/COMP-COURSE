;; Produced by JasminVisitor (JavaClass package)
;; http://www.inf.fu-berlin.de/~dahm/JavaClass/
;; Fri Apr 26 20:38:05 AEST 2019

.source test.java
.class public test
.super java/lang/Object

.field  i I
.field  j I
.field  a [I

.method public <init>()V
.limit stack 2
.limit locals 1
.var 0 is this Ltest; from Label0 to Label0

.line 1
	aload_0
	invokespecial java/lang/Object/<init>()V
.line 2
	aload_0
	iconst_3
	putfield test.i I
.line 3
	aload_0
	iconst_5
	putfield test.j I
.line 4
	aload_0
	iconst_2
	newarray int
	putfield test.a [I
Label0:
	return

.end method

.method public test()V
.limit stack 4
.limit locals 4
.var 0 is this Ltest; from Label0 to Label0

.line 6
	iconst_2
	newarray int
	dup
	iconst_0
	bipush 99
	iastore
	dup
	iconst_1
	bipush 88
	iastore
	astore_1
.line 7
	iconst_2
	newarray int
	astore_2
.line 9
	aload_2
	iconst_1
	aload_0
	getfield test.a [I
	iconst_0
	iaload
	iastore
.line 10
	aload_2
	iconst_0
	aload_1
	iconst_0
	iaload
	iastore
.line 11
	aload_0
	iconst_4
	putfield test.i I
.line 12
	aload_0
	bipush 7
	putfield test.j I
.line 13
	iconst_1
	istore_3
Label0:
.line 14
	return

.end method

.method public test1([I[I)V
.limit stack 1
.limit locals 4
.var 0 is this Ltest; from Label0 to Label0
.var 1 is arg0 [I from Label0 to Label0
.var 2 is arg1 [I from Label0 to Label0

.line 16
	iconst_0
	istore_3
Label0:
.line 17
	return

.end method

.method public static main([Ljava/lang/String;)V
.limit stack 1
.limit locals 2
.var 0 is arg0 [Ljava/lang/String; from Label0 to Label0

.line 19
	iconst_5
	istore_1
Label0:
.line 20
	return

.end method
