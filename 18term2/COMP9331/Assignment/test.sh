#!/bin/sh
python3 receiver.py 8080; python3 send.py 127.0.0.1 8080
