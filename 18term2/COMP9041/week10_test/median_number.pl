#!/usr/bin/perl -w
$middle_pos = (@ARGV - 1) / 2;
@sorted_args = sort {$a - $b} @ARGV;
print $sorted_args[$middle_pos],"\n";