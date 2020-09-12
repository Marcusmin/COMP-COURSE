#!/usr/bin/perl -w
sub allTiedUp{
    my (%candidates) = @_;
    my $first = (values %candidates)[0];
    for my $i (values %candidates){
        if ($i != $first){
            return 0;
        }
    }
    return 1;
}
($candidate_file, @votes_file) = @ARGV;
#check is email valid
#read candidates name
open my $f, '<', "$candidate_file";
while($name = <$f>){
    chomp $name;
    push @candidates, $name;
}
for $vote (@votes_file){
    %candidateHash =();
    my @voteName = ();
    open my $email, '<', "$vote";
    #read candidate name from email
    while($line = <$email>){
    chomp $line;
    for $candidate (@candidates){
        $removeSpaceName = $candidate;
        $removeSpaceName =~ s/\s+//;
        if($line =~ /^\s*$candidate\s*$/i || $line =~ /^\s*$removeSpaceName\s*$/i){
            push @voteName, $candidate;
            $candidateHash{$candidate} = 0;
            last;
        }
    }
    }
    if(keys %candidateHash != @candidates){
        print "$vote is not a valid vote\n";
    }else{
        #count first vote
        push @total_votes, \@voteName;
    }
}
#read total votes
for my $v (@total_votes){
    #print "$v";
    $candidates_count{$$v[0]}++;   
}
$total_vote_count = @total_votes;
#caculate vote result
for my $c (keys %candidates_count){
    $probOfWin{$c} = $candidates_count{$c} / $total_vote_count;
}
$max = (sort {$probOfWin{$b} <=> $probOfWin{$a}} keys %probOfWin)[0];
while($probOfWin{$max} < 0.5 && !allTiedUp(%candidates_count)){
    my @weakest_candidates;
    $weakest_candidate_value = (sort {$a<=>$b} values %candidates_count)[0];
    for my $v (keys %candidates_count){
        if($weakest_candidate_value == $candidates_count{$v}){
            push @weakest_candidates, $v;
        }
    }
    #elimate weakest candidate
    for my $c (@weakest_candidates){
        for my $v (@total_votes){
            if(@$v[0] eq $c){
                #print "weakest is $c\n";
                shift @$v;
            }
        }
    }
    %candidates_count = ();
    $total_vote_count = @total_votes;
    for my $v (@total_votes){
        $candidates_count{@$v[0]}++;   
    }
    for my $c (keys %candidates_count){
        $probOfWin{$c} = $candidates_count{$c} / $total_vote_count;
        #print "$c $probOfWin{$c}\n"
    }
    #print "$max $probOfWin{$max}\n";
    $max = (sort {$probOfWin{$b} <=> $probOfWin{$a}} keys %probOfWin)[0];
}
if(allTiedUp(%candidates_count)){
    print join " ", sort keys %candidates_count;
    print "\n";
}else{
    print "$max\n";
}
