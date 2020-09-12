/*
 *	task2.asm
 *
 *  Created: 10/08/2018 6:27:53 PM
 *  Author: Keyang Li
 *	Caculate the result of sum of 
 *	
 */ 
 .include "m2560def.inc"
 .def a = r16
 .def n = r17
 .def sum_low = r24
 .def sum_high = r25

 .macro multiplex	;@0 is sum's low bits, @1 is sum's high bits, @2 is a
 mul @1, @2
 mov @1, r0
 mul @0, @2
 mov @0, r0
 add @1, r1

 .endmacro
 clr sum_high
 clr sum_low

 sum:
 cpi n, 0
 breq end
 adiw sum_high:sum_low, 1
 multiplex sum_low, sum_high, a
 dec n
 rjmp sum

 end:
 rjmp end