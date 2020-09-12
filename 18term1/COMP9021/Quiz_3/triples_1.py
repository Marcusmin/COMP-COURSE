def break_into_digit(num):
    digit_list = []
    while num > 0:
       digit_list.append(num % 10)
       num = num // 10
    return sorted(digit_list)
count = 0
for i in range(10,100):
    if i % 11 == 0:
        continue
    digit_of_i = break_into_digit(i)
    for j in range(10,100):
        if j % 11 == 0:
            continue
        digit_of_j = break_into_digit(j)
        if len(digit_of_j + digit_of_i) != len(set(digit_of_j + digit_of_i)):
            continue
        for k in range(10,100):
            if k % 11 == 0:
                continue
            digit_of_k = break_into_digit(k)
            if len(digit_of_j + digit_of_i + digit_of_k) != len(set(digit_of_j + digit_of_i + digit_of_k)):
                continue
            result = i * j * k
            digit_of_result = break_into_digit(result)
            if digit_of_result == sorted(digit_of_k + digit_of_j + digit_of_i):
                count+=1
                print(f"{i}*{j}*{k}={result}")
print(count)
                    