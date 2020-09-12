## import modules here 
from math import log10
################# Question 1 #################

def multinomial_nb(training_data, sms):# do not change the heading of the function
    # sms is a list of tokens
    # calculate the probability of being a spam or not
    # which is P(C1|sms) and P(C2|sms)
    # P(C1|sms) = P(C1) * P(sms|C1) / P(sms)
    # P(sms|C1) = P(v1|C1) * P(v2|C1) *... vi in sms
    # P(v1|C1) = Count(v1, C1) + 1 / Count(c1) + B
    # P(C1|sms) / P(C2|sms) = (P(C1) * P(sms|C1)) / (P(C2) * P(sms|C2))
    count_ham = 0
    count_spam = 0
    ham = 0
    spam = 0
    bag = set()
    cp_bag = set()
    ham_bag = dict()
    spam_bag = dict()
    for c in training_data:
        if c[1] == 'spam':
            count_spam += 1
            for v in c[0]:
                bag.add(v)
                spam += c[0][v]
                if v not in spam_bag:
                    spam_bag[v] = c[0][v]
                else:
                    spam_bag[v] += c[0][v]
        else:
            count_ham += 1
            for v in c[0]:
                bag.add(v)
                ham += c[0][v]
                if v not in ham_bag:
                    ham_bag[v] = c[0][v]
                else:
                    ham_bag[v] += c[0][v]
    pr_in_spam = 0
    pr_in_ham = 0
    for tk in sms:
        if tk not in bag:
            continue
        if tk not in spam_bag:
            pr =  1 / (spam + len(bag))
        else:
            pr = (spam_bag[tk]+1) / (spam + len(bag))

        pr_in_spam += log10(pr)
        if tk in ham_bag:
            pr = (ham_bag[tk]+1) / (ham + len(bag))

        else:
            pr = 1 / (ham + len(bag))
        pr_in_ham += log10(pr)
    pr_in_spam += log10(count_spam/(count_ham + count_spam))
    # calculate the probability of being a ham
    pr_in_ham += log10(count_ham/(count_ham + count_spam))
    return 10**pr_in_spam / 10**pr_in_ham
