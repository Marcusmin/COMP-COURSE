;
; lab2.asm
;
; Created: 2018/8/23 14:39:03
; Author : Group05
;


; Replace with your application code
.include "m2560def.inc"
.def zero = r21
.set integer = 2
.dseg
	number:	.byte integer
.cseg
	s:	.db "123456"

.macro cal_n	;n * 10 + c - '0' (@1:@2) = (@1:@2)*@3 + @0 -48
	mul @2, @3
	mov @2, r0
	mul @1, @3
	mov @1, r0
	add @2, r1
	subi @0, 48
	add @1, @0
	adc @2, zero

.endmacro
ldi zero, 0

ldi YL, low(RAMEND)	; set up stack
ldi YH, high(RAMEND)
out SPL, YL
out SPH, YH


	;main
ldi YH, high(number)	;pass number's address to function
ldi YL, low(number)
rcall atoi	;call function
ld r18, Y+	;read the result to main
ld r19,Y+
end:
nop
 rjmp end
atoi:
;prologue
	push YL
	push YH
	push r16	;i,save the register used in function
	push r17	;c 's register
	push r18	;n's low bits
	push r19	;n's high bits
	push r20	;load 10
	push r22	;65535's low bits
	push r23	;65535's high bits
	in YL, SPL
	in YH, SPH
	sbiw Y, 6	
	out SPL, YL
	out SPH, YH

	ldi ZL, low(s<<1)
	ldi ZH, high(s<<1)	;store the string "12345" in Z, read string from cseg
	;end prologue
	;function body
	clr r16	;i is r16
	clr r17	;c is r17
	clr r18	;n is r18 and r19
	clr r19	;
	
	ldi r16, 1	;initialize i to 1
	lpm r17, Z+1	;initialize c to *a
	ldi r22, low(65535)
	ldi r23, high(65535)
	loop:
		cpi r17, 48	;if c < '0', turn to end
		brlo done
		cpi r17, 58	;if c > '9'+1, turn to end
		brsh done
		cp r18, r22	;if c > 65536, turn to end
		cpc r19, r23
		brsh done
		ldi r20, 10
		cal_n r17, r18, r19, r20	;get n's value 
		lpm r17, Z+	;c read into string's next character
		inc r16	;increase i by 1
		rjmp loop
	done:
		ldi ZL, low(number)	;read the number's address into Z
		ldi ZH, high(number)
		st Z+, r18
		st Z+, r19	; store the value in data segment
		adiw Y, 6
		out SPL, YL
		out SPH, YH
		pop r23
		pop r22
		pop r20
		pop r19
		pop r18
		pop r17
		pop r16
		pop YH
		pop YL
		ret


