#!/usr/bin/perl -w
sub scrap_course{
   #If argument is empty, which means cannot find more prerequisites, terminate the function.
   for my $course (@_){
      if(!$course){
         #print "!\n";
         return;
      }
      #print "Looking for $course\n";
      my $post_url = "http://www.handbook.unsw.edu.au/postgraduate/courses/2018/$course.html";
      my $under_url = "http://www.handbook.unsw.edu.au/undergraduate/courses/2018/$course.html";
      open my $F,'-|', "wget -q -O- $under_url $post_url" or die "Cannot wget: $!";
      #Scrap the current course's prerequisites
      while (my $line = <$F>) {
         if($line =~ /Prerequisite[s]?:([^<]+)/){
            my $course_msg = $1;
            $course_msg =~ s/^\s//g;
            $course_msg =~ s/\s$//g;
            while ($course_msg =~ /([a-z]{4}\d{4})/i){
               my $c = $1;
               #print "$c\n";
               $course_hash{$c} = 0;
               $course_msg =~ s/$c//i;
               scrap_course($c);
            }
         }
      }
   }
}
sub simple_scrap{
   if(@_ == 1){
      $argv = $_[0];
   }
   $post_url = "http://www.handbook.unsw.edu.au/postgraduate/courses/2018/$argv.html";
   $under_url = "http://www.handbook.unsw.edu.au/undergraduate/courses/2018/$argv.html";

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
}
if(@ARGV == 2){
   scrap_course($ARGV[1]);
   for $c (sort keys %course_hash){
   print "$c\n";
}
}
else{
   simple_scrap($ARGV[0]);
   for $c (sort keys %course_hash){
   print "$c\n";
}
}


