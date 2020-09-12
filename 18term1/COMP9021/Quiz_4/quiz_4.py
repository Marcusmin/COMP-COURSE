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
male_name_dict = defaultdict(list)
female_name_dict = defaultdict(list)
male_year_dict = defaultdict(list)
female_year_dict = defaultdict(list)
for filename in sorted(os.listdir(directory)):
    if not filename.endswith('.txt'):
        continue
    year = int(filename[3:7])
    with open(directory+'/'+filename) as data_file:
        for line in data_file:
            name,gender,count = line.split(',')
            count = int(count)
            if gender == 'M':
                male_name_dict[name].append([year,count])
                male_year_dict[year].append((name,count))
            else:
                female_name_dict[name].append([year,count])
                female_year_dict[year].append((name,count))
#calculate the frequncy of his name
#calculate the male's frequncy firstly
if male_name_dict[first_name]:
	for year_count in male_name_dict[first_name]:
		sum_of_all_name = 0
		for count_of_name_in_this_year in male_year_dict[year_count[0]]:
			sum_of_all_name += count_of_name_in_this_year[1]
		frequency = year_count[1] / sum_of_all_name
		year_count[1] = frequency
#do the same thing for female
if female_name_dict[first_name]:
	for year_count in female_name_dict[first_name]:
		sum_of_all_name = 0
		for count_of_name_in_this_year in female_year_dict[year_count[0]]:
			sum_of_all_name += count_of_name_in_this_year[1]
		frequency = year_count[1] / sum_of_all_name
		year_count[1] = frequency
#These two dics storing (year,frequency)
#sort them as frequency first, and then year is second
if male_name_dict[first_name]:
	most_popular_male_year = sorted(male_name_dict[first_name],key=lambda x:x[1],reverse=True)[0]
	min_male_frequency = most_popular_male_year[1]*100
	male_first_year = most_popular_male_year[0]
if female_name_dict[first_name]:
	most_popular_female_year = sorted(female_name_dict[first_name],key=lambda x:x[1],reverse=True)[0]
	min_female_frequency = most_popular_female_year[1]*100
	female_first_year = most_popular_female_year[0]


if not female_first_year:
    print(f'In all years, {first_name} was never given as a female name.')
else:
    print(f'In terms of frequency, {first_name} was the most popular '
          f'as a female name first in the year {female_first_year}.\n'
          f'  It then accounted for {min_female_frequency:.2f}% of all female names.'
         )
if not male_first_year:
    print(f'In all years, {first_name} was never given as a male name.')
else:
    print(f'In terms of frequency, {first_name} was the most popular '
          f'as a male name first in the year {male_first_year}.\n'
          f'  It then accounted for {min_male_frequency:.2f}% of all male names.'
         )

