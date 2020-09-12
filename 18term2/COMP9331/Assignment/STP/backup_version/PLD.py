#PLD module's constants
import random
'''
pDrop = 0
pDuplicate = 0
pCorrupt = 0
pOrder = 0
maxOrder = 0   
pDelay = 0
'''
random.seed(50)
SegmentOrder = 0	#count how many segmemt has been sent
#PLD module's functions
NbOfSgmHanleByPLD = 0
NbOfSgmDrop = 0
NbOfSgmDelay = 0
NbOfSgmDuplicate = 0
NbOfSgmCorrupt = 0
NbOfSgmOutOfOrder = 0

reorderBuffer = []
reorderSignal = 0
def drop_packets(pDrop):
	probability_of_drop = pDrop
	dice = random.randint(0, 100) / 100
	if args[1].signal != 'A' and args[1].signal != 'S' and args[1].signal != 'F' and dice < probability_of_drop:
		print(f"drop packet {args[1].seq_num}")
		NbOfSgmDrop += 1
		return True


def duplicate_packets(pDuplicate):
	probability_of_duplicate = pDuplicate
	dice = random.randint(0, 100) / 100
	if args[1].signal != 'A' and args[1].signal != 'S' and args[1].signal != 'F' and dice < probability_of_drop:
		print(f"duplicate packet {args[1].seq_num}")
		NbOfSgmDuplicate += 1
		return True

def create_bit_error_within_packets(pCorrupt):
	probability_of_corrupt = pCorrupt
	dice = random.randint(0, 100) / 100
	if args[1].signal != 'A' and args[1].signal != 'S' and args[1].signal != 'F' and dice < probability_of_corrupt:
		print(f"Corrupt packet {args[1].seq_num}")
		NbOfSgmCorrupt += 1
		return True

def transmits_out_of_order_packet(pOrder):
	probability_of_reorder = pOrder
	dice = random.randint(0, 100) / 100
	if args[1].signal != 'A' and args[1].signal != 'S' and args[1].signal != 'F' and not reorderBuffer \
	and dice < probability_of_corrupt:
		print(f"Reorder packet {args[1].seq_num}")
		NbOfSgmOutOfOrder += 1
		return True

def delays_packet(pDelay, MaxDelay):
	probability_of_delay = pDelay
	dice = random.randint(0, 100) / 100
	if args[1].signal != 'A' and args[1].signal != 'S' and args[1].signal != 'F' and dice < probability_of_delay:
		print(f"Delay packet {args[1].seq_num}")
		NbOfSgmDelay += 1
		return True

def send_delay(MaxDelay):
	delay_duration = random.randrange(0, MaxDelay) / 1000	#millisecond to second
	
