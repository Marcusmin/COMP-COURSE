#!/bin/sh
for cfile in $@
do
   for line in `egrep '#include' $cfile|egrep -oi '\".*\"'`
   do 
      headfile=`echo "$line"|sed 's/\"\(.*\)\"/\1/'`
      if ! test -e "$headfile"
      then
         echo "$headfile included into $cfile does not exist"
      fi
   done
done
