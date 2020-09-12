/*
 * try.asm
 *
 *  Created: 2018/10/11 20:58:04
 *   Author: liky
 */ 
 .include "m2560def.inc"
.dseg
	var:
	.byte 2

main:
rjmp main
