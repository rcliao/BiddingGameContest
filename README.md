BiddingGameContest
==================

This is simple contest framework for the bidding game project. This contest will provide a centeral manager to control bots and the game engine.

## Dependencies
* [Python 2.7](http://www.python.org/)

## Run the Project?
After installing python, check if you installed `python` correctly by opening terminal and type in `python` It should direct you to the python interpreter.

You will need to put the bot(s) under the `bots/` folder in order for the manager to find out your bot. And put the game logic under `games/` folder. The manager will create 2 sub-process running the game and the bots accordingly.

Then, you will need to run `manager.py` by `python manager.py` Then, you are done! Enjoy.
