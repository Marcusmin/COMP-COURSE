#!/usr/bin/perl -w
while($line=<STDIN>){
    if($line =~ /[^0-9]*(\d+)/){    
        $initial_int = $1;
        push @ints, $initial_int;
        push @lines, $line;
    }
}
if(@ints){
    $largest_int = (sort {$b<=>$a} @ints)[0];
    $count = 0;
    for $i (@ints){
        if($i == $largest_int){
            print $lines[$count];
        }
        $count ++;
    }
}
