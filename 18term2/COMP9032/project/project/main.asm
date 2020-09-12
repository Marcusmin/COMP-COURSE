;
; project.asm
;
; Created: 2018/10/19 13:22:43
; Author : liky

; LCD settings: 1. Connect LCD data pins D0-D7 to PORTL7-0.
; 2. Connect the four LCD control pins BE-RS to PORTA4-7.

;LED settings: 1. Connect LED BAR pins 0-3 to PORTC0-3 as ball's position
;2. Connect  LED BAR pins6-9 to PORTC4-7

;Keypad setting: C3-0 connnected to PF7-4
;R3-0 connected to PF3-0

;Motor setting: connected to PC3

;Push Button: connected to PB0

.include "m2560def.inc"
.dseg
    KeypadValue: .byte 1
.cseg
;keypad define
.def row    =r16		; current row number
.def col    =r17		; current column number
.def rmask   =r18		; mask for current row
.def cmask	=r19		; mask for current column
.def temp1	=r20		; keypad value
.def temp2  =r21		; helper to read keypad value

.def isGameStart = r22	;flag, for checking if game has started
.def isGameGoingOn = r15	;flag, for checking if game going on
.def score = r23	;store the user's score

.equ PORTCDIR =0xF0			; use PortF for input/output from keypad: PF7-4, output, PF3-0, input
.equ INITCOLMASK = 0xEF		; scan from the leftmost column, the value to mask output
.equ INITROWMASK = 0x01		; scan from the bottom row
.equ ROWMASK  =0x0F			; low four bits are output from the keypad. This value mask the high 4 bits.

;lcd needed command macro
.macro do_lcd_command
    push r16
	ldi r16, @0
	rcall lcd_command
	rcall lcd_wait
    pop r16
.endmacro
;motor starts to spin
.macro motor_spin
    sbi PORTC, 7-3
.endmacro

;motor stop spinning
.macro motor_stop
	cbi PORTC, 7-3
.endmacro


.macro do_lcd_data
    push r16
	ldi r16, @0
	rcall lcd_data
	rcall lcd_wait
    pop r16
.endmacro

;@0 indicate which cup is up
.macro light_cup
	sbi PORTC, (7-@0)
.endmacro

.macro dark_cup
	cbi PORTC, (7-@0)
.endmacro
; all led on
.macro  all_light
    light_cup 0
    light_cup 1
    light_cup 2
.endmacro
; all led off
.macro  all_dark
    dark_cup 0
    dark_cup 1
    dark_cup 2
.endmacro
; result indicator on
.macro winner_light
	sbi PORTC, 7 - 7
	sbi PORTC, 7 - 6
	sbi PORTC, 7 - 5
	sbi PORTC, 7 - 4
.endmacro
; result indicator off
.macro winner_dark
	cbi PORTC, 7 - 7
	cbi PORTC, 7 - 6
	cbi PORTC, 7 - 5
	cbi PORTC, 7 - 4
.endmacro
;display score's digits on lcd
.macro display_digits	;display @1's value on lcd, @0 is helper register
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
		mov r16, @0	;load digit ascii code to r23
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
			rjmp loop_2
			;same function as above 3 digits situation
	one_digit:
		mov r16, @0	;second digit display
		call lcd_data
		call lcd_wait	;last digit display
		ldi temp1, '0'
		add @1, temp1	;the last digit of @1
		mov r16, @1
		call lcd_data
		call lcd_wait
		clr @1	
.endmacro



.org 0x0000
jmp RESET

.org int0addr
jmp pressButton

.org int1addr
jmp win_shuffle

RESET:
    ;build up a stack
    ldi r16, low(RAMEND)
	out SPL, r16
	ldi r16, high(RAMEND)
	out SPH, r16
    ; Initialize lcd
	ser r16
	sts DDRL, r16
	out DDRA, r16
	clr r16
	sts PORTL, r16
	out PORTA, r16

	do_lcd_command 0b00111000 ; 2x5x7
	rcall sleep_5ms
	do_lcd_command 0b00111000 ; 2x5x7
	rcall sleep_1ms
	do_lcd_command 0b00111000 ; 2x5x7
	do_lcd_command 0b00111000 ; 2x5x7
	do_lcd_command 0b00001000 ; display off
	do_lcd_command 0b00000001 ; clear display
	do_lcd_command 0b00000110 ; increment, no display shift
	do_lcd_command 0b00001110 ; Cursor on, bar, no blink
	;display "Ready..."
	do_lcd_data 'R'
	do_lcd_data 'e'
	do_lcd_data 'a'
	do_lcd_data 'd'
	do_lcd_data 'y'
	do_lcd_data '.'
	do_lcd_data '.'
	do_lcd_data '.'
    ; Initialize Keypad
	ldi temp1, PORTCDIR			; columns are outputs, rows are inputs
	out	DDRF, temp1

    ; Initialize LED
    ser r16
    out DDRC, r16  ;output
    clr r16
    out PORTC, r16
    
    ;enable interrupt 0
    in r16, EIMSK
	ori r16, (1<<int0) | (1<<int1)	;enable interrupt 0 and external interrupt 1
	out EIMSK, r16
	ldi r16, (2<<ISC00) | (2<<ISC10)	;set interrupt mode to be falling edge triggered
	sts EICRA, r16	;falling edge 
	sei	;global interrupt

    clr isGameStart	;clear flags
	clr isGameGoingOn

    ldi YL, low(KeypadValue)
    ldi YH, high(KeypadValue)
    st Y, isGameStart   ;initalize Keypad value with 0
	;config timer
	ldi r16, (1 << CS02)|(1 << CS00)	;select clock cycle
	out TCCR0B, r16
    jmp waitStart	;wait user interaction

;external interrupt 1
;result indicator will flash if user get a correct guess
win_shuffle:
	winner_light
	call sleep_25ms
	call sleep_25ms
	call sleep_25ms
	winner_dark
	call sleep_25ms
	call sleep_25ms
	winner_light
	call sleep_25ms
	call sleep_25ms
	winner_dark
	call sleep_25ms
	call sleep_25ms
	call sleep_25ms
	winner_light
	call sleep_25ms
	call sleep_25ms
	call sleep_25ms
	winner_dark
	reti

;external interrupt 0
pressButton:	;when button pressed
    in r16, EIMSK
	andi r16, 0	|(1<<int1)
	out EIMSK, r16 ;disable interrupt 0
	sbi EIFR, 0

    cpi isGameStart, 0xff   ;if game has start
    breq willRunGuess   ;run guessing process
    rjmp haveNotStart   ;otherwise go to inital start state
    willRunGuess:	;for guessing
		;debouncing
	    in r16, EIMSK
		ori r16, (1<<int0)|(1<<int1)
		out EIMSK, r16	;enable interrupt 0
		sbi EIFR, 0	;set interrupt flag
		sei	;global interrupt
		;above is to debouncing
		motor_stop
        jmp hasStart	;go to game processing
    haveNotStart:
        ser isGameStart ;flag is set, next interrupt would not come this branch
		motor_spin	;motor spin
        do_lcd_command 0b00000001	;clean screen
		;show "start..." on lcd
        do_lcd_data 'S'
        do_lcd_data 't'
        do_lcd_data 'a'
        do_lcd_data 'r'
        do_lcd_data 't'
        do_lcd_data '.'
        do_lcd_data '.'
        do_lcd_data '.'
		;delay 200ms
		call delay_100ms
		call delay_100ms
		;enable interrupt 0
        in r16, EIMSK
        ori r16, 1<<int0
        out EIMSK, r16
        sbi EIFR, 0
        ;TODO: Balls shuffled
		clr score	;clean "score register"
        reti

waitStart:
    ;display "Ready..."
    light_cup 2	;light up Cup 3
    ;if button is pressed, will trigger an interrupt called pressButton
	;if game has started(player's first press on button)
	cpi isGameStart, 0xff
	breq inital_state	;go to inital status
    ;otherwise wait
    rjmp waitStart
	inital_state:	;
	call sleepwithflash
	jmp inital_state

hasStart:	;if the game has start
	cp isGameGoingOn, isGameStart	;(isGameStart must be 0xff)
	breq readKeypadValue
    ;motor_stop	;stop motor
    do_lcd_command 0b00000001	;clean the lcd
    do_lcd_data 'S'
    do_lcd_data 'c'
    do_lcd_data 'o'
    do_lcd_data 'r'
    do_lcd_data 'e'
    do_lcd_data ':'
    gamePlaying:
        readKeypadValue:
			;motor_stop	;stop motor
            call keypad	;let player's guess
			call sleep_1ms	;keypad may bouncing
            ldi YL, low(KeypadValue)
            ldi YH, high(KeypadValue)
            ld r16, Y	;load the keypad value in r16
            cpi r16, 0  ;if keypad have no value yet
            breq readKeypadValue    ;then read key pad value again
			in temp1, tcnt0	;otherwise, timer generate random number
			call mod3	;mod timer's value with 3
			subi r16, '1'	;r16(keypad value) 's value is also from 0 to 2
			;let ball position light up
			clr r17
			out PORTC, r17	;led turn off
			find_ball_position:
				dark_cup 0	;turn off cup 1's light
				dark_cup 1	;turn off cup 2's light
				dark_cup 2	;turn off cup 3's light
				cpi temp1, 0	;if random number's value is 0
				breq is_zero
				cpi temp1, 1	;if random number's value is 1
				breq is_one
				cpi temp1, 2	;if random number's value is 2
				breq is_two
			is_zero:
				sbi PORTC, 7 - 0	;turn on cup 0
				rjmp settlement	;go to display result
			is_one:
				sbi PORTC, 7 - 1	;turn on cup 1
				rjmp settlement	;go to display result
			is_two:
				sbi PORTC, 7 - 2	;turn on cup 2
			settlement:
			;call sleep_1s ;wait 1second to display result to user
			cp r16, temp1	;compare random number with player's input
			breq get_score	;if player's guess is correct, get score
			lose_score:	;otherwise, lose a score
				clr r16
				st Y, r16	;set keypad value to be 0
				cpi score, 0	;if score cannot lose more marks
				breq newGame	;game restart(back to initial status)
				dec score	;otherwise decrease the score
				cpi score, 0	;if score becomes zero,
				breq newGame	;back to initial start status
				call display_score;display score on the lcd
				jmp waitNextRound	;begin next round
				newGame:	;initial status
					clr isGameGoingOn	;clear flag, game is not going on anymore
					call delay_1s	;display player's last guess result
				    do_lcd_command 0b00000001	;clean the lcd screen
					do_lcd_data 'S'
					do_lcd_data 't'
					do_lcd_data 'a'
					do_lcd_data 'r'
					do_lcd_data 't'
					do_lcd_data '.'
					do_lcd_data '.'
					do_lcd_data '.'	;show "start..." on lcd
					motor_spin	;motor spin
					;clr isGameStart	;game is terminated
					jmp inital_state
            get_score:	;user gets a score
				mov isGameGoingOn, isGameStart	;game has been started and going on
				inc score	;score + 1
				call display_score;display score
				sbi DDRD, 1	;trigger interrupt 1
				cbi DDRD, 1
				clr r16	;clean keypadValue
				st Y, r16
				jmp waitNextRound	;wait for user to have another round
    jmp gamePlaying

	waitNextRound:	;game is going on, wait for next round
	;motor_spin
	rjmp waitNextRound

;lcd needed function
.equ LCD_RS = 7
.equ LCD_E = 6
.equ LCD_RW = 5
.equ LCD_BE = 4

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
	sts PORTL, r16
	nop
	lcd_set LCD_E
	nop
	nop
	nop
	lcd_clr LCD_E
	nop
	nop
	nop
	ret

lcd_data:
	sts PORTL, r16
	lcd_set LCD_RS
	nop
	nop
	nop
	lcd_set LCD_E
	nop
	nop
	nop
	lcd_clr LCD_E
	nop
	nop
	nop
	lcd_clr LCD_RS
	ret

lcd_wait:
	push r16
	clr r16
	sts DDRL, r16
	sts PORTL, r16
	lcd_set LCD_RW
lcd_wait_loop:
	nop
	lcd_set LCD_E
	nop
	nop
    nop
	lds r16, PINL
	lcd_clr LCD_E
	sbrc r16, 7
	rjmp lcd_wait_loop
	lcd_clr LCD_RW
	ser r16
	sts DDRL, r16
	pop r16
	ret

.equ F_CPU = 16000000
.equ DELAY_1MS = F_CPU / 4 / 1000 - 4
; 4 cycles per iteration - setup/call-return overhead

sleep_1ms:	;delay 1ms
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

sleep_5ms:	;delay 5ms
	rcall sleep_1ms
	rcall sleep_1ms
	rcall sleep_1ms
	rcall sleep_1ms
	rcall sleep_1ms
	ret

sleep_25ms:	;delay 25ms
	call sleep_5ms
	call sleep_5ms
	call sleep_5ms
	call sleep_5ms
	call sleep_5ms
	ret

sleepwithflash:	;dimmed light
all_dark
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
all_light
ret


delay_100ms:
call sleep_25ms
call sleep_25ms
call sleep_25ms
call sleep_25ms
ret

delay_1s:	;200*5ms = 1000ms =~ 1s
    push r16
    ldi r16, 200
    sleeping:
        call sleep_5ms
        dec r16
        cpi r16, 0
        brne sleeping
    pop r16
    ret

sleep_5s:
    ldi r18, 5
    a_s_sleeping:
        call delay_1s
        dec r18
        cpi r18, 0
        brne a_s_sleeping
    ret

;display score on lcd
display_score:
	;display "score:"
	push score	;store the score
	do_lcd_command 0b00000001	;clean lcd
	do_lcd_data 'S'
    do_lcd_data 'c'
    do_lcd_data 'o'
    do_lcd_data 'r'
    do_lcd_data 'e'
    do_lcd_data ':'
	display_digits r17,score	;display score of user with 3 digits
	pop score
	ret

mod3:	;only particular for getting keypad
	sub3: ;temp1 % 3, get a number from 0 to 2
	subi temp1, 3
	cpi temp1, 3
	brsh sub3
	ret

keypad:
    all_light   ;ball shuffle with dimmed light
    ; push row	; current row number
    ldi cmask, INITCOLMASK		; initial column mask
    clr	col						; initial column
    colloop:
    cpi col, 4
    breq Keypad
    out	PORTF, cmask				; set column to mask value (one column off)
    ldi temp1, 0xFF
    delay:
    dec temp1
    brne delay

    in	temp1, PINF				; read PORTD
	all_dark
    andi temp1, ROWMASK
    cpi temp1, 0xF				; check if any rows are on
    breq nextcol
                                ; if yes, find which row is on
    ldi rmask, INITROWMASK		; initialise row check
    clr	row						; initial row
    rowloop:
    cpi row, 4
    breq nextcol
    mov temp2, temp1
    and temp2, rmask				; check masked bit
    breq convert 				; if bit is clear, convert the bitcode
    inc row						; else move to the next row
    lsl rmask					; shift the mask to the next bit
    jmp rowloop

    nextcol:
    lsl cmask					; else get new mask by shifting and 
    inc col						; increment column value
    jmp colloop					; and check the next column

    convert:
    cpi col, 3					; if column is 3 we have a letter
    breq letters				
    cpi row, 3					; if row is 3 we have a symbol or 0
    breq symbols

    mov temp1, row				; otherwise we have a number in 1-9
    lsl temp1
    add temp1, row				; temp1 = row * 3
    add temp1, col				; add the column address to get the value
    subi temp1, -'1'			; add the value of character '0'
    jmp convert_end

    letters:
    ldi temp1, 'A'
    add temp1, row				; increment the character 'A' by the row value
    jmp convert_end

    symbols:
    cpi col, 0					; check if we have a star
    breq star
    cpi col, 1					; or if we have zero
    breq zero					
    ldi temp1, '#'				; if not we have hash
    jmp convert_end
    star:
    ldi temp1, '*'				; set to star
    jmp convert_end
    zero:
    ldi temp1, '0'				; set to zero

    convert_end:
    ldi YL, low(KeypadValue)
    ldi YH, high(KeypadValue)
    st Y, temp1
    ret