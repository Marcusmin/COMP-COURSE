#!/bin/sh
while read line
do
   whole="$whole$line"
done
echo "$whole" | sort
o_file= `cat $1`
echo "$o_file"
