#!/usr/bin/python3
#PLD module's constants
import random
import sys
'''
pDrop = 0
pDuplicate = 0
pCorrupt = 0
pOrder = 0
maxOrder = 0   
pDelay = 0
'''
seed = sys.argv[14]
random.seed(seed)
#SegmentOrder = 0	#count how many segmemt has been sent
#PLD module's functions


def drop_packets(pDrop):
	probability_of_drop = pDrop
	dice = random.random()
	if dice < probability_of_drop:
		return True


def duplicate_packets(pDuplicate):
	probability_of_duplicate = pDuplicate
	dice = random.random()
	if dice < probability_of_duplicate:
		return True

def create_bit_error_within_packets(pCorrupt):
	probability_of_corrupt = pCorrupt
	dice = random.random()
	if dice < probability_of_corrupt:
		print(f"Corrupt packet")
		return True

def transmits_out_of_order_packet(pOrder):
	probability_of_reorder = pOrder
	dice = random.random()
	if dice < probability_of_reorder:
		return True

def delays_packet(pDelay, MaxDelay):
	probability_of_delay = pDelay
	dice = random.random()
	if dice < probability_of_delay:
		return True

	
