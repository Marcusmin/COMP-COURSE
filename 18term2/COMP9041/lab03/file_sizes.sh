#!/bin/bash
for file in *
do
test -d "$file" && continue
if test `cat -- "$file"|wc -l` -lt 10
then
Small="$Small $file"
elif test `cat -- "$file"|wc -l` -lt 100
then
Medium="$Medium $file"
else
Large="$Large $file"
fi
done
echo "Small files: $Small"
echo "Medium-sized files: $Medium"
echo "Large files: $Large"
