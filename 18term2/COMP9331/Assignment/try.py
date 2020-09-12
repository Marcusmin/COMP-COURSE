file = open('test0.pdf', 'rb')
MSS = 100
file_chunks = []
payload = file.read(MSS)
file_chunks.append(payload)
while payload:
	payload = file.read(MSS)
	file_chunks.append(payload)
while file_chunks:
	print(file_chunks.pop())