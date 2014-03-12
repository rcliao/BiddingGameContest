#!/usr/bin/env python
from subprocess import Popen, PIPE
from os import listdir

botFiles = []

def initGame():
	game = Popen(['java', 'game/BiddingGame'], stdin=PIPE, stdout=PIPE)

	game.stdin.write('Hello\n')
	game.stdin.flush()
	counter = game.stdout.readline()

	print str(counter)

def initBots():
	# find out all bot files in bots directory
	for file in listdir("bots"):
		botFiles.append(file)

def main():
	initBots()

if __name__ == "__main__":
	main()