#!/bin/sh
for image in $@
do
if test -e "$image"
then
display "$image"
printf "Address to  e-mail this image to?"
read address
printf "Message to accompany image? "
read msg
echo "$msg"|mutt -s 'penguins!' -e 'set copy=no' -a "$image" -- "$address"
echo "penguins.png sent to $address"
else
echo "No Such File!"
continue
fi

done





