#!/bin/sh
for fold in "$@"
do
    for file in "$fold"/*.mp3
    do
        if test -e "$file"
        then
            title=`echo $file|cut -f2 -d'-'|sed 's/^\s*//g'`
            artist=`echo $file | cut -f3 -d'-'|sed 's/\.mp3//g' | sed 's/\s*$//g' | sed 's/^\s*//g'`
            album=`echo $file | cut -f1 -d'-'|sed 's/\/[0-9]*\s*$//g'|sed 's/^[^\/]*\///g'|sed 's/\/$//g'`
            year=`echo $file | cut -f2 -d',' | cut -f1 -d'-' | cut -f1 -d'/'| sed 's/^\s//g'`
            track=`echo $file | cut -f2 -d',' | cut -f1 -d'-' | sed 's/[0-9]*\///g' | sed 's/\s*//g'`
            id3 -t "$title" "$file" 1>/dev/null
            id3 -a "$artist" "$file" 1>/dev/null
            id3 -A "$album" "$file" 1>/dev/null
            id3 -y "$year" "$file" 1>/dev/null
            id3 -T "$track" "$file" 1>/dev/null
        fi
    done
done
