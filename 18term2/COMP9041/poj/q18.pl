#!/usr/bin/perl -w
while($line = <>){
    my @nums = split /\s+/, $line;
    for my $i (@nums){
        $total_nums{$i} = 0 if($i =~ /\d+/);
    }
}
$min = (sort keys %total_nums)[0];
$max = (sort {$b <=> $a} keys %total_nums)[0];
while($min <= $max){
    if(!defined $total_nums{$min}){
        print "$min\n";
        last;
    }
    $min ++;
}

