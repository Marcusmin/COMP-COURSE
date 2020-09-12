/*
 * AsmFile1.asm
 *
 *  Created: 2018/10/22 11:00:16
 *   Author: liky
 */ 
 ; Port F is used for keypad, high 4 bits for column selection, low four bits for reading rows. On the board, RF7-4 connect to C3-0, RF3-0 connect to R3-0.
; Port D is used to display the ASCII value of a key.

.include "m2560def.inc"
	
.def row    =r16		; current row number
.def col    =r17		; current column number
.def rmask   =r18		; mask for current row
.def cmask	=r19		; mask for current column
.def temp1	=r20		
.def temp2  =r21

.equ PORTFDIR =0xF0			; use PortD for input/output from keypad: PF7-4, output, PF3-0, input
.equ INITCOLMASK = 0xEF		; scan from the leftmost column, the value to mask output
.equ INITROWMASK = 0x01		; scan from the bottom row
.equ ROWMASK  =0x0F			; low four bits are output from the keypad. This value mask the high 4 bits.

;rjmp	RESET


RESET:

	ldi temp1, PORTFDIR			; columns are outputs, rows are inputs
	out	DDRC, temp1
	ser temp1					; PORTC is outputs
	sts DDRL, temp1				
	sts PORTL, temp1


main:
			in temp1, DDRC		; restart main loop
			sts PORTL, temp1