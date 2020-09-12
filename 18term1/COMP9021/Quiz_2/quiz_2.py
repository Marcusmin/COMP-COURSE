# Written by *** and Eric Martin for COMP9021


'''
Prompts the user for two strictly positive integers, numerator and denominator.

Determines whether the decimal expansion of numerator / denominator is finite or infinite.

Then computes integral_part, sigma and tau such that numerator / denominator is of the form
integral_part . sigma tau tau tau ...
where integral_part in an integer, sigma and tau are (possibly empty) strings of digits,
and sigma and tau are as short as possible.
'''


import sys
from math import gcd


try:
    numerator, denominator = input('Enter two strictly positive integers: ').split()
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()
try:
    numerator, denominator = int(numerator), int(denominator)
    if numerator <= 0 or denominator <= 0:
        raise ValueError
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()


has_finite_expansion = False
integral_part = 0
sigma = ''
tau = ''

def is_finite(num,denum):
    denum = denum // gcd(num,denum)
    while denum != 1:
        if denum % 2 == 0:
            denum = denum // 2
        elif denum % 5 == 0:
            denum = denum // 5
        else:
            return False
    if denum == 1:
        return True

def find_loop(reminder_recorder):
    for i in range(len(reminder_recorder) - 1):
        for j in range(i + 1, len(reminder_recorder)):
            if reminder_recorder[i] == reminder_recorder[j]:
                return i,j

def find_decimal(num,divisor):
    reminder_recorder = []
    reminder = num % divisor
    reminder_recorder.append(reminder)
    decimal = ''
    for i in range(999999):
        s = str(reminder * 10 // divisor)
        decimal += s
        reminder = (reminder * 10 % divisor)
        reminder_recorder.append(reminder)
    loop_trace = find_loop(reminder_recorder)
    beginner = loop_trace[0]
    ender = loop_trace[1]
    if beginner == 0:
        sigma = ''
        tau = decimal[beginner:ender]
    else:
        sigma = decimal[:beginner]
        tau = decimal[beginner:ender]
    return  sigma,tau


if numerator % denominator == 0:
    has_finite_expansion = True
    integral_part = numerator // denominator
else:
    if is_finite(numerator,denominator):
        has_finite_expansion = True
        integral_part = numerator // denominator
        reminder = numerator % denominator
        while reminder:
            sigma += str(reminder * 10 // denominator)
            reminder = reminder * 10 % denominator
        print(sigma)
    else:
        has_finite_expansion = False
        integral_part = numerator // denominator
        decimal = find_decimal(numerator, denominator)
        sigma = decimal[0]
        tau = decimal[1]

# Replace this comment with your code

if has_finite_expansion:
    print(f'\n{numerator} / {denominator} has a finite expansion')
else:
    print(f'\n{numerator} / {denominator} has no finite expansion')
if not tau:
    if not sigma:
        print(f'{numerator} / {denominator} = {integral_part}')
    else:
        print(f'{numerator} / {denominator} = {integral_part}.{sigma}')
else:
    print(f'{numerator} / {denominator} = {integral_part}.{sigma}({tau})*')

