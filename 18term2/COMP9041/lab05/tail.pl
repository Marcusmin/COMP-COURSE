#!/usr/bin/perl -w
$nbOfLine = 10;
if($#ARGV >= 0){
   foreach $arg (@ARGV){
      if($arg =~ /^-[0-9]+$/){
         $nbOfLine = -$arg;
      }
      elsif($arg eq "--version"){
         print "$0: version 0.1\n";
      }
      else{
         push @files, $arg;
      }
   }
}
else{
   @lines = <STDIN>;
   $count = $nbOfLine - 1;
   $begin_line = $#lines - $count;
   if($begin_line < 0){
      $begin_line = 0;
   }
   while($begin_line <= $#lines){
      if(defined $lines[$begin_line]){
         print $lines[$begin_line];
      }
      $begin_line ++;
   }
}
foreach $file (@files){
   open F, '<', $file or die "$0: Can't open $file: $!\n";
   @lines = <F>;
   close F;
   $count = $nbOfLine - 1;
   $begin_line = $#lines - $count;
   if($begin_line < 0){
      $begin_line = 0;
   }
   if($#files > 0){
      print "==> $file <==\n";
   }
   while($begin_line <= $#lines){
      if(defined $lines[$begin_line]){
         print $lines[$begin_line];
      }
      $begin_line ++;
   }
}
