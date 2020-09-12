#!/bin/bash
if test $# -ne 2
    then echo "Usage: ./echon.sh <number of lines> <string>"
    exit
fi
if ! test $1 -eq $1 2>/dev/null || test $1 -lt 0 2>/dev/null
    then echo "./echon.sh: argument 1 must be a non-negative integer"
    exit
fi
time=$1
while test $time -gt 0
do
echo $2
time=`expr $time - 1`
done
