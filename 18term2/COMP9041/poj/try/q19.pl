#!/usr/bin/perl -w
use File::Compare;
for my $f (@ARGV){
    $hasRead{$f} = 0;
}
for my $f (@filenames){
    if($hasRead{$f} == 0){
        $hasRead{$f} = 1;
        my @has_same_content_files;
        push @has_same_content_files, $f;
        for my $another_file (@filenames){
            if($another_file ne $f && $hasRead{$another_file} == 0 && Compare($f, $another_file == 0)){
                push @has_same_content_files, $another_file;
                $hasRead{$another_file} = 1;
            }
        }
    }
    if(@has_same_content_files > 1){
        push @output, @has_same_content_files;
    }
}
for $e (@output){
    print "$e\n";
}
