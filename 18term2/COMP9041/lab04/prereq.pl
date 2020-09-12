#!/usr/bin/perl -w
$post_url = "http://www.handbook.unsw.edu.au/postgraduate/courses/2018/$ARGV[0].html";
$under_url = "http://www.handbook.unsw.edu.au/undergraduate/courses/2018/$ARGV[0].html";

open F,'-|', "wget -q -O- $under_url $post_url" or die "Cannot wget: $1";
while ($line = <F>) {
    if($line =~ /Prerequisite[s]?:([^<]+)/){
      $course_msg = $1;
      $course_msg =~ s/^\s//g;
      $course_msg =~ s/\s$//g;
      while ($course_msg =~ /([a-z]{4}\d{4})/i){
         $c = $1;
         print "$c\n";
         $course_msg =~ s/$c//i;
      }
    }
}
