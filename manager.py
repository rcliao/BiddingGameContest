#!/usr/bin/env python
from subprocess import Popen, PIPE

game = Popen(['java', 'game/BiddingGame'], stdin=PIPE, stdout=PIPE)

game.stdin.write('Hello\n')
game.stdin.flush()
counter = game.stdout.readline()

print str(counter)