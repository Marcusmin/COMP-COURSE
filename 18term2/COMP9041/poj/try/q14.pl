#!/usr/bin/perl -w
for $i (@ARGV){
    if($i =~ /^\d+$/){
        push @int, $i;
    }
}
@int = sort @int;
for $i (@int){
    print "$i\n";
}
