'''
沙漏问题
'''
#要打印的个数
count = int(input())
#要打印的符号
signal = input()
#判断上半段的行数
nb_of_line = 0
for i in range(count):
    if 2 * (i ** 2) - 1 > count:
        nb_of_line = i - 1
        break
#输出沙漏上半段
print(nb_of_line)
up_half = nb_of_line
space_line = 0
while up_half > 0:
    for i in range(space_line):
        print(' ',end = '')
    for j in range(2 * up_half - 1):
        print(signal,end = '')
    print()
    space_line += 1
    up_half -= 1

down_half = 2
while down_half <= nb_of_line:
    for i in range(nb_of_line - down_half):
        print(' ',end = '')
    for j in range(2 * down_half - 1):
        print(signal, end = '')
    print()
    down_half += 1
