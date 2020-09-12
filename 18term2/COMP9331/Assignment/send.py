import sys, socket, pickle, random
from stp_segment import *
import threading

'''
variables below is the argument from command line
'''

'''
receiver_host_ip = int(sys.argv[1])
receiver_port = int(sys.argv[2])
file = sys.argv[3]
MWS = int(sys.argv[4])
MSS = int(sys.argv[5])
gamma = int(sys.argv[6])
PLD.pDrop = int(sys.argv[7])
PLD.pDuplicate = int(sys.argv[8])
PLD.pCorrupt = int(sys.argv[9])
PLD.pOrder = int(sys.argv[10])
PLD.maxOrder = int(sys.argv[11])
PLD.pDelay = int(sys.argv[12])
PLD.pmaxDelay = int(sys.argv[13])
PLD.seed = int(sys.argv[14])
'''

#Build up a sender socket
class UDP_sender:
    '''
    A simple sender use UDP socket to send file to target receiver
    '''
    def __init__(self, receiver_host_ip, receiver_port,\
     file_name = None, MSS = None):
        '''
        input:
            receiver_host_ip:int
            receiver_port:int
            file:string
        output:
            build up an udp socket
            self.host: a vector contains receiver's ip and receiver's port
            self.udpsocket: a udp socket to send segment and receive receiver's ack
            self.mss: the maxinum data(byte) in segment
        '''
        self.receiver_host_ip = receiver_host_ip
        self.receiver_port = receiver_port
        self.file_name = file_name
        self.MSS = MSS
        self.host = (self.receiver_host_ip, self.receiver_port)
        self.udpsocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def build_up_connection(self):
        '''
        three-way handshake
        input:
        output:
        self.ISN: initial sequence number, int
        self.port: sender's port for sending segments, int
        ack_msg: receiver's ack message, object
        first_seg: specified for 3-way handshake, object
        second_seg; specified for 3-way handshake, object
        return: ack_msg: object
        '''
        self.handshake_ack = None
        self.udpsocket.settimeout(3.0)
        self.ISN = random.randrange(100, 400)
        self.port = self.udpsocket.getsockname()[1]
        first_seg = STP_segment(self.port, self.receiver_port, self.ISN, \
            "syn", 0)
        self.send_syn(first_seg)
        print(f"sender is sending to {self.host}")
        while True:
            try:
                self.handshake_ack = self.receive_syn()
                print(f"Get ack from {self.recv_addr}")
                break
            except socket.timeout:
                print("Time out, send first segment again")
                self.send_syn(first_seg)
        if self.handshake_ack:   #if receive ack successfully
            if self.recv_addr == self.host:   #check the if receiver's message is correct or not
                second_seg = STP_segment(self.port,self.receiver_port, self.ISN, \
            "ack", self.ISN + 1)   #send second segment to build up connection
                self.send_syn(second_seg)
                return True
            else:
                return False
        else:
            return False

    def terminate_connection(self):
        '''
        four segments terminate
        sender intiate termination.
        input:
        output:
        '''
        FIN = STP_segment(self.port, self.receiver_port, self.NextSeqNum, \
            "FIN", 0)
        print("Sending FIN to receiver")
        self.send_syn(FIN)  #frist FIN send
        print("Waiting ACK from receiver")
        ACK_FIN_1 = self.receive_ack()
        while not ACK_FIN_1:
            ACK_FIN_1 = self.receive_ack()  #looking forward the ack from receiver
        print("receiver acknowledged")
        FIN_from_recver = self.receive_ack()    #wait for FIN from receiver
        while not FIN_from_recver:
            FIN_from_recver = self.receive_ack()
        ACK_from_recv_2 = STP_segment(self.port, self.receiver_port, self.NextSeqNum, \
            "FIN", 0)
        self.send_syn(ACK_from_recv_2)

    def send_file(self):
        '''
        input:
        output:
        '''
        #3-way handshake
        while not self.build_up_connection():
            print("Trying to handshake with receiver.")
        print("handshake successsfully!");
        #separate file into segments
        file = open(self.file_name, 'rb')
        self.file_chunks = []
        payload = file.read(self.MSS)
        self.file_chunks.append(payload)
        while payload:
            payload = file.read(self.MSS)
            self.file_chunks.append(payload)
        #inital sequence number
        self.relative_seq = self.ISN + 2
        self.NextSeqNum = self.ISN + 2
        self.SendBase = self.ISN + 2
        self.Finalsegment = len(self.file_chunks)
        #build up a thread receive ack from receiver
        print("Ready to send files.")
        thread_list = []
        send_th = threading.Thread(target = self.send_segment())
        thread_list.append(send_th)
        recv_th = threading.Thread(target = self.receive_ack())
        thread_list.append(recv_th)
        send_th.start()
        recv_th.start()
        for i in thread_list:   #wait two threads finish
            if i.isAlive():
                i.join()
        print("Send mission successs")
        print("Tring to close file")
        self.terminate_connection()
        print("")
        file.close()
        



    def send_segment(self):
        '''
        input:
            segment:stp_segment object
        output:
            sending segment
            after sent message: print message
        '''
        print(f"Sending data to {self.receiver_host_ip}: {self.receiver_port}")
        while self.NextSeqNum - self.relative_seq < self.Finalsegment:
            segment = STP_segment(seq_num = self.NextSeqNum)
            segment.upload_payload(self.file_chunks[self.NextSeqNum - self.relative_seq])
            self.NextSeqNum += 1
            self.udpsocket.sendto(pickle.dumps(segment), self.host)
            print(f"Sending {self.NextSeqNum - self.relative_seq} to {self.host}")
        return

    def send_syn(self, syn):
        self.udpsocket.sendto(pickle.dumps(syn), self.host)
    
    def receive_syn(self):
        return_segment, self.recv_addr = self.udpsocket.recvfrom(10000)
        return return_segment

    def receive_ack(self):
        '''
        input: 
            none
        output:
            segment with ack:class stp_segment
        '''
        while self.SendBase - self.relative_seq < self.Finalsegment:
            try:
                return_segment, self.recv_addr = self.udpsocket.recvfrom(10000)
                print(f"Get ack from {self.recv_addr}")
                return_segment = pickle.loads(return_segment)
                if return_segment.ack_num > self.SendBase:
                    self.SendBase = return_segment.ack_num
            except socket.timeout:
                print("Time out")
        return




if __name__ == '__main__':
    receiver_host_ip = sys.argv[1]
    receiver_port = int(sys.argv[2])
    sender = UDP_sender(receiver_host_ip, receiver_port, "test0.pdf", 100)
    sender.send_file()
    
