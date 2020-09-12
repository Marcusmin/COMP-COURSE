#!/bin/sh
for i in Recogniser/*.vc
do
    name=`echo $i|cut -f 1 -d '.'`
    res=`echo "$name.sol"`
    # echo "$res"
    java VC.vc $i > test.sol
    echo "===$i===COMPARE====$res====================="
    diff test.sol "$res" || break
done
echo "=======$i content====================================="
cat "$i"
