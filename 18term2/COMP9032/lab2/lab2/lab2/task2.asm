/*
 * task2.asm
 *
 *  Created: 2018/8/30 15:18:41
 *  Author: Group 5
 *	Mission: Binary positional division
 */ 
.def helper = r24	;XOR
.dseg
	quotient: .byte 2	;quotient store in data memory
.cseg
	dividend: .dw 5625	;dividend in program memory
	divisor: .dw 9	;divisor is program memory
main:
	;set up a stack
	ldi YL, low(RAMEND)
	ldi YH, high(RAMEND)
	out SPL, YL
	out SPL, YH

	ldi ZL, low(dividend<<1)	;load divident's address from program memory
	ldi ZH, high(dividend<<1)	;load divisor's address from program memory

	lpm r16, Z+
	lpm r17, Z

	ldi ZL, low(divisor<<1)		;load divident's address from program memory
	ldi ZH, high(divisor<<1)

	lpm r18, Z+
	lpm r19, Z
	rcall binary_positional_division	;call the function
	
	ldi YL, low(quotient)	;get result from data space
	ldi YH, high(quotient)
	ld r22, Y+
	ld r23, Y
end:
nop
rjmp end
binary_positional_division:
	;prologue
	push ZL
	push ZH
	push r16
	push r17	;(r16:r17)use as dividend
	push r18	
	push r19	;(r18:r19)use as dividor
	push r20
	push r21	;(r20:r21) as local variable bit_position of function
	push r22
	push r23	;(r22:r23) as local variable quotient
	in YL, SPL
	in YH, SPH
	sbiw Y, 2
	std Y+1, r18
	std Y+2, r19	;pass the divisor to (Y+1:Y+2)
	out SPL, YL
	out SPL, YH 

	;function body
	ldi r20, 1
	clr r21	;unsigned int bit_position = 1

	clr r22
	clr r23;	quotient = 0;

	clr helper	;make helper to be 0

	first_loop:		;(dividend > divisor) && !(divisor & 0x8000)
		cp r18, r16
		cpc r19, r17	;if divisor >= divident, break the loop
		brsh second_loop
		andi r18, low(0x8000)
		andi r19, high(0x8000)	;divisor & 0x8000
		ldi helper, 0xFF
		EOR r18, helper
		EOR r19, helper	;!(divisor & 0x8000)
		clr helper
		cp r18, helper
		cpc r19, helper
		breq second_loop
		ldi helper, 1
		ldd r18, Y+1
		ldd r19, Y+2	;reload the divisor's value
		lsl r19
		sbrc r18, 7
		add r19, helper	;if r18's most significant bit is 1, then r19 + 1
		lsl r18	;divisor << 1
		lsl r21
		sbrc r20, 7
		add r21, helper	;if r21's most significant bit is 1, then r20 + 1
		lsl r20	
		std Y+1, r18	;store divisor's value to data space
		std Y+2, r19
		rjmp first_loop
	
	second_loop:
		clr helper	;reset the helper to zero
		cp r20, helper
		cpc r21, helper	;
		brlo done	;if bit_postion < 0, break
		cp r20, helper
		cpc r21, helper
		breq done	;if bit_position == 0, break
		cp r16, r18	
		cpc r17, r19
		brsh first_if	;if dividend >= divisor
		entry:
		ldi helper, 128	;make helper becom 1000,0000
		lsr r18
		sbrc r19, 0
		add r18, helper
		lsr r19	;logical right shift divisor
		lsr r20
		sbrc r21, 0
		add r20, helper
		lsr r21	;logical right shift bit_positon
		rjmp second_loop
		first_if:	;if dividend >= divisor
			sub r16, r18
			sbc r17, r19	;dividend = dividend - divisor
			add r22, r20
			adc r23, r21	;quotient = quotient + bit_position
			rjmp entry
	done:
		ldi XL, low(quotient)
		ldi XH, high(quotient)
		st X+, r22
		st X, r23	;store the result in data space
		adiw Y, 2
		out SPL, YL
		out SPH, YH	;reload the stack pointer
		pop r23
		pop r22
		pop r21
		pop r20
		pop r19
		pop r18
		pop r17
		pop r16
		pop ZH
		pop ZL
		ret	;return




