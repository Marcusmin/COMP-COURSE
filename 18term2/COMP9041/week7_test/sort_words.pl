#!/usr/bin/perl -w
while($line = <STDIN>){
   @words = split /\s/, $line;
   @words = sort @words;
   print join ' ', @words;
   print "\n";
}
