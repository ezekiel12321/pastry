from genericpath import isfile
import sys
import os.path
# import numpy as np

def getBroadcastTime(numNode):
    # for k in range(numFile):
    for i in range(numNode):
        if os.path.isfile('Node_{}.log'.format(i)):
            with open('Node_{}.log'.format(i)) as f:
                lines = f.readlines()
            for j in range(len(lines)):
                if lines[j].__contains__('Root sent broadcast message at'):
                    _len = len(lines[j])
                    broadcastTime = lines[j][_len-14:_len]
                    # root first broadcasts models
                    return int(broadcastTime)
    # if this vm does not have root
    return 9999999999999


def getRecBroadcastTime(numNode):
    nodeRecBroadcastTime = []
    # for k in range(numFile):
    for i in range(numNode):
        if os.path.isfile('Node_{}.log'.format(i)):
            with open('Node_{}.log'.format(i)) as f:
                lines = f.readlines()
            for j in range(len(lines)):
                if lines[j].__contains__('received BroadcastContent'):
                    _len = len(lines[j])
                    recbroadcastTime = lines[j][_len-14:_len]
                    nodeRecBroadcastTime.append(int(recbroadcastTime))
    # return this vm's last receive broadcast time
    return max(nodeRecBroadcastTime)


def getAggregationTime(numNode):
    _result = []
    # for k in range(numFile):
    for i in range(numNode):
        if os.path.isfile('Node_{}.log'.format(i)):
            with open('Node_{}.log'.format(i)) as f:
                lines = f.readlines()
            for j in range(len(lines)):
                if lines[j].__contains__('sends UpdateContent'):
                    _len = len(lines[j])
                    aggTime = lines[j][_len-14:_len]
                    _result.append(int(aggTime))
                    break
    # return this vm's first send update time
    return min(_result)

def getLastRootRecTime(numNode):
    # root_idx = ''
    # for k in range(numFile):
    for i in range(numNode):
        if os.path.isfile('Node_{}.log'.format(i)):
            with open('Node_{}.log'.format(i)) as f:
                lines = f.readlines()
            for j in range(len(lines)-1,0,-1):
                # print(k,i)
                if lines[j].__contains__('Root received'):
                    _len = len(lines[j])
                    lastRecTime = lines[j][_len-14:_len]
                    # _tem = 14
                    # rec_id = ''
                    # while(lines[j][_tem] != ' '):
                    #     rec_id += lines[j][_tem]
                    #     _tem += 1
                    # print("fr = {}, received {} messages".format(k, int(rec_id)))
                    # return root's last recevied agg time
                    return int(lastRecTime)
    # this vm does not have root
    # return 1667248216653
    return 9999999999999

numNode = int(sys.argv[1])
print('Broad: {}, revBroad: {}'.format(getBroadcastTime(numNode), getRecBroadcastTime(numNode)))
print('Aggre: {}, lstAggre: {}'.format(getAggregationTime(numNode), getLastRootRecTime(numNode)))


# print('Broadcast to {} nodes takes {:.2f} sec.'.format(sys.argv[1],getBroadcastTime(int(sys.argv[1]), int(sys.argv[2]))/1000))
# print('Aggregation for {} nodes takes {:.2f} sec.'.format(sys.argv[1],getAggregationTime(int(sys.argv[1]), int(sys.argv[2]))/1000))
                