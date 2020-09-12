#!/usr/bin/perl -w
($filename, $digits) = @ARGV;
open my $f,'<', $filename;
while($line = <$f>){
    @words_in_line = split /\s+/, $line;
    push @words, @words_in_line;
}
%map = (
    2=>"abc",
    3=>"def",
    4=>"ghi",
    5=>"jkl",
    6=>"mno",
    7=>"pqrs",
    8=>"tuv",
    9=>"wxyz"
);
for my $i (keys %map){
    for my $c (split //, $map{$i}){
        $ctod{$c} = $i;
    }
}
for my $w (@words){
    #convert words to digits
    my @chars = split //, $w;
    my $words_to_digits;
    for my $c (@chars){
       $words_to_digits .= $ctod{$c};
    }
    if ($words_to_digits eq $digits){
        push @matched, $w;
    }
}
for my $w (@words){
    #convert words to digits
    my @chars = split //, $w;
    my $words_to_digits;
    for my $c (@chars){
       $words_to_digits .= $ctod{$c};
    }
    for my $another_w (@words){
        my @another_chars = split //, $another_w;
        my $words_to_digits2;
        for my $c1 (@another_chars){
           $words_to_digits2 .= $ctod{$c1};
        }
        $sum_up_digits =  $words_to_digits.$words_to_digits2;
        if ($sum_up_digits eq $digits){
            push @matched, "$w $another_w";
        }
    }
}
for my $i (@matched){
    print "$i\n";
}
