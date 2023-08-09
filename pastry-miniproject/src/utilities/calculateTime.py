import sys
import os.path

numNode = int(sys.argv[1])
numKillNode = int(sys.argv[2])
numRemNode = numNode - numKillNode
find_thing = False
for i in range(numNode):
    if (os.path.isfile('Node_%s.log' %i)):
        with open('Node_%s.log' %i) as f:
            _str = f.read()
        with open('Node_%s.log' %i) as f:
            lines = f.readlines()
        if 'Root received' in _str:
            earliest_kill_time = finished_recovery_time = ''
            for j in range(len(lines)):
                # if lines[j].__contains__('earliest'):
                #     _len = len(lines[j])
                #     earliest_kill_time = lines[j][_len-14:_len]
                if lines[j].__contains__('Root received {}'.format(numRemNode)):
                    _len = len(lines[j])
                    finished_recovery_time = lines[j][_len-14:_len]
                    # print(lines[j][_len-14:_len])
                    for k in range(j, len(lines)):
                        if lines[k].__contains__('earliest'):
                            _len = len(lines[k])
                            earliest_kill_time = lines[k][_len-14:_len]
                            if int(earliest_kill_time) < 2000000000000:
                                break
                    break
            if finished_recovery_time == '':
                print('The failure recovery failed since the root did not receive {} ids.'.format(numRemNode))
                find_thing = True
            else:
                _temp = int(finished_recovery_time) - int(earliest_kill_time)
                print('It takes {} nodes {} ms to finish failure recovery of {} node failures.'.format(numNode, _temp, numKillNode))
                find_thing = True
            break
if find_thing == False:
    print('No data.')
            

# print('Minimum Number of Hops: {}'.format(min(final_result_hop)))
# logger.log("It takes " + NUM_NODES + " nodes " + (finished_time-first_kill_time)
#            + " ms to finish failure recovery of " + NUM_KILL_NODES + " node failures
