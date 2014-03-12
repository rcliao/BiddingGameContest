#!/usr/bin/env python
from subprocess import Popen, PIPE
from os import listdir
import random
import logging
import time

botFiles = {}
botPlays = {}

def initGame():
    game = Popen(['java', 'game/BiddingGame'], stdin=PIPE, stdout=PIPE)
    logging.info('Created subprocess for game ' + str(game.pid))
    return game

def getState(game):
    game.stdin.write('+state\n')
    game.stdin.flush()
    pos = game.stdout.readline()
    p1moves = game.stdout.readline()
    p2moves = game.stdout.readline()

    return pos, p1moves, p2moves

def getGameState(game):
    game.stdin.write('+gameState\n')
    game.stdin.flush()
    end = game.stdout.readline().strip()

    return end

def getWinner(game):
    game.stdin.write('+winner\n')
    game.stdin.flush()
    winner = game.stdout.readline().strip()

    return winner

def doMoves(game, p1bid, p2bid):
    game.stdin.write('-player\n')
    game.stdin.write(p1bid.strip() + '\n')
    game.stdin.write(p2bid.strip() + '\n')
    game.stdin.flush()

def readBotFiles():
    # find out all bot files in bots directory
    for file in listdir("bots"):
        if file.endswith(".java") or file.endswith(".scala") or \
           file.endswith(".py") or file.endswith(".jar"):
            botFiles[file] = 0
            botPlays[file] = 0

def randomPair():
    # pair up random bots
    randomNumbers = random.sample(range(0, len(botFiles)), 2)
    bot1 = botFiles.keys()[randomNumbers[0]]
    bot2 = botFiles.keys()[randomNumbers[1]]

    return bot1, bot2

def initBots(bot1file, bot2file):
    # compile bot before runing it
    autoCompile(bot1file)
    autoCompile(bot2file)

    bot1 = createProcess(bot1file)
    bot2 = createProcess(bot2file)

    return bot1, bot2

def autoCompile(filename):
    if filename.endswith(".scala"):
        pass
    elif filename.endswith(".py"):
        pass
    elif filename.endswith(".java"):
        basename = filename.split(".")[0]
        if basename+".class" not in listdir("bots"):
            compileProcess = Popen(['javac', 'bots/' + filename])
            compileProcess.wait()

def createProcess(botFile):
    if botFile.endswith(".scala"):
        bot = Popen(['scala', 'bots/' + botFile], stdin=PIPE, stdout=PIPE)
        return bot
    elif botFile.endswith(".py"):
        bot = Popen(['python', 'bots/' + botFile], stdin=PIPE, stdout=PIPE)
        return bot
    elif botFile.endswith(".java"):
        basename = botFile.split(".")[0]
        bot = Popen(['java', 'bots/' + basename], stdin=PIPE, stdout=PIPE)
        return bot
    elif botFile.endswith(".jar"):
        bot = Popen(['java', '-jar', 'bots/' + botFile], stdin=PIPE, stdout=PIPE)
        return bot

def displayTable():
    print '| %30s | %10s | %10s |' % ('Bot Name', 'Win Count', '# of Plays')
    print '+-%30s-+-%10s-+-%10s-+' % ('------------------------------', '----------', '----------')
    for b in botFiles.keys():
        print '| %30s | %10f | %10d |' % (b, botFiles[b], botPlays[b])

def graphBottle(pos):
    for i in xrange(int(pos)):
        print '  ',
    print "S"
    print '0  1  2  3  4  5  6  7  8  9  10'

def main():
    # setup logger
    logging.basicConfig(level=logging.INFO)
    logger = logging.getLogger(__name__)

    # setup
    readBotFiles()
    logging.info('Read Bots: ' + str(botFiles))

    # play game
    logging.info('Game is about to start')

    gameid = 1

    try:
        while True:

            bot1file, bot2file = randomPair()

            hdlr = logging.FileHandler('logs/game' + str(gameid) + ' (' + str(bot1file) + ' vs ' + str(bot2file) + ').log')
            formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
            hdlr.setFormatter(formatter)
            logger.addHandler(hdlr)
            logger.setLevel(logging.INFO)

            logger.info('selected bot: ' + bot1file + ', ' + bot2file)

            logger.info('Game 1 starts')

            p1win = 0
            p2win = 0

            for i in xrange(2):
            # not specific enoug

                game = initGame()

                while getGameState(game) != "END":

                    bot1, bot2 = initBots(bot1file, bot2file)

                    pos,p1moves,p2moves = getState(game)

                    p1bid, err = bot1.communicate('1\n' + pos + p1moves + p2moves)
                    p2bid, err = bot2.communicate('2\n' + pos + p1moves + p2moves)

                    doMoves(game, p1bid, p2bid)

                    graphBottle(pos)

                    logger.info('message: ' + '\n' + '1\n' + pos + p1moves + p2moves.strip())
                    logger.info('p1bid : ' + str(p1bid).strip())
                    logger.info('p2bid : ' + str(p2bid).strip())

                winner = getWinner(game)
                if winner == "Player 1":
                    winner = winner + ' ' + bot1file.split(".")[0] + ' | win count ' + str(botFiles[bot1file])
                    p1win += 1 if i is 0 else 0
                    p2win += 0 if i is 0 else 1
                else:
                    winner = winner + ' ' + bot2file.split(".")[0] + ' | win count ' + str(botFiles[bot2file])
                    p1win += 0 if i is 0 else 1
                    p2win += 1 if i is 0 else 0

                logger.info('WINNER IS: ' + winner)

                logger.info('\n\n')

                sorted(botFiles.items(), key=lambda x: x[1])
                displayTable()
                time.sleep(2)

                logger.info('\n\n')
                game.kill()

                tempbot = bot1file
                bot1file = bot2file
                bot2file = tempbot

                logger.info('Game 2 starts')

            if p1win == p2win:
                botFiles[bot1file] += 0.5
                botFiles[bot2file] += 0.5
            elif p1win > p2win:
                botFiles[bot1file] += 1
                botFiles[bot2file] -= 1
            else:
                botFiles[bot2file] += 1
                botFiles[bot1file] -= 1
            botPlays[bot1file] += 1
            botPlays[bot2file] += 1
            gameid += 1

    except KeyboardInterrupt:
        game.kill()
        bot1.kill()
        bot2.kill()
        compileProcess.kill()


if __name__ == "__main__":
    main()