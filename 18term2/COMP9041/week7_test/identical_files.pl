#!/usr/bin/perl -w
if(@ARGV == 0){
   print "Usage: $0 <files>\n";
   exit 1;
}
for $file (@ARGV){
   $number ++;
   open $f, '<', $file or die "Cannot open file: $!";
   my $content = '';
   while($line = <$f>){
      $content .= $line;
   }
   if($number == 1){
      $content_of_file{$content} = 0;
      next;
   }
   if($number > 1){
      if (! defined $content_of_file{$content}){
         print "$file is not identical\n";
         exit 1;
      }
      else{
         $content_of_file{$content} = 0;
      }
   }
}
print "All files are identical\n";
