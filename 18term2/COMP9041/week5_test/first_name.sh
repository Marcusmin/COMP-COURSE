#!/bin/sh
cat "$1" | cut -f1-3 -d'|'|egrep -i 'COMP[29]041'|sed 's/\s*$//g'|sort|uniq|cut -f3 -d'|'|cut -f2 -d','|sed 's/^\s*//g'|cut -f1 -d' '|sort|uniq -c|sort -n -r|head -1|egrep -oi '[a-z]+$'
