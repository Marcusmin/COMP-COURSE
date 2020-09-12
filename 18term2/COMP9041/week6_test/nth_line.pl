#!/usr/bin/perl -w
$nb_of_line = $ARGV[0];
$file_name = $ARGV[1];
open $f, '<', $file_name;
@lines = <$f>;
if(@lines < $nb_of_line){
   exit 1;
}
print "$lines[$nb_of_line - 1]";
