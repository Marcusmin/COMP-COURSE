#!/usr/bin/perl -w
if($#ARGV != 1){
   print "Usage: $0 <number of lines> <string>\n";
   exit;
}
$times = $ARGV[0];
$string = $ARGV[1];
if($times =~ /^[0-9]+$/){
   while($times > 0){
      print $string;
      print "\n";
      $times --;
   }   
}
else{
   print "./echon.pl: argument 1 must be a non-negative integer\n";
   exit;
}

