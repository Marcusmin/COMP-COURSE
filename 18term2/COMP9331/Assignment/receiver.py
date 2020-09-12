import sys, socket, pickle, time
from stp_segment import *
'''
receiver_port = int(sys.argv[1])
file_r = int(sys.argv[2])
'''

'''

'''

class UDP_receiver:
    def __init__(self, receiver_port = None, file_r = None, MSS = 0):
        self.receiver_port = receiver_port
        self.rcv_box = file_r
        self.udprcv_welcomesocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.addr = ('', self.receiver_port)

    def build_up_connection(self):
        '''
        three way hand shake
        accept the first segment from sender
        and send ack
        if shakehand successfully, create a new socket
        close welcome socket
        '''
        self.udprcv_welcomesocket.bind(self.addr)
        print(f"receiver is listening to {self.addr}")
        reply = self.receive_segment(self.udprcv_welcomesocket)
        print(f"Receive {reply.signal} from {self.sender_addr}")
        self.addr = (self.sender_addr[0], self.receiver_port)
        if reply.signal == 'syn':
            self.udprcv_welcomesocket.close()
            print(f"welcome socket closed")
            self.udprcv_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            print(f"transfer socket created")
            self.udprcv_socket.bind(self.addr)
            print(f"transfer socket bind to {self.addr}")
            self.send_ack(reply.seq_num + 1, self.udprcv_socket)
            self.receive_segment(self.udprcv_socket)
            print(f"handshake's last message from {self.sender_addr}")
            return True
        else:
            return False
        
    def terminate_connection(self, ack_num):
        '''
        four segment connection terminate
        '''
        self.send_ack(ack_num, self.udprcv_socket) #sender has initiated an termination, server send ack
        time.sleep(4)
        FIN = STP_segment(self.receiver_port, self.sender_addr[1],\
         0, 'FIN', ack_num)
        self.udprcv_socket.sendto(pickle.dumps(FIN), self.sender_addr)
        ACK_from_sender = self.udprcv_socket.recvfrom(1000)
        while not ACK_from_sender:
            ACK_from_sender = self.udprcv_socket.recvfrom(1000)
    
    def receive_segment(self, which_socket):
        '''
        input: none
        output: file from sender:.pdf
        '''
        reply, self.sender_addr= which_socket.recvfrom(10000)
        reply = pickle.loads(reply)
        return reply
    def send_ack(self, ack_num, which_socket):
        '''
        '''
        self.ack = STP_segment(self.receiver_port, self.sender_addr[1],\
         0, 'ack', ack_num)
        which_socket.sendto(pickle.dumps(self.ack), self.sender_addr)
        print(f"{ack_num} has send to {self.sender_addr}")

    def receive_file(self):
        while not self.build_up_connection():
            print("try handshake")
        print("Handshake success")
        #self.udprcv_socket.settimeout(10.0)
        reply = self.receive_segment(self.udprcv_socket)
        ack_num = reply.seq_num + 1
        file = open("backup.pdf", 'wb')
        print("Write into backup.pdf")
        while reply:
            self.send_ack(ack_num, self.udprcv_socket)
            file.write(reply.payload)
            reply = self.receive_segment(self.udprcv_socket)
            if reply.signal == 'FIN':
                break
            print(reply.payload)
            ack_num = reply.seq_num + 1
        if reply.signal == 'FIN':
            self.terminate_connection(ack_num)
            file.close()
        else:
            print("Some issue terminate the connection")

if __name__ == '__main__':
    recver = UDP_receiver(int(sys.argv[1]))
    recver.receive_file()
        
        

