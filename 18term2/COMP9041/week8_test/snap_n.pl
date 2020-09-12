#!/usr/bin/perl -w
$max_time = $ARGV[0];
while($line = <STDIN>){
   $lines{$line} ++;
   for $e (keys %lines){
      if($lines{$e} >= $max_time){
         print "Snap: $e";
         exit 1;
      }
   }
   
}
