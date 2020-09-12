#!/usr/bin/perl -w
open F, '<', $ARGV[0] or die "Cannot open this file!\n";
$counter = 0;
while($line = <F>){
   chomp $line;
   if(!$line){
      next;
   }
   @data = split /\s+/, $line;
   if($data[2] eq 'Orca'){
      $counter += $data[1];
   }
}
print "$counter Orcas reported in $ARGV[0]\n";


