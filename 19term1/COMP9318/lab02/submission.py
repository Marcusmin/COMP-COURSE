## import modules here 
import pandas as pd
import numpy as np



################# Question 1 #################
def cost(x):
    average = sum(x)/len(x)
    res = 0
    for i in x:
        res += (i - average)**2
    return res


def v_opt_dp(x, num_bins):# do not change the heading of the function
    table_of_suffix = [[0 for j in range(len(x))] for i in range(num_bins)]
    binning = [[] for i in range(len(x))]   #each el is a uniq binning way
    pre_binning = [[] for i in range(len(x))]
    for bin_counter in range(1, num_bins+1):
        for i in range(0, len(x)):
            suffix = x[i:]
            rest_bin = num_bins - bin_counter   #rest of bins
            rest_el = len(x) - len(suffix) #len of prefix
            if rest_el < rest_bin or len(suffix) < bin_counter:
                table_of_suffix[bin_counter-1][i] = -1
            elif bin_counter == 1: #base case
                table_of_suffix[bin_counter-1][i] = cost(suffix)
                binning[i] = [suffix]
            else:
                # pre_suffix = table_of_suffix[bin_counter-2][i+1:]
                suffix_cost = table_of_suffix[bin_counter-2]
                suffix_start = i + 1
                suffix_end = len(suffix_cost)
                min_opt = suffix_cost[suffix_start] + cost(x[i:suffix_start])
                opt_index = suffix_start
                #find optimal solution
                for j in range(suffix_start, suffix_end):
                    if suffix_cost[j] == -1: #invalid suffix
                        continue
                    else:
                        if min_opt > cost(x[i:j]) + suffix_cost[j]:  #better one
                            min_opt = cost(x[i:j]) + suffix_cost[j]
                            opt_index = j
                table_of_suffix[bin_counter-1][i] = min_opt
                binning[i] = [e for e in pre_binning[opt_index]] + [x[i:opt_index]]
        pre_binning = binning
    res = binning[0]
    return table_of_suffix,res[::-1]

    
