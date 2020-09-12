#!/bin/sh
for file in *.jpg
do
new_file=`echo "$file"|sed 's/\([^\.]*\)\..*/\1.png/'`
if test -e "$new_file"
then echo ""$new_file" already exists"
else
convert "$file" "$new_file" 2>/dev/null
rm $file 2>/dev/null
fi
done
