#!/usr/bin/perl -w
$start = $ARGV[0];
$end = $ARGV[1];
$file_name = $ARGV[2];
open $FILE ,'>', $file_name;
for $i ($start..$end){
   print $FILE "$i\n";
}
close $FILE;
