#! /usr/local/bin/bash
for i in `ls t*.vc` 
do
	echo $i:
	java VC.vc $i 
    diff ${i}u `basename $i .vc`.sol
done
