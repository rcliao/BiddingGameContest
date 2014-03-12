#!/bin/python
# Head ends here
import sys
def calculate_bid(player,pos,first_moves,second_moves):
    """your logic here"""
    # Set up the danger zone and compute money
    if player == 1:
        me, opponent, draw = countMoney(first_moves, second_moves, player)
        danger = 9
        winZone = 1
    else:
        me, opponent, draw = countMoney(second_moves, first_moves, player % 2)
        danger = 1
        winZone = 9

    # no money return zero
    if me == 0:
        bid = 0
    # if last move, drop all money
    elif pos == winZone:
        bid = me
    # if danger, pull not losing move
    elif pos == danger:
        if draw % 2 == player % 2:
            bid = (opponent + 1) if opponent <= me else me
        else:
            bid = (opponent + 1) if opponent + 1 <= me else me
    else:
        opponentStep = (10 - pos) if player == 1 else pos

        # winning for sure
        if opponent < opponentStep:
            bid = 1
        else:
            # setting the winning or losting situation
            winning = pos < 5 if player == 1 else pos >= 5

            if winning:
                if draw % 2 == player % 2:
                    bid = int(opponent / opponentStep)
                else:
                    bid = int(opponent / opponentStep)
            else:
                step = pos if player == 1 else (10-pos)
                if draw % 2 == player % 2:
                    bid = int(me / step)
                else:
                    bid = int(me / step)+1

            if me >= bid:
                bid = bid if bid != 0 else 1
            elif me < bid:
                bid = 1
            else:
                bid = 0
    return bid
# counting the money
def countMoney(first_moves, second_moves, player):
    me = 0
    opponent = 0
    draw = 1
    for i in range(len(first_moves)):
        if first_moves[i] > second_moves[i]:
            me += first_moves[i]
        elif first_moves[i] < second_moves[i]:
            opponent += second_moves[i]
        elif draw % 2 == player:
            me += first_moves[i]
            draw += 1
        else:
            opponent += second_moves[i]
            draw += 1

    me = 100 - me
    opponent = 100 - opponent

    return me, opponent, draw % 2
# Tail starts here
#gets the id of the player
player = input()

scotch_pos = input()         #current position of the scotch

first_moves = [int(i) for i in raw_input().split()]
second_moves = [int(i) for i in raw_input().split()]
bid = calculate_bid(player,scotch_pos,first_moves,second_moves)
print bid