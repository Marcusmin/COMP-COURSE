#!/usr/bin/perl -w
if(@ARGV > 1){
   die "Too many arguments";
}
$filename = $ARGV[0];
open $inFILE, '<', $filename or die;

while($line = <$inFILE>){
   while($line =~ /\d/){
      $line =~ tr/[0-9]/#/;
   }
   push @lines, $line;
}
open $outFILE, '>' ,$filename or die;
while(@lines){
   $new_line = shift @lines;
   print $outFILE $new_line;
}
