#!/usr/bin/perl -w
$counter = 0;
while ($line = <STDIN>){
   @words = split /[^a-z]/i, $line;
   @words = sort @words;
   for $w (@words){
      if(! $w eq ''){
         $counter += 1;
      }
   }
}
print "$counter words\n";
