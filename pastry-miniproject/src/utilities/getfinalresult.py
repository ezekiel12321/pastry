import os.path
# from unittest import result

kill_node = [1,2,4,8,16,32,64,50]
fanout = [8,16,32]
decay = 1.7


for i in fanout:
    for j in kill_node:
        if (os.path.isfile('result_{}fo_1000_{}.txt'.format(i,j))):
            with open('result_{}fo_1000_{}.txt'.format(i,j)) as f:
                lines = f.readlines()
            count = 0
            sec = 0
            _result = []
            for k in range(len(lines)):
                if lines[k].__contains__('It takes'):
                    # seconds start at idx 20
                    sec_idx = 0
                    _sec = ''
                    while lines[k][20+sec_idx] != ' ':
                        _sec += lines[k][20+sec_idx]
                        sec_idx += 1
                    sec += int(_sec)
                    _result.append(int(_sec))
                    count += 1
            print('Average time is {:.2f} ms (all result {}) when {} nodes were killed and fanout is {}'.format(sec/count, _result,j,i))




