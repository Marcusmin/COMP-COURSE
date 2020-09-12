import sys, socket, pickle, random, os, time
from Segment import *
import threading
from PLD import *

pDrop = 0.01

def write_log(func):
    def print_log(*args, **kwargs):
        if args[1].signal == 'D' or args[1].signal == 'F':
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
        timeInterval = self.EstimatedRTT + self.gamma * self.DevRTT
        self.UdpSendSocket.settimeout(timeInterval)
    
    #rewrite the function for easy to decorate
    @write_log
    @drop_packets(pDrop)
    def SendSegment(self, sgm, receiver_host, log):
        self.UdpSendSocket.sendto(pickle.dumps(sgm),receiver_host)
    
    def RecvSegment(self):
        reply, address = self.UdpSendSocket.recvfrom(1000)
        return reply, address
    
    def SendFile(self):
        #initialize config
        self.UdpSendSocket.bind(('', 35678))
        sendBase = 0
        NextSeqNum = 0
        fileHandler = open(self.file_name, 'rb')    #open file in byte style
        log = open("sender_log.txt", 'w+')
        #store file into buffer
        fileChunks = []
        byte = fileHandler.read(1)
        fileChunks.append(byte)
        while byte:
            byte = fileHandler.read(1)
            fileChunks.append(byte)
        #fileChunks = list(reversed(fileChunks))
        #handle files into bytes
        receiver_host = (self.receiver_ip, receiver_port)
        #send bytes until the last one
        print("Transfer Start")
        #transfer data until buffer is empty
        while NextSeqNum < len(fileChunks):
            data = fileChunks[NextSeqNum] #pop out data from buffer
            sgm = segment(35678, self.receiver_port, NextSeqNum, 0, 'D')    #create a segment
            sgm.upload_payload(data)    #segment's payload is 1 byte data
            #wait and send
            sendTime = time.time()
            self.SendSegment(sgm, receiver_host, log)   #send data
            while True: #dealing with timeout issue
                try:
                    reply, address = self.RecvSegment() #wait and receive ack from receiver
                    #timmer sim
                    # recvTime = time.time()
                    # sampleRTT = sendTime - recvTime
                    # self.EstimatedRTT = 0.875 * self.EstimatedRTT + 0.125 * sampleRTT
                    # self.DevRTT = (1 - 0.25) * self.DevRTT + 0.25 * abs(sampleRTT - self.EstimatedRTT)
                    # timeInterval = self.EstimatedRTT + self.gamma * self.DevRTT
                    # print(f"{timeInterval}")
                    # self.UdpSendSocket.settimeout(timeInterval)
                    #reset timer
                    replyData = pickle.loads(reply) #read reply
                    self.print_rcv_log(replyData, log)  #write log
                    break
                except socket.timeout:
                    print("Time out, resend")
                    self.SendSegment(sgm, receiver_host, log)
            # while replyData.ack_num < NextSeqNum: #deal with incorrct ack
            #     print("ack incorrect, resend")
            #     self.SendSegment(sgm, receiver_host, log)   #send data again
            #     while True:
            #         try:
            #             reply, address = self.RecvSegment()
            #             recvTime = time.time()
            #             sampleRTT = sendTime - recvTime
            #             self.EstimatedRTT = 0.875 * self.EstimatedRTT + 0.125 * sampleRTT
            #             self.DevRTT = (1 - 0.25) * self.DevRTT + 0.25 * abs(sampleRTT - self.EstimatedRTT)
            #             timeInterval = self.EstimatedRTT + self.gamma * self.DevRTT
            #             print(f"{timeInterval}")
            #             self.UdpSendSocket.settimeout(timeInterval)
            #             replyData = pickle.loads(reply)
            #             self.print_rcv_log(replyData, log)
            #         except socket.timeout:
            #             print("Time out, resend")
            #             self.SendSegment(sgm, receiver_host, log)
            NextSeqNum = replyData.ack_num + 1
        sgm = segment(35678, self.receiver_port, NextSeqNum, 0, 'F')
        self.SendSegment(sgm, receiver_host, log)
        
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
            
            

        
    