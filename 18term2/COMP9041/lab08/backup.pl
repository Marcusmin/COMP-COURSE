#!/usr/bin/perl -w
$filename = $ARGV[0];
open $f, '<', $filename;
while($line = <$f>){
   push @content, $line;
}
$counter = 0;
$backup_name = '.'.$filename.'.'."$counter";
while(-e $backup_name){
   $counter ++;
   $backup_name = '.'.$filename.'.'."$counter"
}
open $b_f, '>', $backup_name;
while(@content){
   $backup_line = shift @content;
   print $b_f $backup_line;
}
print "Backup of '$filename' saved as '$backup_name'\n";

