#!/usr/bin/perl -w
sub count_word{
   my $specified_word = $_[0];
   my $counter = 0;
   open my $f, '<', $_[1];
   while(my $line = <$f>){
      my @words = split /[^a-z]/i, $line;
      for $w (@words){
         if(lc $w eq lc $specified_word){
            $counter += 1;
         }
      }
   }
   return $counter;
}
sub total_words{
   my $counter = 0;
   open my $f, '<', $_[0];
   while (my $line = <$f>){
      my @words = split /[^a-z]/i, $line;
      @words = sort @words;
      for my $w (@words){
         if(! $w eq ''){
            $counter += 1;
         }
      }
   }
   return $counter;
}
for $file (glob "lyrics/*.txt"){
   #$artist =~ s/lyrics\//ig $filename;
   $data{$file}[0] = count_word($ARGV[0], $file);
   $data{$file}[1] = total_words($file);
   $data{$file}[2] =  count_word($ARGV[0], $file) / total_words($file);
}
for $name (sort keys %data){
   $new_name = $name;
   $new_name =~ s/lyrics\///;
   $new_name =~ s/\.txt//;
   $new_name =~ s/_/ /ig;
   printf "%4d/%6d = %.9f %s\n", ($data{$name}[0], $data{$name}[1],$data{$name}[2], $new_name);
}
