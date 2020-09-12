/*
 * task2.asm
 *
 * Created: 2018/9/12 12:28:00
 * Author: Group 5
 * Mission:
 * Use 2 external interrupts to start or stop the display of LEDs.
 */ 
 ;PORTD 7 bit --> INT0 connecting to PB0
 ;PORTD 6 bit --> INT1 connecting to PB1
 ;PORTC stores the pattern
 .include "m2560def.inc"
 .def temp = r16
 .def pattern = r17
 .def iH = r25	;these register are used in macro to achieve 0.5 second delay
 .def iL = r24	;between each pattern
 .def countH = r21
 .def countL = r20
 .def flag = r18
 .equ loop_count = 1024
 .equ PATTERN_1 = 0b10000000
 .equ PATTERN_2 = 0b00001000
 .equ PATTERN_3 = 0b00000001

.macro DELAY	;approximately 0.5 seconds
	;delay
	ldi countL, low(loop_count)	;1
	ldi countH, high(loop_count)	;1
	clr iH	;1
	clr iL	;1
	loop:	;(8196 + 18) * 1024 + 4 = 8411140 CC
		cp iL, countL	;1
		cpc iH, countH	;1
		brsh done	;1,2
		push iL	;2
		push iH	;2
		clr iL	;1
		clr iH	;1
		loop2:	;8 * 1024 + 4 = 8196
			cp iL, countL	;1
			cpc iH, countH	;1
			brsh done_2	;1, 2
			adiw iH:iL, 1	;2
			nop	;1
			rjmp loop2	;2
		done_2:
		pop iH	;2
		pop iL	;2
		adiw iH:iL, 1	;2
		nop	;1
		rjmp loop	;2
	done:
.endmacro

;set up interrupt vector
rjmp RESET
.org int0addr	;default address in m2560inc.def
jmp EXT_INT0	;interrupt vectors
.org int1addr
jmp EXT_INT1


RESET:
	ser temp
	out DDRC, temp	;port C to be output
	ldi temp, (2 << ISC00)	;temp is 0b00000010
	sts EICRA, temp	;make int01:int00 to be 10, 
					;which means set int0 to be an falling edge triggered interrupt
	in temp, EIMSK	;load mask register's value to temp
	ori temp, (1<<INT0)
	out EIMSK, temp	;enable int0

	ldi temp, (2 << ISC10)	;temp is 0b00000010
	sts EICRA, temp	;make int11:int10 to be 10, 
					;which means set int1 to be an falling edge triggered interrupt
	in temp, EIMSK	;load mask register's value to temp
	ori temp, (1<<INT1)
	out EIMSK, temp	;enable int1
	sei	;enable global interrupt
	jmp main	;begin main programming

EXT_INT0:
	push temp	;save conflict register
	in temp, SREG
	push temp	;save status register
	terminate_INT0:
		ser flag	; set flag
		pop temp
		out SREG, temp
		pop temp
		reti

EXT_INT1:
	push temp	;save conflict register
	in temp, SREG
	push temp	;save status register
	terminate_INT1:
		clr flag	; clr flag
		pop temp
		out SREG, temp
		pop temp
		reti

main:
	ldi pattern, PATTERN_1
	out PORTC, pattern
	DELAY
	DELAY
	halt1:
	cpi flag, 0xff	; if trigger interrupt 0, which will set flag, then stop at this pattern
	breq halt1

	ldi pattern, PATTERN_2
	out PORTC, pattern
	DELAY
	DELAY
	halt2:
	cpi flag, 0xff
	breq halt2
	
	ldi pattern, PATTERN_3
	out PORTC, pattern
	DELAY
	DELAY
	halt3:
	cpi flag, 0xff
	breq halt3
	
	rjmp main