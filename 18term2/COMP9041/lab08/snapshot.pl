#!/usr/bin/perl -w
use File::Copy;
use Cwd;
sub save{   #save
   my $counter = 0;
   my $new_dir = ".snapshot".".$counter";
   while(-e $new_dir){
      $counter++;
      $new_dir = ".snapshot".".$counter";
   }
   mkdir $new_dir;
   for my $file (glob "*.*"){
      if($file =~ /^snapshot.pl/){
         next;
      }
      copy $file, $new_dir;
   }
   print "Creating snapshot $counter\n";
}
sub loads{
   save();
   my $n = $_[0];
   my $target_dir = ".snapshot.".$n;
   my $current_dir = ".";
   opendir(my $cd, $target_dir);
   @current_files = readdir $cd;
   for my $f (@current_files){
      if(-f "$target_dir/$f"){
         copy "$target_dir/$f", $current_dir;
      }
   }
   print "Restoring snapshot $n\n";
}
if(@ARGV == 2){
   $n = $ARGV[1];
   loads($n);
}else{
   save();
}
