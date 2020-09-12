#!/usr/bin/python3
import sys, socket, pickle, random, os, time, copy
from collections import deque
from Segment import *
import threading
from PLD import *
from collections import deque

pDrop = float(sys.argv[7])    #read in system arguements
pDuplicate = float(sys.argv[8])
pCorrupt = float(sys.argv[9])
pOrder = float(sys.argv[10])
MaxOrder = int(sys.argv[11])
pDelay = float(sys.argv[12])
MaxDelay = int(sys.argv[13])


original_time = time.time()

class Timeout(Exception):
    pass




# def write_log(func):
#     def print_log(*args, **kwargs):
#         if args[1].signal == 'D' or args[1].signal == 'F' or args[1].signal == 'S': 
#             event = 'snd'
#         elif args[1].signal == 'A':
#             event = 'rcv'
#         type_of_packet = args[1].signal
#         seq_num = args[1].seq_num
#         ack_num = args[1].ack_num
#         time_of_event = time.time() - original_time
#         if args[1].payload:
#             number_of_bytes = len(args[1].payload)
#         else:
#             number_of_bytes = 0
#         log = args[3]
#         line = f"{event:4}\t{time_of_event:4,.2f}\t{type_of_packet:4}\t{seq_num:4}\t{number_of_bytes:4}\t{ack_num:4}\n"
#         log.write(line)
#         print(line, end = '')
#         return func(*args, **kwargs)
#     return print_log


class STPSender:
    def __init__(self, receiver_ip, receiver_port, file_name, MSS, MWS, gamma):
        self.receiver_ip = receiver_ip
        self.receiver_port = receiver_port
        self.file_name = file_name
        self.MSS = MSS
        self.MWS = MWS
        self.EstimatedRTT = 500 / 10**3
        self.gamma = gamma
        self.DevRTT = 250 / 10**3
        self.timestamp = time.time()
        self.UdpSendSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)   #create a udp socket to send file
        #log arguments
        self.sizeOfFile = 0
        self.SegmentTransmitted = 0
        self.NbOfSgmHanleByPLD = 0  #ok
        self.NbOfSgmDrop = 0    #ok
        self.NbOfSgmDelay = 0   #ok
        self.NbOfSgmDuplicate = 0   #ok
        self.NbOfSgmCorrupt = 0     #ok
        self.NbOfSgmOutOfOrder = 0
        self.fastRetransmitSegment = 0
        self.RXTDuetoTimeout = 0
        self.DuplicatedAck = 0
        #self.UdpSendSocket.settimeout(timeInterval)
    
    #rewrite the function for easy to decorate
    def timmer(self):
        while True:
            while self.isSending:
                if time.time() - self.sendTime > self.timeInterval:
                    self.timeout = True
                    print("Time out")
                if self.isSendingOver:
                    break
            if self.isSendingOver:
                break

    #@write_log
    def SendSegment(self, sgm, receiver_host, log):
        self.UdpSendSocket.sendto(pickle.dumps(sgm),receiver_host)
    
    def RecvSegment(self):
        reply, address = self.UdpSendSocket.recvfrom(1000)
        return reply, address
    
    def send(self):
        self.segmentDict = dict()
        self.NextSeqNum = 1  #count bytes, last byte sent
        while self.sendBase < len(self.fileChunks):    #thread terminate untill send all bytes in filechunks
            #time.sleep(0.1)
            if self.fastRetransmit:
                try:
                    sgm = self.segmentDict[self.sendBase]
                    self.fastRetransmit = False
                    self.SendSegment(sgm, self.receiver_host, self.log)   #send data
                    self.fastRetransmitSegment += 1
                    self.SegmentTransmitted += 1
                    self.print_snd_log('snd/RXT', time.time() - self.original, 'D', sgm.seq_num, len(sgm.payload), 1)
                except KeyError:
                    pass
            if self.timeout:
                self.isResend = True
                for i in sorted(self.segmentDict):
                    try:
                        self.SendSegment(self.segmentDict[i], self.receiver_host, self.log)
                        self.print_snd_log('snd/RXT', time.time() - self.original, 'D', i, len(sgm.payload), 1)
                        self.SegmentTransmitted += 1
                        self.RXTDuetoTimeout += 1
                        self.timeout = False
                    except KeyError:
                        self.timeout = False
            while self.NextSeqNum - self.sendBase > self.MWS: #stop sending
                print("MWS limit\r")
                self.isResend = True
                for i in sorted(self.segmentDict):
                    try:
                        self.print_snd_log('snd/RXT', time.time() - self.original, 'D', i, len(sgm.payload), 1)
                        self.SendSegment(self.segmentDict[i], self.receiver_host, self.log)
                        self.SegmentTransmitted += 1
                    except KeyError:
                        pass
            if self.NextSeqNum + self.MSS < len(self.fileChunks):
                dataBuffer = self.fileChunks[self.NextSeqNum:self.NextSeqNum + self.MSS] #data whose length is MSS into buffer
                seqNum = self.NextSeqNum
                self.NextSeqNum = self.NextSeqNum + self.MSS
            else:
                if self.NextSeqNum == len(self.fileChunks) - 1:
                    continue
                dataBuffer = self.fileChunks[self.NextSeqNum:len(self.fileChunks)]
                seqNum = self.NextSeqNum
                self.NextSeqNum = len(self.fileChunks)
            # if seqNum < self.sendBase:
            #     continue
            if not dataBuffer:
                continue
            sgm = segment(35678, self.receiver_port, seqNum, 0, 'D')    #create a header
            sgm.upload_payload(dataBuffer)    #segment's payload is 1 byte data
            self.segmentDict[seqNum] = sgm
            #wait and send
            if drop_packets(pDrop):
                self.NbOfSgmDrop += 1
                self.NbOfSgmHanleByPLD += 1
                print(f"drop packet {sgm.seq_num}")
                self.print_snd_log('snd/drop', time.time() - self.original, 'D', sgm.seq_num, len(sgm.payload), 1)
                self.SegmentTransmitted += 1
            elif duplicate_packets(pDuplicate):
                self.NbOfSgmDuplicate += 1
                self.NbOfSgmHanleByPLD += 1
                print(f"duplicate packet {sgm.seq_num}")
                self.SendSegment(sgm, self.receiver_host, self.log)
                self.print_snd_log('snd', time.time() - self.original, 'D', sgm.seq_num, len(sgm.payload), 1)
                self.SegmentTransmitted += 1
                self.SendSegment(sgm, self.receiver_host, self.log)
                self.print_snd_log('snd/dup', time.time() - self.original, 'D', sgm.seq_num, len(sgm.payload), 1)
                self.SegmentTransmitted += 1
            elif create_bit_error_within_packets(pCorrupt):
                self.NbOfSgmCorrupt += 1
                self.NbOfSgmHanleByPLD += 1
                b = 0b1
                if len(sgm.payload) > 1:
                    theByteOrder = random.randint(0, len(sgm.payload) - 1)
                    theByte = sgm.payload[theByteOrder]
                    theByte = struct.unpack('B', theByte)[0]    #integer
                    theCorruptByte = b ^ theByte
                    copyOfSegment = copy.deepcopy(sgm)
                    copyOfSegment.payload[theByteOrder] = bytes(theCorruptByte)
                    print(f"create bit error with in packets {sgm.seq_num}")
                    self.SendSegment(copyOfSegment, self.receiver_host, self.log)
                    self.print_snd_log('snd/corr', time.time() - self.original, 'D', sgm.seq_num, len(sgm.payload), 1)
                    self.SegmentTransmitted += 1
                else:
                    self.SendSegment(sgm, self.receiver_host, self.log)
                    self.SegmentTransmitted += 1
            elif transmits_out_of_order_packet(pOrder):
                self.NbOfSgmOutOfOrder += 1
                self.NbOfSgmHanleByPLD += 1
                if not self.wait_queue:
                    print(f"out of order packet {sgm.seq_num}")
                    self.wait_queue.append(sgm)
                    self.waitOrder = MaxOrder
                    TOP_th = threading.Thread(target=self.send_outoforder_packet, args=(sgm,))
                    TOP_th.start()
                else:
                    pass
            elif delays_packet(pDelay, MaxDelay):
                self.NbOfSgmDelay += 1
                self.NbOfSgmHanleByPLD += 1
                delay_packet_th = threading.Thread(target=self.send_delay_packet, args=(sgm,))
                print(f"delay packet {sgm.seq_num}")
                delay_packet_th.start()
            else:
                if self.waitOrder > 0:
                    self.waitOrder -= 1
                self.SendSegment(sgm, self.receiver_host, self.log)   #send data
                self.print_snd_log('snd', time.time() - self.original, 'D', sgm.seq_num, len(sgm.payload), 1)
                self.SegmentTransmitted += 1
                if not self.isResend:
                    self.sendTime = time.time()
        print("send thread over")
    
    def send_outoforder_packet(self, sgm):
        while True:
            if self.waitOrder == 0:
                self.SendSegment(sgm, self.receiver_host, self.log)   #send data
                self.print_snd_log('snd/rord', time.time() - self.original, 'D', sgm.seq_num, len(sgm.payload), 1)
                self.SegmentTransmitted += 1
                break
            elif self.isSendingOver:    #may raise a problem
                self.SendSegment(sgm, self.receiver_host, self.log)   #send data
                self.print_snd_log('snd/rord', time.time() - self.original, 'D', sgm.seq_num, len(sgm.payload), 1)
                self.SegmentTransmitted += 1
                break

    def send_delay_packet(self, sgm):
        delay_duration = random.randint(0, MaxDelay) / 1000
        time.sleep(delay_duration)
        self.SendSegment(sgm, self.receiver_host, self.log)
        self.print_snd_log('snd/dely', time.time() - self.original, 'D', sgm.seq_num, len(sgm.payload), 1)
        self.SegmentTransmitted += 1
            
    def SendFile(self):
        self.original = time.time()
        #initialize config
        self.UdpSendSocket.bind(('', 35678))
        self.log = open("sender_log.txt", 'w+')
        self.receiver_host = (self.receiver_ip, receiver_port)
        self.BuildUpConnection(self.receiver_host, self.log)
        #handshake
        fileHandler = open(self.file_name, 'rb')    #open file in byte style
        #store file into buffer
        self.fileChunks = [b'']
        send_th = threading.Thread(target=self.send)
        byte = fileHandler.read(1)
        self.sizeOfFile += 1
        self.fileChunks.append(byte)
        f = open("compare_s", 'w')
        while byte:
            byte = fileHandler.read(1)
            self.sizeOfFile += 1
            print(byte,file = f)
            self.fileChunks.append(byte)
        #handle files into bytes
        #send bytes until the last one
        self.isSending = True #shared in timmer
        self.timeInterval = self.EstimatedRTT + self.gamma * self.DevRTT #shared in timmer
        th_timmer = threading.Thread(target = self.timmer) #create a timmer listening to transfer
        self.isResend = False
        self.timeout = False
        self.sendBase = 1
        self.sendTime = time.time()
        self.isSendingOver = False
        self.needRetransmit = False
        self.fastRetransmit = False
        self.wait_queue = []
        self.waitOrder = 0
        send_th.start()
        th_timmer.start()
        print(f"Transfer Start, total data is {len(self.fileChunks)}")
        duplicate_ack = 0
        while self.sendBase < len(self.fileChunks): #dealing with timeout issue
            if not self.isResend:
                self.timeInterval = self.calculateTimeInterval(self.sendTime)
            reply = self.RecvSegment()[0] #wait and receive ack from receiver
            replyData = pickle.loads(reply) #read reply
            for i in sorted(self.segmentDict):
                if i < replyData.ack_num:
                    self.segmentDict.pop(i) #get ack, release buffer???
            self.print_rcv_log('rcv', time.time() - self.original, 'A', 1, 0, replyData.ack_num)  #write log
            if self.sendBase >= replyData.ack_num:
                duplicate_ack += 1
                self.DuplicatedAck += 1
                if duplicate_ack == 3:
                    duplicate_ack = 0
                    self.fastRetransmit = True
            else:
                self.isResend = False
                self.sendBase = replyData.ack_num
            #print(self.sendBase)
        send_th.join()
        self.isSendingOver = True
        #terminate connection
        print("FIN_WAIT_1")
        sgm = segment(35678, self.receiver_port, self.NextSeqNum, 0, 'F')
        self.SendSegment(sgm, self.receiver_host, self.log)
        self.print_snd_log('snd', time.time() - self.original, 'F', sgm.seq_num, 0, 1)
        while True:
            try:
                reply = self.RecvSegment()[0]
                self.print_rcv_log('rcv', time.time() - self.original, 'A', 1, 0, 1)
                break
            except Timeout:
                self.SendSegment(sgm, self.receiver_host, self.log)
        replyData = pickle.loads(reply)
        #FIN_WAIT_2
        print("FIN_WAIT_2")
        while True:
            try:
                reply = self.RecvSegment()[0]
                replyData = pickle.loads(reply)
                self.print_rcv_log('rcv', time.time() - self.original, 'F', 1, 0, replyData.ack_num)
                if replyData.signal == 'F':
                    break
            except Timeout:
                pass
        sgm = segment(35678, self.receiver_port, self.NextSeqNum, 0, 'A')
        self.SendSegment(sgm, self.receiver_host, self.log)
        self.print_snd_log('snd', time.time() - self.original, 'A', sgm.seq_num, 0, 1)
        time.sleep(4.0)
        self.UdpSendSocket.close()
        print(file = self.log)
        print("=============================================================", file = self.log)
        print(f"Size of the file (in Bytes) {self.sizeOfFile}", file = self.log)
        print(f"Segments transmitted (including drop & RXT) {self.SegmentTransmitted}", file = self.log)
        print(f"Number of Segments handled by PLD {self.NbOfSgmHanleByPLD}", file = self.log)
        print(f"Number of Segments dropped {self.NbOfSgmDrop}", file = self.log)
        print(f"Number of Segments Corrupted {self.NbOfSgmCorrupt}", file = self.log)
        print(f"Number of Segments Re-ordered {self.NbOfSgmOutOfOrder}", file = self.log)
        print(f"Number of Segments Duplicated {self.NbOfSgmDuplicate}", file = self.log)
        print(f"Number of Segments Delayed {self.NbOfSgmDelay}", file = self.log)
        print(f"Number of Retransmissions due to TIMEOUT {self.RXTDuetoTimeout}", file = self.log)
        print(f"Number of FAST RETRANSMISSION {self.fastRetransmitSegment}", file = self.log)
        print(f"Number of DUP ACKS received {self.DuplicatedAck}", file = self.log)
        print("=============================================================", file = self.log)

    def BuildUpConnection(self, receiver_host, log):
        seqNum = 0
        sgm = segment(35678, self.receiver_port, seqNum, 0, 'S')
        self.SendSegment(sgm, receiver_host, log)
        self.print_snd_log('snd', time.time() - self.original, 'S', sgm.seq_num, 0, 0)
        sendTime = time.time()
        isResend = False
        while True:
            try:
                self.RecvSegment()
                self.print_rcv_log('rcv', time.time() - self.original, 'SA', 1, 0, 1)  #write log
                if not isResend:
                    self.timeInterval = self.calculateTimeInterval(sendTime)
                break
            except Timeout:
                print("time out")
                isResend = True
                self.SendSegment(sgm, receiver_host, log)
                self.print_snd_log('snd', time.time() - self.original, 'S', sgm.seq_num, 0, 0)
        seqNum += 1
        sgm = segment(35678, self.receiver_port, seqNum, 1, 'A')
        self.SendSegment(sgm, receiver_host, log)
        self.print_snd_log('snd', time.time() - self.original, 'A', sgm.seq_num, 0, 1)
        return True


    def calculateTimeInterval(self, sendTime):
        beta = 0.25
        alpha = 0.125
        sampleRTT = time.time() - sendTime
        self.EstimatedRTT = (1 - alpha) * self.EstimatedRTT + alpha * sampleRTT
        self.DevRTT = (1 - beta) * self.DevRTT + beta * abs(sampleRTT  - self.EstimatedRTT)
        timeInterval = self.EstimatedRTT + 4 * self.DevRTT
        return timeInterval
    
    def print_snd_log(self,event, timeOfevent, typeOfPacket, seqNum, NbOfData, ackNum):
        print(f"{event:4}\t{timeOfevent:4,.2f}\t{typeOfPacket:4}\t{seqNum:4}\t{NbOfData:4}\t{ackNum:4}", file = self.log)
        
    def print_rcv_log(self, event, time_for_happen, type_of_packet, seq_num, number_of_bytes, ack_num):
        '''
        event:
            A(ACK)
            DA(Duplicate ACK)
        '''
        line = f"{event:4}\t{time_for_happen:4,.2f}\t{type_of_packet:4}\t{seq_num:4}\t{number_of_bytes:4}\t{ack_num:4}\n"
        self.log.write(line)

if __name__ == "__main__":
    receiver_ip = sys.argv[1]
    receiver_port = int(sys.argv[2])
    file_name = sys.argv[3]
    MSS = int(sys.argv[5])
    MWS = int(sys.argv[4])
    gamma = int(sys.argv[6])
    sender = STPSender(receiver_ip, receiver_port, file_name, MSS, MWS, gamma)
    sender.SendFile()
            
            

        
    