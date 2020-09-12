import sys
from collections import deque

def get_gcd(a, b):
    if b == 0:
        return a
    else:
        return get_gcd(b, a % b)

def is_same_list(L_1, L_2):
    if len(L_1) != len(L_2):
        return False
    for i in range(len(L_1)):
        if L_1[i] != L_2[i]:
            return False
    else:
        return True

##each number in frieze.txt is a class which store the value
##and its directions
class code_of_frieze:
    def __init__(self, value = None):
        self.value = value
        self.direction = self.find_direction_of_code()
    def find_direction_of_code(self):
        self.direct = {8:0, 4:0, 2:0, 1:0}
        self.d_checker = self.value
        for key in self.direct:
            if self.d_checker >= key:
                self.direct[key] += 1
                self.d_checker = self.d_checker - key
        return self.direct

class FriezeError(Exception):
    def __init__(self, message):
        self.message = message

class Frieze:
    def __init__(self, frieze_name = None):
        self.frieze_name = frieze_name
        self.frieze_txt = self.read_txt_code()
        self.codes_of_frieze = self.read_code_of_frieze()
        is_correct_freize = self.is_format_correct() and self.is_intersection() and self.is_number_of_line_correct() and (self.find_period() >= 2)
        if is_correct_freize:
            ##print('Correct input')
            pass
        else:
            raise FriezeError('Incorrect Input')


    ##Read some_freize_flie.txt and store it into a array
    def read_txt_code(self):
        try:
            with open(self.frieze_name) as frieze_data:
                self.frieze_code = []
                self.frieze_line = frieze_data.readline()
                while self.frieze_line:
                    self.frieze_code.append(self.frieze_line.split(' '))
                    self.frieze_line = frieze_data.readline()
                self.new_frieze_code = []
                for line in self.frieze_code:
                    while '\n' in line:
                        line.remove('\n')
                    while '' in line:
                        line.remove('')
                    if len(line) > 0:
                        self.new_frieze_code.append(line)
                for line in range(len(self.new_frieze_code)):
                    for e in range(len(self.new_frieze_code[line])):
                        self.new_frieze_code[line][e] = int(self.new_frieze_code[line][e])
                return self.new_frieze_code
        except FileNotFoundError:
            sys.exit()

    ##Transform the array from read_txt_code() function
    def read_code_of_frieze(self):
        self.fcode = [[1] * len(self.new_frieze_code[line]) \
                 for line in range(len(self.new_frieze_code))]
        for line in range(len(self.new_frieze_code)):
            for e in range(len(self.new_frieze_code[line])):
                self.fcode[line][e] = code_of_frieze(self.new_frieze_code[line][e])
        return self.fcode

    ##The limitation of frieze's height and length
    def is_number_of_line_correct(self):
        ##Check height
        if len(self.frieze_txt) < 2 + 1 or len(self.frieze_txt) > 16 + 1:
            print('Wrong height')
            return False
        self.len_of_row = len(self.frieze_txt[0])
        for row in range(len(self.frieze_txt)):
            ##Check if each row satisfy the number limit.
            if len(self.frieze_txt[row]) > 50 + 1 or len(self.frieze_txt[row]) < 4 + 1:
                print('Wrong length')
                return False
            ##Each row should have same number of numbers
            if len(self.frieze_txt[row]) != self.len_of_row:
                print('Each row should have same number of numbers')
                return  False
            for col in range(len(self.frieze_txt[row])):
                ##Check if each number in frieze is less than 15 and larger than 0
                if self.frieze_txt[row][col] not in set(range(0,16)):
                    print('Number in rectangle should be less than 16 and larger than or equel to 0')
                    return False
        return True

    ##The should be no intersection between points
    def is_intersection(self):
        for line in range(1,len(self.codes_of_frieze)):
            for col in range(len(self.codes_of_frieze[line]) - 1):
                if self.codes_of_frieze[line][col].direction[2] != 0:
                    if self.codes_of_frieze[line - 1][col].direction[8] != 0:
                        print('Intersection with 2 and 8')
                        return False
                if self.codes_of_frieze[line][col].direction[8] != 0:
                    if self.codes_of_frieze[line + 1][col].direction[2] != 0:
                        print('Intersection with 8 and 2')
                        return False
        return True

    #Check if it is a correct frieze
    def is_format_correct(self):
        ##The last number of first row should be zero
        if self.frieze_txt[0][-1] != 0:
            print('Last number of first row should be 0')
            return False
        ##The number on rightmost colum should be 0 or 1
        for line in self.frieze_txt:
            if line[-1] != 0 and line[-1] != 1:
                print('Rightmost column should have no number except 1 and 0')
                return False
        ##The number of first line should have no direction 1  and last line of frieze should have no 8
        for i in self.codes_of_frieze[0]:
            if i.direction[1] != 0 or i.direction[2] != 0:
                print('First row frieze should not have 1')
                return False
        for j in self.codes_of_frieze[-1]:
            if j.direction[8] != 0:
                print('Last row of frieze should not have 8')
                return False
        return True


    def display(self):
        tex_file_name = self.frieze_name[:-4] + '.tex'
        with open(tex_file_name, 'w') as tex_file_data:
            print('\\documentclass[10pt]{article}\n'
                  '\\usepackage{tikz}\n'
                  '\\usepackage[margin=0cm]{geometry}\n'
                  '\\pagestyle{empty}\n'
                  '\n'
                  '\\begin{document}\n'
                  '\n'
                  '\\vspace*{\\fill}\n'
                  '\\begin{center}\n'
                  '\\begin{tikzpicture}[x=0.2cm, y=-0.2cm, thick, purple]',file = tex_file_data
            )
            print('% North to South lines', file = tex_file_data)
            for i in range(len(self.codes_of_frieze[0])):
                consecutive_v_line = []
                verticle_line = deque()
                for j in range(1, len(self.codes_of_frieze)):
                    if self.codes_of_frieze[j][i].direction[1] != 0:
                        v_c = [(i, j - 1), (i, j)]
                        if verticle_line.__len__() == 0:
                            verticle_line.append([(i, j - 1), (i, j)])
                        else:
                            coordinate = verticle_line.popleft()
                            if v_c[0] == coordinate[1]:
                                coordinate[1] = v_c[1]
                                verticle_line.appendleft(coordinate)
                            else:
                                verticle_line.appendleft(v_c)
                                consecutive_v_line.append(coordinate)
                    else:
                        pass
                while verticle_line:
                    consecutive_v_line.append(verticle_line.popleft())
                for cr in consecutive_v_line:
                    print(f'    \draw ({cr[0][0]},{cr[0][1]}) -- ({cr[1][0]},{cr[1][1]});', file = tex_file_data)
            print('% North-West to South-East lines', file = tex_file_data)
            consecutive_nw_to_se_line = []
            for i in range(len(self.codes_of_frieze)):
                nw_se_in_each_row = deque()
                for j in range(len(self.codes_of_frieze[i])):
                    if self.codes_of_frieze[i][j].direction[8] != 0:
                        nw_to_se = [(j, i), (j + 1, i + 1)]
                        nw_se_in_each_row.append(nw_to_se)
                if not consecutive_nw_to_se_line:
                    while nw_se_in_each_row:
                        consecutive_nw_to_se_line.append(nw_se_in_each_row.popleft())
                else:
                    while nw_se_in_each_row:
                        l = nw_se_in_each_row.popleft()
                        for line in consecutive_nw_to_se_line:
                            if line[1] == l[0]:
                                line[1] = l[1]
                                break
                        else:
                            consecutive_nw_to_se_line.append(l)
            for i in consecutive_nw_to_se_line:
                print(f'    \draw ({i[0][0]},{i[0][1]}) -- ({i[1][0]},{i[1][1]});', file = tex_file_data)
            
            print('% West to East lines', file = tex_file_data)
            for i in range(len(self.codes_of_frieze)):
                consecutive_hor_line = []
                horizental_line = deque()
                for j in range(len(self.codes_of_frieze[i])):
                    if self.codes_of_frieze[i][j].direction[4] != 0:
                        h_c = [(j, i), (j + 1, i)]
                        if not horizental_line:
                            horizental_line.append(h_c)
                        else:
                            h_coordinate = horizental_line.popleft()
                            if h_coordinate[1] == h_c[0]:
                                h_coordinate[1] = h_c[1]
                                horizental_line.appendleft(h_coordinate)
                            else:
                                consecutive_hor_line.append(h_coordinate)
                                horizental_line.appendleft(h_c)
                while horizental_line:
                    consecutive_hor_line.append(horizental_line.pop())
                for hc in consecutive_hor_line:
                    print(f'    \draw ({hc[0][0]},{hc[0][1]}) -- ({hc[1][0]},{hc[1][1]});', file = tex_file_data)

            print('% South-West to North-East lines', file = tex_file_data)
            consecutive_SW_to_NE_line = []
            for i in range(1, len(self.codes_of_frieze)):
                SW_to_NE_in_each_row = deque()
                for j in range(len(self.codes_of_frieze[i])):
                    if self.codes_of_frieze[i][j].direction[2] != 0:
                        SW_to_NE = [(j, i), (j + 1, i - 1)]
                        SW_to_NE_in_each_row.append(SW_to_NE)
                if not consecutive_SW_to_NE_line:
                    while SW_to_NE_in_each_row:
                        consecutive_SW_to_NE_line.append(SW_to_NE_in_each_row.popleft())
                else:
                    while SW_to_NE_in_each_row:
                        l = SW_to_NE_in_each_row.popleft()
                        for line in consecutive_SW_to_NE_line:
                            if l[1] == line[0]:
                                line[0] = l[0]
                                break
                        else:
                            consecutive_SW_to_NE_line.append(l)
            ##sort the lines
            consecutive_SW_to_NE_line = sorted(consecutive_SW_to_NE_line, key = lambda line: (line[0][1], line[0][0]))
            for i in consecutive_SW_to_NE_line:
                print(f'    \draw ({i[0][0]},{i[0][1]}) -- ({i[1][0]},{i[1][1]});', file = tex_file_data)

            print('\\end{tikzpicture}\n'
                  '\\end{center}\n'
                  '\\vspace*{\\fill}\n'
                  '\n'
                  '\end{document}'
                  , file = tex_file_data
                  )
    def find_period(self):
        period_list = []
        for row in range(0, len(self.frieze_txt)):
            can_find_period = False
            len_of_codes = len(self.frieze_txt[row]) - 1
            #if row == 0 or row == len(self.frieze_txt) - 1:
                #len_of_codes = len_of_codes - 1
            for p in range(1, len_of_codes // 2 + 1):
                for e in range(len_of_codes - p):
                    if self.frieze_txt[row][e] != self.frieze_txt[row][e + p]:
                        break
                else:
                    if p == 1:
                        break
                    else:
                        can_find_period = True
                        period = p
                        period_list.append(period)
                if can_find_period:
                    #print(f'row {row} has peroid {period}')
                    break
                #else:
                    #print(f'row {row} has no peroid')
        #print(period_list)
        if period_list:
            dq = deque(period_list)
            while len(dq) > 1:
                f = dq.popleft()
                gcd = get_gcd(f, dq[0])
                lcm = f * dq[0] // gcd
                dq[0] = lcm
            return dq[0]
        else:
            return False
        
    def is_verticle_reflection(self):
        ##Axis of Symmetry is the boundary of two number
        is_reflective = False
        period = self.find_period()
        for i in range(period + 1):
            #print(i,end = '    ')
            can_every_pass_test = True
            ##There is a series of axis in this frieze and every axis should be vaild
            for e in range(0, len(self.codes_of_frieze[0]) // period):
                can_pass_test = True
                if (i + e * period) * 2 + 1 >= len(self.codes_of_frieze[0]) - 1:
                    break
                ##Do not need to test the axis exceed the half peroid
                if (i + e * period) >= len(self.codes_of_frieze[0]) // 2:
                    break
                    ##To estimate if current i + e * peroid is a symmetry axis
                else:
                        ##Symmetry Test
                    for row in range(len(self.codes_of_frieze)):
                        for col in range(0, i + e * period + 1):
                            if self.codes_of_frieze[row][col].direction[1] != 0:
                                if self.codes_of_frieze[row][(i + e * period) * 2 - col + 1].direction[1] == 0:
                                    #print(f'{i}+{e} on (row{row},col{col}) Error happens at direction [1]' )
                                    can_pass_test = False
                                    break
                            if self.codes_of_frieze[row][col].direction[4] != 0:
                                if self.codes_of_frieze[row][(i + e * period) * 2 - col].direction[4] == 0:
                                    #print(f'{i}+{e} on (row{row},col{col}) Error happens at direction [4]' )
                                    can_pass_test = False
                                    break
                            if self.codes_of_frieze[row][col].direction[2] != 0:
                                if self.codes_of_frieze[row - 1][(i + e * period) * 2 - col].direction[8] == 0:
                                    #print(f'{i}+{e} on (row{row},col{col}) Error happens at direction [2]' )
                                    can_pass_test = False
                                    break
                            if self.codes_of_frieze[row][col].direction[8] != 0:
                                if self.codes_of_frieze[row + 1][(i + e * period) * 2 - col].direction[2] == 0:
                                    #print(f'{i}+{e} on (row{row},col{col}) Error happens at direction [8]' )
                                    can_pass_test = False
                                    break
                        if can_pass_test == False:
                            can_every_pass_test = False
                            break
            if can_every_pass_test:
                #print('You are verticle relective')
                return True

        for i in range(period + 1):
            is_valid_axis = False
            for e in range(0, len(self.codes_of_frieze[0]) // period):
                can_pass_test = True
                if (i + e * period) * 2 + 1 >= len(self.codes_of_frieze[0]) - 1:
                    break
                if (i + e * period) >= len(self.codes_of_frieze[0]) // 2:
                    break
                else:
                    for row in range(len(self.codes_of_frieze)):
                        if self.codes_of_frieze[row][i + e * period].direction[4] != 0:
                            if self.codes_of_frieze[row][i + e * period - 1].direction[4] == 0:
                                can_pass_test = False
                                break
                        if self.codes_of_frieze[row][i + e * period].direction[2] != 0:
                            if self.codes_of_frieze[row - 1][i + e * period - 1].direction[8] == 0:
                                can_pass_test = False
                                break
                        if self.codes_of_frieze[row][i + e * period].direction[8] != 0:
                            if self.codes_of_frieze[row + 1][i + e * period - 1].direction[2] == 0:
                                can_pass_test = False
                                break
                    for row in range(len(self.codes_of_frieze)):
                        for col in range(i + e * period):
                            if self.codes_of_frieze[row][col].direction[1] != 0:
                                if self.codes_of_frieze[row][(i + e * period) * 2 - col].direction[1] == 0:
                                    can_pass_test = False
                                    break
                            if self.codes_of_frieze[row][col].direction[4] != 0:
                                if self.codes_of_frieze[row][(i + e * period) * 2 - col - 1].direction[4] == 0:
                                    can_pass_test = False
                                    break
                            if self.codes_of_frieze[row][col].direction[2] != 0:
                                if self.codes_of_frieze[row - 1][(i + e * period) * 2 - col - 1].direction[8] == 0:
                                    can_pass_test = False
                                    break
                            if self.codes_of_frieze[row][col].direction[8] != 0:
                                if self.codes_of_frieze[row + 1][(i + e * period) * 2 - col - 1].direction[2] == 0:
                                    can_pass_test = False
                                    break
                        if not can_pass_test:
                            break
                    if not can_pass_test:
                        break
            if can_pass_test:
                is_valid_axis = True
                break
        if is_valid_axis:
            return True
        else:
            return False








        
    def is_horizental_reflection(self):
        if len(self.codes_of_frieze) % 2 == 0:
            tail_num = len(self.codes_of_frieze) - 1
            half_part = tail_num + 1 // 2
            for row in range(half_part):
                for col in range(len(self.codes_of_frieze[row])):
                    if self.codes_of_frieze[row][col].direction[1] != 0:
                        if self.codes_of_frieze[tail_num - row + 1][col].direction[1] == 0:
                            return False
                    if self.codes_of_frieze[row][col].direction[2] != 0:
                        if self.codes_of_frieze[tail_num - row][col].direction[8] == 0:
                            return False
                    if self.codes_of_frieze[row][col].direction[4] != 0:
                        if self.codes_of_frieze[tail_num - row][col].direction[4] == 0:
                            return False
                    if self.codes_of_frieze[row][col].direction[8] != 0:
                        if self.codes_of_frieze[tail_num - row][col].direction[2] == 0:
                            return False
            print('Your are horizental reflection!')
            return True
        else:
            sys_axis = (len(self.codes_of_frieze) - 1) // 2
            tail_num = len(self.codes_of_frieze) - 1
            for col in range(len(self.codes_of_frieze[sys_axis])):
                if self.codes_of_frieze[sys_axis][col].direction[2] == 0 and self.codes_of_frieze[sys_axis][col].direction[8] != 0:
                    return False
                elif self.codes_of_frieze[sys_axis][col].direction[2] != 0 and self.codes_of_frieze[sys_axis][col].direction[8] == 0:
                    return False
                else:
                    pass
                if self.codes_of_frieze[sys_axis][col].direction[1] != 0:
                    if self.codes_of_frieze[sys_axis + 1][col].direction[1] == 0:
                        return False
            for row in range(sys_axis):
                for col in range(len(self.codes_of_frieze[row])):
                    if self.codes_of_frieze[row][col].direction[2] != 0:
                        if self.codes_of_frieze[tail_num - row][col].direction[8] == 0:
                           return False
                    if self.codes_of_frieze[row][col].direction[8] != 0:
                        if self.codes_of_frieze[tail_num - row][col].direction[2] == 0:
                            return False
                    if self.codes_of_frieze[row][col].direction[4] != 0:
                        if self.codes_of_frieze[tail_num - row][col].direction[4] == 0:
                            return False
                    if self.codes_of_frieze[row][col].direction[1] != 0:
                        if self.codes_of_frieze[tail_num - row + 1][col].direction[1] == 0:
                            return False
            print('Your are horizental reflection!')
            return True


    def is_glided_horizental_reflection(self):
        period = self.find_period()
        if len(self.codes_of_frieze) % 2 == 0:
            ##print('Case 1.')
            tail_num = len(self.codes_of_frieze) - 1
            half_part = tail_num + 1 // 2
            for row in range(half_part):
                for col in range(len(self.codes_of_frieze[row]) - 1):
                    if col + period >= len(self.codes_of_frieze[0]):
                        break
                    if self.codes_of_frieze[row][col].direction[1] != 0:
                        if self.codes_of_frieze[tail_num - row + 1][col + period//2].direction[1] == 0:
                            ##print(f'Error at row{row}col{col} and direction [1]')
                            return False
                    if self.codes_of_frieze[row][col].direction[2] != 0:
                        if self.codes_of_frieze[tail_num - row][col + period//2].direction[8] == 0:
                            ##print(f'Error at row{row}col{col} and direction [2]')
                            return False
                    if self.codes_of_frieze[row][col].direction[4] != 0:
                        if self.codes_of_frieze[tail_num - row][col + period//2].direction[4] == 0:
                            ##print(f'Error at row{row}col{col} and direction [4]')
                            return False
                    if self.codes_of_frieze[row][col].direction[8] != 0:
                        if self.codes_of_frieze[tail_num - row][col + period//2].direction[2] == 0:
                            ##print(f'Error at row{row}col{col} and direction [8]')
                            return False
            print('Your are glided horizental reflection!')
            return True
        else:
            ##print('Case 2')
            sys_axis = (len(self.codes_of_frieze) - 1) // 2
            tail_num = len(self.codes_of_frieze) - 1
            for col in range(len(self.codes_of_frieze[sys_axis]) - 1):
                if col + period >= len(self.codes_of_frieze[0]):
                    break
                if self.codes_of_frieze[sys_axis][col].direction[2] == 0 and self.codes_of_frieze[sys_axis][col + period//2].direction[8] != 0:
                    ##print(f'Error at col{col} and direction [1]')
                    return False
                elif self.codes_of_frieze[sys_axis][col].direction[2] != 0 and self.codes_of_frieze[sys_axis][col + period//2].direction[8] == 0:
                    ##print(f'Error at col{col} and direction [1]')
                    return False
                else:
                    pass
                if self.codes_of_frieze[sys_axis][col].direction[1] != 0:
                    if self.codes_of_frieze[sys_axis + 1][col + period//2].direction[1] == 0:
                        ##print(f'Error at col{col} and direction [1]')
                        return False
            for row in range(sys_axis):
                for col in range(len(self.codes_of_frieze[0]) - 1):
                    if col + period >= len(self.codes_of_frieze[row]) - 1:
                        break
                    if self.codes_of_frieze[row][col].direction[2] != 0:
                        if self.codes_of_frieze[tail_num - row][col + period//2].direction[8] == 0:
                            ##print(f'Error at row{row}col{col} and direction [2]')
                            return False
                    if self.codes_of_frieze[row][col].direction[8] != 0:
                        if self.codes_of_frieze[tail_num - row][col + period//2].direction[2] == 0:
                            ##print(f'Error at row{row}col{col} and direction [8]')
                            return False
                    if self.codes_of_frieze[row][col].direction[4] != 0:
                        if self.codes_of_frieze[tail_num - row][col + period//2].direction[4] == 0:
                            ##print(f'Error at row{row}col{col} and direction [4]')
                            return False
                    if self.codes_of_frieze[row][col].direction[1] != 0:
                        if self.codes_of_frieze[tail_num - row + 1][col + period//2].direction[1] == 0:
                            ##print(f'Error at row{row}col{col} and direction [1]')
                            return False
            print('Your are horizental reflection!')
            return True
    
    def is_rotation(self):
        pass
    

    def analyse(self):
        pass

        

