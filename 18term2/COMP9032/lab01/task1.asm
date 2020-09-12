;
; lab1.asm
;
; Created: 2018/7/31 21:34:54
; Author : liky
;


; Replace with your application code
.include "m2560def.inc"
.def a = r16
.def b = r17

loop:
cp a,b ;compare a and b
brlo branch1 ; if a < b, then turn to branch1
cp a,b
breq end
sub a, b ; if a >= b, then a = a - b
rjmp loop

branch1:
sub b,a ; if a < b, then b = b - a
rjmp loop ; go back to run loop instructions

end:
nop
rjmp end

