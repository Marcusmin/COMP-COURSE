# Uses National Data on the relative frequency of given names in the population of U.S. births,
# stored in a directory "names", in files named "yobxxxx.txt with xxxx being the year of birth.
#
# Prompts the user for a first name, and finds out the first year
# when this name was most popular in terms of frequency of names being given,
# as a female name and as a male name.
# 
# Written by *** and Eric Martin for COMP9021


import os
from collections import defaultdict

first_name = input('Enter a first name: ')
directory = 'names'
min_male_frequency = 0
male_first_year = None
min_female_frequency = 0
female_first_year = None

# Replace this comment with your code
male_name = 
female_name_year_count = []
year_dict = defaultdict(list)
for filename in sorted(os.listdir(directory)):
    if not filename.endswith('.txt'):
        continue
    year = int(filename[3:7])
    with open(directory+'\\'+filename) as data_file:
        for line in data_file:
            name,gender,count = line.split(',')
            count = int(count)
            if gender == 'M':
                male_name_year_count.append((year,count))
            else:
                female_name_year_count.append((year,count))
            year_dict[year].append((name,count))
print(year_dict)
#if not female_first_year:
    #print(f'In all years, {first_name} was never given as a female name.')
#else:
    #print(f'In terms of frequency, {first_name} was the most popular '
          #f'as a female name first in the year {female_first_year}.\n'
          #f'  It then accounted for {min_female_frequency:.2f}% of all female names.'
         #)
#if not male_first_year:
 #   print(f'In all years, {first_name} was never given as a male name.')
#else:
 #   print(f'In terms of frequency, {first_name} was the most popular '
  #        f'as a male name first in the year {male_first_year}.\n'
   #       f'  It then accounted for {min_male_frequency:.2f}% of all male names.'
    #     )

