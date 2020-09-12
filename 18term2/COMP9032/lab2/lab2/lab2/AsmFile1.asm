/*
 * AsmFile1.asm
 *
 *  Created: 2018/8/23 18:53:00
 *  Author: liky
 */ 
 .include "m2560def.inc"
 clr r2 
ldi r18, 0xFF
ldi r16, low(0xFF01)
ldi r17, high(0xFFFF)
eor r16, r18
eor r17, r18

breq end
nop
nop


end:
nop
rjmp end