for image in $@
do
date=`ls -l $image |cut -f6-8 -d' '`
convert -gravity south -pointsize 36 -draw "text 0,10 '$date'" "$image" "$image"
done
