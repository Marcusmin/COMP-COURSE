#!/usr/bin/perl -w
$file = $ARGV[0];
open my $f, '<', $file;
while ($line = <$f>){
   push @lines, $line;
}
@lines = sort {length $a <=> length $b || $a cmp $b} @lines;
for $l (@lines){
   print $l;
}
