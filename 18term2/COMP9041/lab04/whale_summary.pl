#!/usr/bin/perl -w
open F, '<', $ARGV[0] or die "Cannot open this file\n";
while($line = <F>){
   chomp $line;
   @data = split /\s+/, $line;
   my @whale_name;
   for $i (2..$#data){
      $whale_name[$i - 2] = $data[$i];
   }
   $whale_name = join ' ', @whale_name;
   $whale_name = lc $whale_name;
   chomp $whale_name;
   if($whale_name =~ m/s$/i){
      $whale_name =~ s/s$//g;
   }
   $whale_data{$whale_name}[0] += $data[1];
   $whale_data{$whale_name}[1] ++;
}
for $whale (sort keys %whale_data){
   print "$whale observations: $whale_data{$whale}[1] pods, $whale_data{$whale}[0] individuals\n";
}
