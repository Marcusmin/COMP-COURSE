#!/usr/bin/python3
import sys
import time
from socket import *

if len(sys.argv) < 3:
    print("command line error")
    sys.exit()

HOST = sys.argv[1]
PORT = int(sys.argv[2])
ADDR = (HOST, PORT)
BUFSIZE = 1024
DELAY = 1000
RTT_group = []

udpsocket = socket(AF_INET, SOCK_DGRAM)
for i in range(10):
    while True:
        try:
            data = f"PING {i} {time.time()} \r\n"
            send_time = int(round(time.time() * 1000))
            udpsocket.sendto(data.encode(), ADDR)
            udpsocket.settimeout(3.0)
            reply, ADDR = udpsocket.recvfrom(BUFSIZE)
            RTT = int(round(time.time() * 1000)) - send_time
            break
        except timeout:
            print("Message lost")
    RTT_group.append(RTT)
    print(f"Ping to {HOST}, seq = {i}, rtt = {RTT} ms")
print(f"Minimum RTT is {min(RTT_group)} ms, maximum RTT is {max(RTT_group)} ms,\
average RTT is {sum(RTT_group)//len(RTT_group)} ms")
        
        
        
    
