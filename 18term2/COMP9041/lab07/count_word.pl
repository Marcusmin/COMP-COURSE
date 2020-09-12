#!/usr/bin/perl -w
$specified_word = $ARGV[0];
$counter = 0;
while($line = <STDIN>){
   @words = split /[^a-z]/i, $line;
   for $w (@words){
      if(lc $w eq lc $specified_word){
         $counter += 1;
      }
   }
}
print "$specified_word occurred $counter times\n";
