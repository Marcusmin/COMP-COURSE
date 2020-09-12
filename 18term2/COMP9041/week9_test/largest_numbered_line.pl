#!/usr/bin/perl -w
@lines = <STDIN>;
for $l (@lines){
   chomp $l;
   @nums = $l =~ /((?:\-{1}\d*\.?\d+)|\d*\.\d+|\d+)/g;
   push @global_nums, @nums;
}
#print "@global_nums\n";
$max = $global_nums[0];
for $i (@global_nums){
   if($max < $i){
      $max = $i;
   }
}
for $l (@lines){
   @nums = $l =~ /((?:\-{1}\.?\d+)|\d*\.\d+|\d+)/g;
   for $n (@nums){
      if($n == $max){
         print $l,"\n";
         last;
      }
   }
}
