#!/usr/bin/perl -w
for $i (<STDIN>){
   $i =~ s/[^\([a-z]{4}\d{4}\)]//ig;
   print $i;
}
