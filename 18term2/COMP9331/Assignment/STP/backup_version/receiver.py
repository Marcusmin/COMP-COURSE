import sys, socket, pickle, time
from Segment import *

class UDP_receiver:
    def __init__(self, receiver_port = None, file_r = None):
        self.receiver_port = receiver_port
        self.rcv_box = file_r
        self.udprcv_welcomesocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.addr = ('', self.receiver_port)
        self.udprcv_welcomesocket.bind(self.addr)
    
    def recvFile(self):
        expectedSeq = 0
        #receive first segment from sender
        handshakeSgm, addr = self.udprcv_welcomesocket.recvfrom(1000)
        handshakeSgm = pickle.loads(handshakeSgm)
        #handshake
        while True:
            if handshakeSgm.signal == 'S':
                InitalSeqNum = handshakeSgm.seq_num
                expectedSeq = InitalSeqNum + 1
                self.udprcv_welcomesocket.close()
                self.udprcv_receiversocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                sender_ip = addr[0]
                sender_port = addr[1]
                addr = ('localhost', receiver_port)
                self.udprcv_receiversocket.bind(addr)
                #send ack back to sender
                sgm = segment(self.receiver_port, sender_port, 0, expectedSeq - 1, 'A')
                sender_addr = (sender_ip, sender_port)
                self.udprcv_receiversocket.sendto(pickle.dumps(sgm), sender_addr)
                print(f"Build connection with {addr}")
                break
        file_containter = open(self.rcv_box, 'wb')
        #waiting segments from sender
        recvedSegment, addr = self.udprcv_receiversocket.recvfrom(1000)
        recvedData = pickle.loads(recvedSegment)
        # print(recvedData.signal)
        while recvedData.signal == 'A':
            print("Handshake over")
            break
        recvedSegment, addr = self.udprcv_receiversocket.recvfrom(1000)
        recvedData = pickle.loads(recvedSegment)
        while recvedData.signal == 'D':
            seqNum = recvedData.seq_num
            while seqNum != expectedSeq:   #if expect received sequence's number is not correct
                self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)   #resend the ack
                print(f"receive {seqNum}, expect {expectedSeq}")
                recvedSegment, addr = self.udprcv_receiversocket.recvfrom(1000)
                recvedData = pickle.loads(recvedSegment)
                seqNum = recvedData.seq_num
            #if the sequence number is correct, write and update ack
            data = recvedData.payload   # A list of bytes
            for e in data:
                expectedSeq += 1
                file_containter.write(e)
            ack = expectedSeq - 1
            sgm = segment(self.receiver_port, addr[1], 0, ack, 'A')
            self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
            recvedSegment, addr = self.udprcv_receiversocket.recvfrom(1000)
            recvedData = pickle.loads(recvedSegment)
        #terminate the connection
        print("FIN_WAIT_1")
        ack += 1
        sgm = segment(self.receiver_port, addr[1], 0, ack, 'A')
        self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
        time.sleep(3.0)
        print("FIN_WAIT_2")
        sgm = segment(self.receiver_port, addr[1], 0, ack, 'F')
        self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
        while True:
            try:
                reply, addr = self.udprcv_receiversocket.recvfrom(1000)
                recvedData = pickle.loads(reply)
                break
            except socket.timeout:
                self.udprcv_receiversocket.sendto(pickle.dumps(sgm), addr)
        print("Transfer Over")
        file_containter.close()

if __name__ == "__main__":
    receiver_port = 8080
    file_r = "backup.pdf"
    receiver = UDP_receiver(8080, file_r)
    receiver.recvFile()

