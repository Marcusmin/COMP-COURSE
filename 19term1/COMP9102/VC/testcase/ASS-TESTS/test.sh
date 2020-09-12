#/bin/sh
rm out
for f in *.vc
do	
	
	echo "trying $f"
	echo "trying $f" >> out
	java VC.vc "$f" >> out
	y=${f%.vc}
	echo "$y"
	if test -e "$y.j"
	then
		jasmin "$y.j"
		java "$y"
	fi
done
