;press button to switch on/off motor
;PB0 evokes external interrupt 0
;PB1 evokes external interrupt
;Mot connect to PORTF pin0
;default mode: motor stop
;external interrupt 0 drives motor
;external interrupt 1 stop motor


.include "m2560def.inc"

.def temp = r16
;interrupt vector
jmp RESET
.org int0addr
	rjmp EXT_INT0
;.org int1addr
;	rjmp EXT_INT1

RESET:
	;make port F pin0 to be output
	sbi DDRF, 0
	;port F pin 0 is high level(default mode: driving motor)
	cbi PORTF, 0
	;make port D to be input
	;enable interrupt 1 and 0
	in temp, EIMSK
	ori temp, (1<<int0)|(1<<int1)	;enable interrupt 0 and interrupt 1
	out EIMSK, temp
	;set interrupt mode to be falling edge triggered
	ldi temp, (2<<ISC00) | (2<<ISC10)
	sts EICRA, temp	;both falling edge 
	;set global interrupt
	sei
	;go to main
	rjmp main

EXT_INT0:
	;press PB0, go to drive motor
	sbi PORTF, 0
	reti
EXT_INT1:
	;press PB1, go to stop motor
	cbi PORTF, 0
	reti


;mot is low level

;mot is high level

main:
	rjmp main


.equ F_CPU = 16000000
.equ DELAY_1MS = F_CPU / 4 / 1000 - 4
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

sleep_1s:
	push r16
	ldi r16, 100
	sleep_loop:
		rcall sleep_5ms
		subi r16, 1
		brne sleep_loop
	pop r16
	ret