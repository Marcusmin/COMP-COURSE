#!/bin/sh
for file in *.htm
do
   newfile=`echo "$file" | sed 's/\([^\.]*\)\.htm/\1\.html/g'`
   if test -e "$newfile"
   then
      echo "$newfile exists"
      exit 1
   fi
   mv "$file" "$newfile"
done
