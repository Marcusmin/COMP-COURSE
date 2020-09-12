#!/usr/bin/env python3
from socket import *
from time import ctime
import sys

HOST = '' # It can receive from any IP address
PORT = int(sys.argv[1]) #First argument from command line is port
BUFSIZE = 1024 #Maxinum data from client is 1KB
ADDR = (HOST, PORT)

tcp_server_socket = socket(AF_INET, SOCK_STREAM) #Get a tcp socket
tcp_server_socket.bind(ADDR) #Bind socket to address
tcp_server_socket.listen(3) #Maxinum number of connection is 3

while True:
    print("Waiting for connection")
    tcp_client_socket, addr = tcp_server_socket.accept() #Build up connection
    print(f"Connected with {addr}")
    get_msg = tcp_client_socket.recv(BUFSIZE)
    if not get_msg:
        continue
    target_file = get_msg.split()[1][1:]
    if target_file.decode().split('.')[1] == 'html':
        file_format = 'text/html'
    elif target_file.decode().split('.')[1] == 'png':
        file_format = 'image\png'
    print('Sending')
    try:
        file = open(target_file, 'rb')
        send_file = file.read()
        tcp_client_socket.send('HTTP/1.0 200 OK\r\n'.encode())
        tcp_client_socket.send(f"Content-type: {file_format}\r\n\r\n".encode())
        tcp_client_socket.send(send_file)
        print("Send Success")
    except FileNotFoundError:
        tcp_client_socket.send('HTTP/1.0 404 Not Found\r\n'.encode())
        tcp_client_socket.send(f"Content-type: text/html; charset=ISO-8859-1\r\n\r\n".encode())
        tcp_client_socket.send("404 Not Found".encode())
    print('Sending Done')
    tcp_client_socket.close()
tcp_server_socket.close()
        
