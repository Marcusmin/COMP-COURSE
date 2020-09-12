/*
 * timer_try.asm
 *
 *  Created: 2018/10/25 17:31:47
 *   Author: liky
 */ 
 .include "m2560def.inc"

 .def led = r16
 ser led
 out DDRC, led;portc is output
 out PORTC, led;portc is light

