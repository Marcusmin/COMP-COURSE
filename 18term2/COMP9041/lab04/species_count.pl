#!/usr/bin/perl -w
open F, '<', $ARGV[1] or die "Cannot open this file\n";
$pod_count = 0;
$individual_count = 0;
while($line = <F>){
   chomp $line;
   if($line){
      @data = split /\s+/, $line;
      my @whale_name;
      for $i (2..$#data){
         $whale_name[$i - 2] = $data[$i];
      }
      $whale_name = join ' ', @whale_name;
      if($whale_name eq $ARGV[0]){
         $pod_count += 1;
         $individual_count += $data[1];
      }
   }
}
print "$ARGV[0] observations: $pod_count pods, $individual_count individuals\n"
