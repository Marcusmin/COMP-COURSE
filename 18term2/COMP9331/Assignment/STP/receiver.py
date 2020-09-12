#!/usr/bin/python3

import sys, socket, pickle, time, struct
from Segment import *

class UDP_receiver:
    def __init__(self, receiver_port = None, file_r = None):
        self.receiver_port = receiver_port
        self.rcv_box = file_r
        self.udprcv_welcomesocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.addr = ('', self.receiver_port)
        self.udprcv_welcomesocket.bind(self.addr)
        self.AmountOfDataRecv = 0
        self.TotalSegmentRecv = 0
        self.DataSegmentRecv = 0
        self.DataSegmentWithError = 0
        self.DuplicateDataSegmentRecv = 0
        self.DuplicateAckSent = 0
    
    def recvFile(self):
        self.log = open("receiver_log.txt", 'w+')
        f = open("compare_r.txt", 'w')
        expectedSeq = 0
        #receive first segment from sender
        handshakeSgm, addr = self.udprcv_welcomesocket.recvfrom(1200)
        startTime = time.time()
        handshakeSgm = pickle.loads(handshakeSgm)
        self.write_log("rcv", time.time() - startTime, 'S', handshakeSgm.seq_num, 0, 0)
        #handshake
        while True:
            if handshakeSgm.signal == 'S':
                InitalSeqNum = handshakeSgm.seq_num
                expectedSeq = InitalSeqNum + 1
                self.udprcv_welcomesocket.close()
                self.udprcv_receiversocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                sender_ip = addr[0]
                sender_port = addr[1]
                addr = ('', receiver_port)
                self.udprcv_receiversocket.bind(addr)
                #send ack back to sender
                sgm = segment(self.receiver_port, sender_port, 0, expectedSeq - 1, 'A')
                sender_addr = (sender_ip, sender_port)
                self.udprcv_receiversocket.sendto(pickle.dumps(sgm), sender_addr)
                self.write_log("snd", time.time() - startTime, 'SA', 0, 0, 1)
                print(f"Build connection with {addr}")
                break
        file_containter = open(self.rcv_box, 'wb')
        #waiting segments from sender
        recvedSegment, addr = self.udprcv_receiversocket.recvfrom(1200)
        self.write_log("rcv", time.time() - startTime, 'A', 1, 0, 1)
        recvedData = pickle.loads(recvedSegment)
        # print(recvedData.signal)
        while recvedData.signal == 'A':
            print("Handshake over")
            break
        self.outOfOrder = dict() #buffer the out of order packet
        recvedSegment, addr = self.udprcv_receiversocket.recvfrom(1200)
        recvedData = pickle.loads(recvedSegment)
        self.write_log("rcv", time.time() - startTime, 'D', recvedData.seq_num, len(recvedData.payload), 1)
        seqNum = 0
        while recvedData.signal == 'D':
            #check data's checksum
            if not self.isChecksumCorrect(recvedData):
                corrupt_seqNum = recvedData.seq_num
                print(f"corrputed {corrupt_seqNum}")
                #self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
                recvedSegment, addr = self.udprcv_receiversocket.recvfrom(1500)
                self.TotalSegmentRecv += 1  #every recv increase 1
                self.DataSegmentWithError += 1   #corrput data segment count
                recvedData = pickle.loads(recvedSegment)
                self.write_log("rcv", time.time() - startTime, 'D', recvedData.seq_num, len(recvedData.payload), 1)
            else:
                seqNum = recvedData.seq_num
                if seqNum != expectedSeq:   #if expect received sequence's number is not correct
                    if seqNum > expectedSeq:
                        print(f"Store {seqNum} in buffer")
                        self.outOfOrder[seqNum] = recvedData
                    if seqNum < expectedSeq:
                        self.DuplicateDataSegmentRecv += 1  #duplicated data segment count
                        self.write_log("snd/DA", time.time() - startTime, 'A', 1, 0, sgm.ack_num)
                        self.DuplicateAckSent += 1  #count duplicated ack
                    self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)   #resend the ack
                    print(f"receive {seqNum}, expect {expectedSeq}")
                    recvedSegment, addr = self.udprcv_receiversocket.recvfrom(1500)
                    self.TotalSegmentRecv += 1  #every recv increase 1
                    recvedData = pickle.loads(recvedSegment)
                    if recvedData.signal == 'D':
                        self.write_log("rcv", time.time() - startTime, 'D', recvedData.seq_num, len(recvedData.payload), 1)
                    else:
                        self.write_log("rcv", time.time() - startTime, 'F', recvedData.seq_num, 0, 1)
                #if the sequence number is correct, write and update ack
                else:
                    self.DataSegmentRecv += 1    #data segment recv, only data segment write into file
                    data = recvedData.payload
                    for e in data:
                        expectedSeq += 1
                        file_containter.write(e)
                        self.AmountOfDataRecv += 1  #only the data write into file need to account
                        print(e, file=f)
                    while True:
                        try:
                            if self.outOfOrder[expectedSeq]:
                                Data = self.outOfOrder.pop(expectedSeq)
                                data = Data.payload
                                for e in data:
                                    expectedSeq += 1
                                    print(e, file=f)
                                    file_containter.write(e)
                                    self.AmountOfDataRecv += 1  #only the data write into file need to account
                                #release outoforder buffer
                                ack = expectedSeq
                                sgm = segment(self.receiver_port, addr[1], 0, ack, 'A')
                                print(f"has received {seqNum} before, ack {ack}")
                                #send ack
                                self.DataSegmentRecv += 1    #data segment recv
                                self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
                                self.write_log("snd", time.time() - startTime, 'A', 1, 0, sgm.ack_num)
                        except KeyError:
                            break
                    ack = expectedSeq
                    sgm = segment(self.receiver_port, addr[1], 0, ack, 'A')
                    print(f"receive {seqNum}, ack {ack}")
                    self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
                    self.write_log("snd", time.time() - startTime, 'A', 1, 0, sgm.ack_num)
                    recvedSegment, addr = self.udprcv_receiversocket.recvfrom(1500)
                    self.TotalSegmentRecv += 1  #every recv increase 1
                    recvedData = pickle.loads(recvedSegment)
                    if recvedData.signal == 'F':
                        self.TotalSegmentRecv -= 1  #every recv increase 1
                        self.write_log("rcv", time.time() - startTime, recvedData.signal, recvedData.seq_num, 0, 1)
                    else:
                        self.write_log("rcv", time.time() - startTime, recvedData.signal, recvedData.seq_num, len(recvedData.payload), 1)
        #terminate the connection
        print("FIN_WAIT_1")
        #ack += 1
        sgm = segment(self.receiver_port, addr[1], 0, ack, 'A')
        self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
        self.write_log("snd", time.time() - startTime, 'A', 1, 0, sgm.ack_num)
        time.sleep(3.0)
        print("FIN_WAIT_2")
        sgm = segment(self.receiver_port, addr[1], 0, ack, 'F')
        self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
        self.write_log("snd", time.time() - startTime, 'F', 1, 0, sgm.ack_num)
        while True:
            try:
                reply, addr = self.udprcv_receiversocket.recvfrom(1000)
                recvedData = pickle.loads(reply)
                self.write_log("rcv", time.time() - startTime, 'A', recvedData.seq_num, 0, 2)
                break
            except socket.timeout:
                self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
        print("Transfer Over")
        print("==============================================",file = self.log)
        print(f"Amount of data received (bytes) {self.AmountOfDataRecv}", file = self.log)
        print(f"Total Segments Received {self.TotalSegmentRecv}",file = self.log)
        print(f"Data segments received {self.DataSegmentRecv}",file = self.log)
        print(f"Data segments with Bit Errors {self.DataSegmentWithError}",file = self.log)
        print(f"Duplicate data segments received {self.DuplicateDataSegmentRecv}",file = self.log)
        print(f"Duplicate ACKs sent {self.DuplicateAckSent}", file = self.log)
        print("==============================================",file = self.log)
        file_containter.close()
    
    def isChecksumCorrect(self, recvedData):
        data = recvedData.payload   # A list of bytes
        checksum = 0
        base = 0b1111111111111111
        overflowbit = 0b100000000000000
        for e in data:
            try:
                checksum += struct.unpack('B', e)[0]
                if checksum > base:
                    checksum -= overflowbit
                    checksum += 1
            except struct.error:
                checksum += 0
        if recvedData.checksum == base ^ checksum:
            return True
        else:
            return False

    def write_log(self, event, timeOfevent, typeOfPacket, seqNum, NbOfData, ackNum):
        print(f"{event:4}\t{timeOfevent:4,.2f}\t{typeOfPacket:4}\t{seqNum:4}\t{NbOfData:4}\t{ackNum:4}", file = self.log)

if __name__ == "__main__":
    receiver_port = int(sys.argv[1])
    file_r = sys.argv[2]
    receiver = UDP_receiver(receiver_port, file_r)
    receiver.recvFile()

