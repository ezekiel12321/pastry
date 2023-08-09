from genericpath import isfile
import sys
import os.path
import numpy as np

def getBroadcastTime(numNode, numFile = 10):
    for k in range(numFile):
        for i in range(numNode):
            if os.path.isfile('fr{}/Node_{}.log'.format(k,i)):
                with open('fr{}/Node_{}.log'.format(k,i)) as f:
                    lines = f.readlines()
                for j in range(len(lines)):
                    if lines[j].__contains__('Root sent broadcast message at'):
                        _len = len(lines[j])
                        broadcastTime = lines[j][_len-14:_len]
                        _rec = getRecBroadcastTime(numNode, numFile = numFile)
                        # _result = [x-broadcastTime for x in _rec]
                        return _rec-int(broadcastTime)


def getRecBroadcastTime(numNode, numFile = 10):
    nodeRecBroadcastTime = []
    for k in range(numFile):
        for i in range(numNode):
            if os.path.isfile('fr{}/Node_{}.log'.format(k,i)):
                with open('fr{}/Node_{}.log'.format(k,i)) as f:
                    lines = f.readlines()
                for j in range(len(lines)):
                    if lines[j].__contains__('received BroadcastContent'):
                        _len = len(lines[j])
                        recbroadcastTime = lines[j][_len-14:_len]
                        nodeRecBroadcastTime.append(int(recbroadcastTime))
    return max(nodeRecBroadcastTime)


def getAggregationTime(numNode, numFile = 10):
    _result = []
    for k in range(numFile):
        for i in range(numNode):
            if os.path.isfile('fr{}/Node_{}.log'.format(k,i)):
                with open('fr{}/Node_{}.log'.format(k,i)) as f:
                    lines = f.readlines()
                for j in range(len(lines)):
                    if lines[j].__contains__('sends UpdateContent'):
                        _len = len(lines[j])
                        aggTime = lines[j][_len-14:_len]
                        _result.append(int(aggTime))
                        break
    return getLastRootRecTime(numNode, numFile=numFile)-min(_result)

def getLastRootRecTime(numNode, numFile=10):
    # root_idx = ''
    for k in range(numFile):
        for i in range(numNode):
            if os.path.isfile('fr{}/Node_{}.log'.format(k,i)):
                with open('fr{}/Node_{}.log'.format(k,i)) as f:
                    lines = f.readlines()
                for j in range(len(lines)-1,0,-1):
                    # print(k,i)
                    if lines[j].__contains__('Root received'):
                        _len = len(lines[j])
                        lastRecTime = lines[j][_len-14:_len]
                        _tem = 14
                        rec_id = ''
                        while(lines[j][_tem] != ' '):
                            rec_id += lines[j][_tem]
                            _tem += 1
                        print("fr = {}, received {} messages".format(k, int(rec_id)))
                        return int(lastRecTime)







print('Broadcast to {} nodes takes {:.2f} sec.'.format(sys.argv[1],getBroadcastTime(int(sys.argv[1]), int(sys.argv[2]))/1000))
print('Aggregation for {} nodes takes {:.2f} sec.'.format(sys.argv[1],getAggregationTime(int(sys.argv[1]), int(sys.argv[2]))/1000))
                