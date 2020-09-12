import sys
class STP_segment:
	'''
	header part of stp segment
	'''
	def __init__(self, source_port = 0, dest_port = 0, seq_num = 0,\
	 signal = '', ack_num = 0):
		'''
		signal == syn:
			3-way handshake
		signal == ''
			sending message
		signal == fin
			4-seg termination
		'''
		self.source_port = source_port
		self.seq_num = seq_num
		self.dest_port = dest_port
		self.signal = signal
		self.ack_num = ack_num
		self.payload = None
		self.length = 0
	def upload_payload(self, data = None):
		'''
		input: data going to send
		output: none
		'''
		self.payload = data
		self.length = sys.getsizeof(self.payload)
		
