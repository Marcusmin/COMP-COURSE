#!/usr/bin/perl -w
use File::Compare;


$dir1 = $ARGV[0];
$dir2 = $ARGV[1];
for my $f1 (glob "$dir1/*"){
    $f1Name = $f1;
    for my $f2 (glob "$dir2/*"){
        if(compare($f1Name, $f2) == 0){
            $f2 =~ s/$dir2\///s;
            $f1 =~ s/$dir1\///;
            if($f1 eq $f2){
               $filename{$f1} = 0;
               last;
            }
        }
    }
}
for my $e (sort keys %filename){
   print "$e\n";
}
