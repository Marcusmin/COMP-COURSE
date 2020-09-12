def check_line_direction(nb):
    codes = {8:0, 4:0, 2:0, 1:0}
    for key in codes:
        if nb >= key:
            print(key)
            codes[key] += 1
            nb = nb - key
    print(codes)
