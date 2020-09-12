#!/usr/bin/perl -w

for my $file (glob "lyrics/*.txt"){
   open my $f, '<', $file or die "Cannot open $file: $!";
   my $name = $file;
   my $counter;
   $name =~ s/lyrics\///;
   $name =~ s/\.txt//;
   $name =~ s/_/ /ig;
   #Count total words
   while (my $line = <$f>){
      my @words = split /[^a-z]/i, $line;
      for my $w (@words){
         if(! $w eq ''){
            $counter += 1;
            $data{$name}{lc $w} += 1;
         }
      }
   }
   $data{$name}{'#'} = $counter; 
}

for my $file (@ARGV){
   open my $f,'<', $file or die "Cannot open $file: $!\n";
   my %artist_probability;
   my %words_probability;
   while (my $line = <$f>){
      my @words = split /[^a-z]/i, $line;
      for my $w (@words){
         $w = lc $w;
         if(! lc $w eq ''){
            for my $artist (keys %data){
               if(exists $data{$artist}{$w}){
                  $words_probability{$w}{$artist} = log (($data{$artist}{$w} + 1) / ($data{$artist}{'#'}));
               }else{
                  $words_probability{$w}{$artist} = log (1 / ($data{$artist}{'#'}));
               }
               
            }
            for $artist (keys %{$words_probability{$w}}){
               $artist_probability{$artist} += $words_probability{$w}{$artist};
            }
         }
      }
   }
   $song_probability{$file} = {%artist_probability};
   close $f;
}

for $song (sort keys %song_probability){
   my @artists = sort {$song_probability{$song}{$a} <=> $song_probability{$song}{$b}} keys %{$song_probability{$song}};
   $name = $artists[$#artists];
   printf "%s most resembles the work of %s (log-probability=%.1f)\n", ($song, $name , $song_probability{$song}{$name});
}

