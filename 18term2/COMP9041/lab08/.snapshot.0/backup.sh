#!/bin/sh
filename="$1"
count=0
backup_name=".$filename.0"
while test -e $backup_name
do
   count=$(($count+1))
   backup_name=".$filename.$count"
done
cp "$filename" "$backup_name"
echo "Backup of '$filename' saved as '$backup_name'"
