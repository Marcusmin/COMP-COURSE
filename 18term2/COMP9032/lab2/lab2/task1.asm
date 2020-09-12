.include "m2560def.inc"
.def al = r16
.def ah = r17

cpi al,low(65536)
cpc ah,high(65536)
breq end
adiw (al:ah),1

end:
nop
rjmp end