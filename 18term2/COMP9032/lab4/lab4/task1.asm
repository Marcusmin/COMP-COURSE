
;
  
.include "m2560def.inc"
;register for reading keypad value
 .def row = r16	;value for row number
 .def col = r17	;value for col number
 .def rmask = r18	;mask for current row during scan
 .def cmask = r19	;mask for current colum during scan
 .def temp = r20
 .def temp1 = r21
 .def counter = r22	;count charactors
 .def argument = r23

 ;constant for reading keypad value
 .equ PORTF_DIR = 0x0F	;PF7:4 are input, for scan row, R3:R0 map to PF7:PF4
 						;PF3:0 are output, for scan colum, C3:C0 map to PF0:PF3
 .equ INIT_COL_MASK = 0b11110111	;scan from leftmost colum, 1111 0111
 .equ INIT_ROW_MASK = 0b00010000	;scan from top row, 0001 0000
 .equ ROWMASK = 0xF0	; mask out the colum value

.macro do_lcd_command	;@0 is a binary code indicating a command
	push r23
	ldi r23, @0
	call lcd_command
	call lcd_wait
	pop r23
.endmacro

.macro do_lcd_data	;@0 is a binary code indicating a data
	ldi r23, @0
	call lcd_data
	call lcd_wait
.endmacro

;data constant
.equ lcd_function_set = 0b00111000	;data length is 8bits(BD4 = 1), display mode is 2 lines(DB3 = 1) x 5x7 font(DB2 = 0)
.equ lcd_display_off = 0b00001000	;display off(DB2 = 0), cursor off(DB1 = 0), blink off(DB0 = 0)
.equ lcd_display_on = 0b00001111	;display on(DB2), cursor on(DB1), blink(DB0)
.equ lcd_entry_set = 0b00000110		;increament(DB1), no shift(DB0)
.equ lcd_clear_display = 0b00000001	;clear display
;command constant
.equ lcd_new_line = 0b11000000	;print new line command

jmp RESET


RESET:
;	initialize lcd
	;build up a stack
	ldi r23, low(RAMEND)
	out SPL, r23
	ldi r23, high(RAMEND)
	out SPH, r23

	ser r23	;set r16 to be 0xFF
	out DDRC, r23	;make port F to be output
	out DDRA, r23	;make port A to be output
	clr r23	;clear r23
	out PORTF, r23	;clear PORTF
	out PORTA, r23	;clear PORTA

	ldi temp, PORTF_DIR	;load direction value
	out DDRF, temp

	do_lcd_command lcd_function_set ; 2x5x7
	rcall sleep_5ms	;transfer data
	do_lcd_command lcd_function_set ; 2x5x7
	rcall sleep_1ms
	do_lcd_command lcd_function_set ; 2x5x7
	do_lcd_command lcd_function_set ; 2x5x7
	do_lcd_command lcd_display_off ; display off
	do_lcd_command lcd_clear_display ; clear display
	do_lcd_command lcd_entry_set ; increment, no display shift
	do_lcd_command lcd_display_on ; Cursor on, bar, blink on

	clr counter
	jmp main


main:
	rcall readKeypad	;read a value from keypad
	inc counter		;increace counter
	cpi counter, 17		;if counter eq 17
	breq newLine	;cursor moves to another line
	cpi counter, 33	;full line
	breq clean_screen	;clear screen
	rcall lcd_data	;ready to another character
	rcall lcd_wait	;wait for transferring
	jmp main
	newLine:
		do_lcd_command lcd_new_line	;print a new line
		rcall lcd_data
		rcall lcd_wait
		jmp main
	clean_screen:
		do_lcd_command lcd_clear_display
		clr counter
		rcall lcd_data
		rcall lcd_wait
		jmp main

.equ LCD_RS = 7	;PORTA pin7
.equ LCD_E = 6	;PORTA pin6
.equ LCD_RW = 5	;PORTA pin5
.equ LCD_BE = 4	;PORTA pin4

.macro lcd_set
	sbi PORTA, @0
.endmacro
.macro lcd_clr
	cbi PORTA, @0
.endmacro

;
; Send a command to the LCD (r16)
;

lcd_command:
	out PORTC, r23	;write command data into port C
	nop
	lcd_set LCD_E	;turn on enable pin
	nop
	nop
	nop
	lcd_clr LCD_E	;turn off enable pin
	nop
	nop
	nop
	ret

lcd_data:
	out PORTC, r23	;write data value into port C
	lcd_set LCD_RS	;RS = 1, indicating command
	nop
	nop
	nop
	lcd_set LCD_E	;turn on enable pin
	nop
	nop
	nop
	lcd_clr LCD_E	;turn off enable pin
	nop
	nop
	nop
	lcd_clr LCD_RS	;clear RS, switch to instruction register
	ret

lcd_wait:
	push r23
	clr r23
	out DDRC, r23	;port C is input mode
	out PORTC, r23	;clear port C
	lcd_set LCD_RW	;RW = 1, read, RS = 0, instruction
	lcd_wait_loop:
		nop
		lcd_set LCD_E	;turn on enable pin
		nop
		nop
		nop
		in r23, PINC	;
		lcd_clr LCD_E	;turn off enable pin
		sbrc r23, 7	;if busy flag is clear, skip out from busy loop
		rjmp lcd_wait_loop
		lcd_clr LCD_RW
		ser r23	
		out DDRC, r23	;port C is output
		pop r23
		ret

.equ F_CPU = 16000000
.equ DELAY_1MS = F_CPU / 4 / 1000 - 4
; 4 cycles per iteration - setup/call-return overhead

sleep_1ms:
	push r24
	push r25
	ldi r25, high(DELAY_1MS)
	ldi r24, low(DELAY_1MS)
delayloop_1ms:
	sbiw r25:r24, 1
	brne delayloop_1ms
	pop r25
	pop r24
	ret

sleep_5ms:
	rcall sleep_1ms
	rcall sleep_1ms
	rcall sleep_1ms
	rcall sleep_1ms
	rcall sleep_1ms
	ret

sleep_a_while:
	push r23
	ldi r23, 25
	sleep_loop:
		rcall sleep_5ms
		subi r23, 1
		brne sleep_loop
	pop r23
	ret
	

 readKeypad:
	ldi cmask, INIT_COL_MASK	;initial colum mask
	clr col	;initial colum value
	colum_scan:
		cpi col, 4	;if outer loop over, back to main
		breq readKeypad
		;else scan a colum
		out PORTF, cmask	;set one of the colum bit to be 0

		ldi temp, 0xff	;slow down the scan operation
		delay:
			dec temp
			brne delay
		;clr temp
		in temp, PINF
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
		subi temp, -'1'
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
		ldi temp, '0'

	convert_end:
		rcall sleep_a_while
		mov r23, temp
		ret

