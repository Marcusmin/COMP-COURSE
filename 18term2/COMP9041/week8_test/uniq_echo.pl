#!/usr/bin/perl -w
$counter = 0;
for $w (@ARGV){
   $words{$w} = $counter if !defined $words{$w};
   $counter++;
}
for $e (sort {$words{$a} <=> $words{$b}} keys %words){
   print "$e ";
}
print "\n";
