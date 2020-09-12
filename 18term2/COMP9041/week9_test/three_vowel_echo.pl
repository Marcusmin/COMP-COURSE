#!/usr/bin/perl -w
@words = @ARGV;
for $wd (@words){
   $w = lc $wd;
   @chars = split //, $w;
   $nb_of_vowels = 0;
   $max = 0;
   for $c (@chars){
      if ($c eq 'a' || $c eq 'e' || $c eq 'i' || $c eq 'o' || $c eq 'u'){
         $nb_of_vowels ++;
      }else{
         if($max < $nb_of_vowels){
            $max = $nb_of_vowels;
         }
         $nb_of_vowels = 0;
      }
   }
   if($max < $nb_of_vowels){
      $max = $nb_of_vowels;
   }
   if($max >= 3){
      print "$wd ";
   }
}
print "\n";
