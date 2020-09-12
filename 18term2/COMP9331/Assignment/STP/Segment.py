#!/usr/bin/python3
import sys
import struct
class segment:
	'''
	header part of stp segment
	'''
	def __init__(self, source_port, dest_port, seq_num = 0, ack_num = 0, signal = ''):
		'''
		signal == SA:
			3-way handshake
		signal == D
			sending message
		signal == F
			4-seg termination
		'''
		self.source_port = source_port
		self.dest_port = dest_port
		self.seq_num = seq_num
		self.signal = signal
		self.ack_num = ack_num
		self.checksum = None
		self.payload = None
		self.length = 0
	def upload_payload(self, data):
		'''
		input: data going to send
		output: none
		'''
		self.payload = data
		sum = 0b0000000000000000
		base = 0b1111111111111111
		overflowbit = 0b100000000000000
		for byte in data:
			try:
				sum += struct.unpack('B', byte)[0]
				if sum > base:
					sum -= overflowbit
					sum += 1
			except struct.error:
				sum += 0
		self.checksum = base ^ sum
		self.length = sys.getsizeof(self.payload)
