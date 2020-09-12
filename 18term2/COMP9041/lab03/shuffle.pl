#!/usr/bin/perl -w
$counter = 0;
@lines = <STDIN>;
$marks[$#lines] = 0;
for $var (@marks){
   $var = 0;
}
while ($counter <= $#lines){
   $randNum = int(rand($#lines + 1));
   if($marks[$randNum] == 0){
      $marks[$randNum] = 1;
      print $lines[$randNum];
      $counter++;
   }
   else{
      next;
   }
}
