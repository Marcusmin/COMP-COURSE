import sys, socket, pickle, random, os, time
from collections import deque
from Segment import *
import threading
from PLD import *

pDrop = 0.01    #read in system arguements
class Timeout(Exception):
    pass

def timmer(args):
    while True:
        while args["isSending"]:
            print(args["timeInterval"])
            if time.time() - args["sendTime"] > args["timeInterval"]:
                raise Timeout
            if args["isSendingOver"]:
                break
        if args["isSendingOver"]:
            break

def send_delay(MaxDelay):
	delay_duration = random.randrange(0, MaxDelay) / 1000	#millisecond to second
    while True:
        if time.time() >= MaxDelay:
            #send
            pass

def write_log(func):
    def print_log(*args, **kwargs):
        if args[1].signal == 'D' or args[1].signal == 'F' or args[1].signal == 'S': 
            event = 'snd'
        elif args[1].signal == 'A':
            event = 'rcv'
        type_of_packet = args[1].signal
        seq_num = args[1].seq_num
        ack_num = args[1].ack_num
        log = args[3]
        line = f"{event}\t{type_of_packet}\t{seq_num}\t{ack_num}\n"
        log.write(line)
        print(line, end = '')
        return func(*args, **kwargs)
    return print_log


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
        #self.UdpSendSocket.settimeout(timeInterval)
    
    #rewrite the function for easy to decorate
    @write_log
    def SendSegment(self, sgm, receiver_host, log):
        if drop_packets(pDrop):
            return
        elif duplicate_packets(pDuplicate):
            #if need duplicate packet, send same segment twice
            self.UdpSendSocket.sendto(pickle.dumps(sgm),receiver_host)
            self.UdpSendSocket.sendto(pickle.dumps(sgm),receiver_host)
        elif create_bit_error_within_packets(pCorrupt):
            bit = 0b1
            sgm.payload[0] = bit ^ sgm.payload[0]
            self.UdpSendSocket.sendto(pickle.dumps(sgm),receiver_host)
        elif transmits_out_of_order_packet(pOrder):
            reorderBuffer.append(sgm)
        elif delays_packet(pDelay):
            pass
        else:
            self.UdpSendSocket.sendto(pickle.dumps(sgm),receiver_host)
    
    def RecvSegment(self):
        reply, address = self.UdpSendSocket.recvfrom(1000)
        return reply, address
    
    def SendFile(self):
        #initialize config
        self.UdpSendSocket.bind(('', 35678))
        log = open("sender_log.txt", 'w+')
        receiver_host = (self.receiver_ip, receiver_port)
        self.BuildUpConnection(receiver_host, log)
        #handshake
        sendBase = 1
        NextSeqNum = 1
        fileHandler = open(self.file_name, 'rb')    #open file in byte style
        #store file into buffer
        fileChunks = [b'', b'']
        byte = fileHandler.read(1)
        fileChunks.append(byte)
        while byte:
            byte = fileHandler.read(1)
            fileChunks.append(byte)
        #handle files into bytes
        #send bytes until the last one
        print("Transfer Start")
        #transfer data until buffer is empty
        isSending = False #shared in timmer
        sendTime = time.time()  #shared in timmer
        timeInterval = self.EstimatedRTT + self.gamma * self.DevRTT #shared in timmer
        timmer_args = {"sendTime":sendTime, "isSending":isSending, "timeInterval":timeInterval, "isSendingOver":False}
        th_timmer = threading.Thread(target = timmer, args=(timmer_args,)) #create a timmer listening to transfer
        th_timmer.start()
        while NextSeqNum < len(fileChunks):
            if reorderBuffer and reorderBuffer[0].seq_num + maxOrder >= NextSeqNum:
                sgm = reorderBuffer.pop()
                self.SendSegment(sgm, receiver_host, log)
            else:
                if NextSeqNum + self.MSS < len(fileChunks):
                    dataBuffer = fileChunks[NextSeqNum:NextSeqNum + self.MSS] #data whose length is MSS into buffer
                    seqNum = NextSeqNum
                    NextSeqNum = NextSeqNum + self.MSS
                else:
                    dataBuffer = fileChunks[NextSeqNum:len(fileChunks)]
                    seqNum = NextSeqNum
                    NextSeqNum = len(fileChunks)
                sgm = segment(35678, self.receiver_port, seqNum, 0, 'D')    #create a header
                sgm.upload_payload(dataBuffer)    #segment's payload is 1 byte data
            #wait and send
            sendTime = time.time()  #timmer for send
            self.SendSegment(sgm, receiver_host, log)   #send data
            SegmentOrder += 1
            isSending = True    #a event signal for timmer thread
            timmer_args["sendTime"] = sendTime
            timmer_args["isSending"] = isSending
            isReSend = False
            while True: #dealing with timeout issue
                try:
                    reply, address = self.RecvSegment() #wait and receive ack from receiver
                    if not isReSend:    #if segment is not resend
                        #calculate sampleRTT and refresh timeinterval
                        timeInterval = self.calculateTimeInterval(sendTime)
                        timmer_args["timerInterval"] = timeInterval
                        #self.UdpSendSocket.settimeout(timeInterval)
                    replyData = pickle.loads(reply) #read reply
                    self.print_rcv_log(replyData, log)  #write log
                    break
                except Timeout:
                    print("Time out, resend")
                    isReSend = True
                    self.SendSegment(sgm, receiver_host, log)
        timmer_args["isSendingOver"] = True
        #terminate connection
        print("FIN_WAIT_1")
        sgm = segment(35678, self.receiver_port, NextSeqNum, 0, 'F')
        self.SendSegment(sgm, receiver_host, log)
        while True:
            try:
                reply, addr = self.RecvSegment()
                break
            except Timeout:
                self.SendSegment(sgm, receiver_host, log)
        replyData = pickle.loads(reply)
        #FIN_WAIT_2
        print("FIN_WAIT_2")
        while True:
            try:
                reply, addr = self.RecvSegment()
                replyData = pickle.loads(reply)
                if replyData.signal == 'F':
                    break
            except Timeout:
                pass
        sgm = segment(35678, self.receiver_port, NextSeqNum, 0, 'A')
        self.SendSegment(sgm, receiver_host, log)
        time.sleep(4.0)
        self.UdpSendSocket.close()

    def BuildUpConnection(self, receiver_host, log):
        seqNum = 0
        sgm = segment(35678, self.receiver_port, seqNum, 0, 'S')
        self.SendSegment(sgm, receiver_host, log)
        sendTime = time.time()
        isResend = False
        while True:
            try:
                self.RecvSegment()
                if not isResend:
                    timeInterval = self.calculateTimeInterval(sendTime)
                    #self.UdpSendSocket.settimeout(timeInterval)
                break
            except Timeout:
                print("timee out")
                isResend = True
                self.SendSegment(sgm, receiver_host, log)
        seqNum += 1
        sgm = segment(35678, self.receiver_port, seqNum, 1, 'A')
        self.SendSegment(sgm, receiver_host, log)
        return True


    def calculateTimeInterval(self, sendTime):
        beta = 0.25
        alpha = 0.125
        sampleRTT = time.time() - sendTime
        self.EstimatedRTT = (1 - alpha) * self.EstimatedRTT + alpha * sampleRTT
        self.DevRTT = (1 - beta) * self.DevRTT + beta * abs(sampleRTT  - self.EstimatedRTT)
        timeInterval = self.EstimatedRTT + 4 * self.DevRTT
        return timeInterval

        
    def print_rcv_log(self, reply, log):
        event = 'rcv'
        type_of_packet = reply.signal
        seq_num = reply.seq_num
        ack_num = reply.ack_num
        line = f"{event}\t{type_of_packet}\t{seq_num}\t{ack_num}\n"
        log.write(line)

if __name__ == "__main__":
    receiver_ip = '127.0.0.1'
    receiver_port = 8080
    file_name = "test0.pdf"
    MSS = 100
    MWS = 300
    gamma = 4
    sender = STPSender(receiver_ip, receiver_port, file_name, MSS, MWS, gamma)
    sender.SendFile()
            
            

        
    