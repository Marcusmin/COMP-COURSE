#!/bin/sh
dir_1=$1
dir_2=$2

content_1=`ls $dir_1`
content_2=`ls $dir_2`
if test -z "$content_1" || test -z "$content_2"
then
    exit
fi

for file_1 in "$dir_1"/*
do
    for file_2 in "$dir_2"/*
    do
        res=`diff "$file_1" "$file_2" 2>/dev/null`
        if test -z "$res"
        then
            file_1_name=`echo "$file_1" | sed s/"$dir_1\/"//`
            file_2_name=`echo "$file_2" | sed s/"$dir_2\/"//`
            if test "$file_1_name" = "$file_2_name"
            then
                echo "$file_1_name"
                break
            fi
        fi
    done
done
