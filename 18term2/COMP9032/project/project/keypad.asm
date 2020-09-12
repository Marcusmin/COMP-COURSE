 
 .def row = r16	;value for row number
 .def col = r17	;value for col number
 .def rmask = r18	;mask for current row during scan
 .def cmask = r19	;mask for current colum during scan
 .def temp = r20
 .def temp1 = r21
 ;.def operand = r22
 ;.def pre_operand = r23
 ;.def iH = r25
 ;.def iL = r24
 .equ PORTF_DIR = 0x0F	;PF7:4 are input, for scan row, R3:R0 map to PF7:PF4
 						;PF3:0 are output, for scan colum, C3:C0 map to PF0:PF3
 .equ INIT_COL_MASK = 0b11110111	;scan from leftmost colum, 1111 0111
 .equ INIT_ROW_MASK = 0b00010000	;scan from top row, 0001 0000
 .equ ROWMASK = 0xF0	; mask out the colum value
 ;.equ loop_count = 1024

;.macro DELAY_05
	;delay
;	ldi iL, low(loop_count)	;1
;	ldi iH, high(loop_count)	;1
;	loop:	;(8196 + 18) * 1024 + 4 = 8411140 CC
;		sbiw iH:iL, 1
;		push iH
;		push iL
;		loop2:	;8 * 1024 + 4 = 8196
;			sbiw iH:iL, 1	;2
;			cpi iL, 0	;1
;			brne loop2
;			cpi iH, 0	;1
;			brne loop2	;1, 2
;			nop	;1
;		done_2:
;		pop iL
;		pop iH
;		cpi iL, 0	;1
;		brne loop
;		cpi iH, 0	;1
;		brne loop	;1,2
;		nop	;1
;	done:
;.endmacro

jmp RESET

 RESET:
 ldi temp, PORTF_DIR	;load direction value
 out DDRC, temp
 ser temp
 sts DDRL, temp	;set PORTC for output
 sts PORTL, temp

 jmp main



 main:
	ldi cmask, INIT_COL_MASK	;initial colum mask
	clr col	;initial colum value
	colum_scan:
		cpi col, 4	;if outer loop over, back to main
		breq main
		;else scan a colum
		out PORTC, cmask	;set one of the colum bit to be 0

		ldi temp, 0xff	;slow down the scan operation
		delay:
			dec temp
			brne delay

		in temp, PINC
		andi temp, ROWMASK	;only need row's value

		cpi temp, 0xF0	;if no row have low bit, means pressed button is not on this colum
		breq next_colum	; we can try another colum
		;otherwise, try to find pressed button's row number

		;else
		ldi rmask, INIT_ROW_MASK
		clr row
		row_scan:
			cpi row, 4	;
			breq next_colum	;
			mov temp1, temp	;we need temp if found out row and col number
			and temp1, rmask	;if the bit where is low that is same as rmask, then foud out its row number
			breq convert	;go to conver row number and colum number to value

			;else
			inc row
			LSL rmask
			jmp row_scan
		
		next_colum:	;first colum's row scan is over
			ASR cmask
			inc col
			jmp colum_scan
		
	convert:
		cpi col, 3
		breq letters	;col 3 is ABCD
		cpi row, 3
		breq symbols	;row 3 is *0#D

		;otherwise we have numbers from 1 to 9
		mov temp, row
		lsl temp	;row * 2
		add temp, row	;row * 3
		add temp, col	;number = 3*row +col
		;subi temp, -'1'
		jmp convert_end
	letters:
		ldi temp, 'A'
		add temp, row
		jmp convert_end
	symbols:
		cpi col, 0
		breq star
		cpi col, 1
		breq zero
		ldi temp, '#'
		jmp convert_end
	star:
		ldi temp, '*'
		jmp convert_end
	zero:
		ldi temp, 0

	convert_end:
		sts PORTL, temp
		jmp main