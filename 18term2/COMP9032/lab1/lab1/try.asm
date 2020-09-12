/*
 * try.asm
 *
 *  Created: 2018/8/16 18:43:34
 *   Author: liky
 */ 
.include "m2560def.inc"
.def zero = r15
.equ m = 2
.equ n = 3
.macro mul2	;@5:@4 = (@3:@2) * (@1:@0)
mul @0, @2	;al * bl
movw

