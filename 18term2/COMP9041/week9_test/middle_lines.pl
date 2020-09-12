#!/usr/bin/perl -w
$file = $ARGV[0];
open $f,'<', $file or die '';
@lines = <$f>;
if (!@lines){
   exit;
}
if(@lines % 2){
   $middle = @lines / 2;
   $middle = int $middle;
   print $lines[$middle];
}else{
   $middle = @lines / 2;
   $middle = int $middle;
   print $lines[$middle - 1];
   print $lines[$middle];
}
