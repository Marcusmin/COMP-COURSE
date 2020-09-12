/*
 * task3.asm
 *
 *  Created: 2018/10/10 18:10:53
 *	mission target: measure motor speed
 *	use timer to build a interrupt which occur every x second
 *	in the interrupt, clear the timer and display the  revolution per second
 */ 
 .include "m2560def.inc"
 .def temp = r16

 .macro Clear	;clear a word @0 which is a word in data space
	ldi YL, low(@0)
	ldi YH, high(@0)
	clr temp
	st Y+, temp
	st Y, temp
.endmacro

.macro display_digits	;display @1's value on lcd
	ldi @0, '0'	;@0 is to get @1's digits' ascii code
	cpi @1, 100	;if @1 smaller than 100, means @1's value is 2 digits
	brlo two_digits
	;TODO:
	loop_1:	;otherwis, get its hundreds digit
		cpi @1, 100
		brlo two_digits
		sbci @1, 100
		inc @0
		rjmp loop_1
	two_digits:
		mov r23, @0	;load digit ascii code to r23
		call lcd_data	;first digit display
		call lcd_wait	;wait data transfer
		ldi @0, '0'	;@0's initial value
		cpi @1, 10	;@1 could be 1 digits decimal value if it smaller than 10
		brlo one_digit
		loop_2:
			cpi @1, 10
			brlo one_digit
			sbci @1, 10
			inc @0
			rjmp loop_2	;same function as above 3 digits situation
	one_digit:
	;TODO
		mov r23, @0	;second digit display
		call lcd_data
		call lcd_wait	;last digit display
		ldi temp, '0'
		add @1, temp	;the last digit of @1
		mov r23, @1
		call lcd_data
		call lcd_wait
		clr @1	
.endmacro

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

.dseg	;store in data space
	TempCounter:
		.byte 2	;if this data is equal to 1000, which means the interrupt happend 1000 time
.cseg

.equ lcd_function_set = 0b00111000	;data length is 8bits(BD4 = 1), display mode is 2 lines(DB3 = 1) x 5x7 font(DB2 = 0)
.equ lcd_display_off = 0b00001000	;display off(DB2 = 0), cursor off(DB1 = 0), blink off(DB0 = 0)
.equ lcd_display_on = 0b00001111	;display on(DB2), cursor on(DB1), blink(DB0)
.equ lcd_entry_set = 0b00000110		;increament(DB1), no shift(DB0)
.equ lcd_clear_display = 0b00000001	;clear display
;command constant

.org 0x0000
jmp RESET

.org int0addr
	jmp EXIT0

.org OVF0addr
	jmp Timer0OVF	;interrupt handler for timeer0 overflow


RESET:
	Clear TempCounter	;clear TempCounter

	;enable interrupt 0
	cbi DDRD, 7	;input
	in temp, EIMSK
	ori temp, 1<<int0	;enable interrupt 0 and interrupt 1
	out EIMSK, temp
	;set interrupt mode to be falling edge triggered
	ldi temp, 2<<ISC00
	sts EICRA, temp	;falling edge 
	sei
	ser temp
	out DDRC, temp	;port C output data to lcd controller
	out DDRA, temp
	do_lcd_command lcd_function_set ; 2x5x7
	rcall sleep_5ms	;transfer data
	do_lcd_command lcd_function_set ; 2x5x7
	rcall sleep_1ms
	rcall sleep_1ms
	do_lcd_command lcd_function_set ; 2x5x7
	do_lcd_command lcd_function_set ; 2x5x7
	;warm up lcd
	do_lcd_command lcd_display_off ; display off
	do_lcd_command lcd_clear_display ; clear display
	do_lcd_command lcd_entry_set ; increment, no display shift
	do_lcd_command lcd_display_on ; Cursor on, bar, blink on

	;PORTF is used to connect to motor
	;clr temp
	sbi	DDRF, 0	;PF0 is output
	sbi PORTF, 0

	clr r17	;used for counting roation
	clr r23	;used for pass value for lcd functions
	ldi r18, '0'	;used to display digits
	jmp main

EXIT0:
	call increase_one_roation	;increase with rotation
	reti

Timer0OVF:	;if timer0 overflow, evoke this subroutine
	ldi YL, low(TempCounter)	;value in data space's low address
	ldi YH, high(TempCounter)	;value in data space's high address
	ld r24, Y+
	ld r25, Y	;(r25:r24) is the value of how many times that overflow happen previously
	adiw r25:r24, 1	;add one every interrupt happens
	;compare r25:r24 with 1000
	ldi temp, low(1000)
	cp r24, temp
	brne NotSecond
	ldi temp, high(1000)
	cpc r25, temp
	brlo NotSecond	;if haven't been a second, handle by NotSecond branch
	;if interrupt 1000 times
	;TODO: roation time display on lcd screen
	LSR r17
	LSR r17	;divided by 4, for four holes
	;convert to digits
	rcall new_display	;refresh lcd screen
	display_digits r18, r17	;r17 store rotation times, 
	;r18 is helping for display of all digits
	Clear TempCounter	;1000 times interrupt complete, empty counter
	NotSecond:	;if not a second, do nothing
		st Y, r25
		st -Y, r24
	reti

main:	;use r17 to count rotation
	;ope connect to PF0
	;opo connect to int0
	;enble timer0 overflow interrupt
	clr temp
	out TCCR0A, temp
	ldi temp, 0b00000011
	out TCCR0B, temp	;prescale value = 64
	ldi temp, 1<<TOIE0
	sts TIMSK0, temp	;enable timer overflow interrupt
	sei	;enable global interrupt
	;enable lcd
	;connect portC with lcd Data
	loop:	;infinite loop
		rjmp loop
	;increase r17 by one









;port A connected to LCD control port
.equ LCD_RS = 7	;PORTA pin7
.equ LCD_E = 6	;PORTA pin6
.equ LCD_RW = 5	;PORTA pin5
.equ LCD_BE = 4	;PORTA pin4

.macro lcd_set	;set specific pin of lcd control port
	sbi PORTA, @0
.endmacro

.macro lcd_clr	;set specific pin of lcd control port
	cbi PORTA, @0
.endmacro

;
; Send a command to the LCD (r23)
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

increase_one_roation:
	inc r17	;store value in r17
	ret

new_display:
	do_lcd_command lcd_clear_display ; clear display
	do_lcd_command lcd_entry_set ; increment, no display shift
	do_lcd_command lcd_display_on ; Cursor on, bar, blink on
	ret


.equ F_CPU = 16000000
.equ DELAY_1D2MS = F_CPU / 4 / 2000 - 4
sleep_1D2ms:	;sleep 0.5 second
	push r24
	push r25
	ldi r25, high(DELAY_1D2MS)	
	ldi r24, low(DELAY_1D2MS)	;(r25:r24) is delay times
delayloop_1D2ms:
	sbiw r25:r24, 1
	brne delayloop_1D2ms
	pop r25
	pop r24
	ret

sleep_1ms:
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	ret

sleep_5ms:
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	rcall sleep_1D2ms
	ret