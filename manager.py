#!/usr/bin/env python
from subprocess import Popen, PIPE
from os import listdir
import random

botFiles = []

def initGame():
	game = Popen(['java', 'game/BiddingGame'], stdin=PIPE, stdout=PIPE)
	return game

def getState(game):
	game.stdin.write('+state\n')
	game.stdin.flush()
	state = game.stdout.readline()

	return state

def readBotFiles():
	# find out all bot files in bots directory
	for file in listdir("bots"):
		botFiles.append(file)

def randomPair():
	randomNumbers = random.sample(range(0, len(botFiles)), 2)
	bot1 = botFiles[randomNumbers[0]]
	bot2 = botFiles[randomNumbers[1]]
	return bot1, bot2

def initBots(bot1, bot2):
	autoCompile(bot1)
	autoCompile(bot2)

def autoCompile(filename):
	if filename.endswith(".scala"):
		pass
	elif filename.endswith(".py"):
		pass
	elif filename.endswith(".java"):
		Popen(['javac', 'bots/' + filename])

def main():
	game = initGame()

	readBotFiles()
	bot1, bot2 = randomPair()
	initBots(bot1, bot2)



if __name__ == "__main__":
	main()