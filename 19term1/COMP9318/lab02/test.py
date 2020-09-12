import submission as submission
x = [3, 1, 18, 11, 13, 17]
num_bins = 4
matrix, bins = submission.v_opt_dp(x, num_bins)
print("Bins = {}".format(bins))
print("Matrix =")
for row in matrix:
    print(row)
