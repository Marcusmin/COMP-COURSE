#!/usr/bin/perl -w
#open $f,'<', "try.txt" or die
@array = (1..100);
@narray = (200,400);
@array += @narray;
for $i (@array[50..$#array]){
   print "$i  ";
}
