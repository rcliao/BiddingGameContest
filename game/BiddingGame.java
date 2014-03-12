package game;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class BiddingGame {
    private int pos;
    private int draw;

    private int firstMoney;
    private int secondMoney;

    private String winner;

    private List<Integer> first_moves;
    private List<Integer> second_moves;

    public BiddingGame() {
        this.pos = 5;
        this.draw = 1;
        this.firstMoney = 100;
        this.secondMoney = 100;
        this.winner = "Tie";
        this.first_moves = new ArrayList<Integer>();
        this.second_moves = new ArrayList<Integer>();
    }

    public int getPos() {
        return pos;
    }

    public String getWinner() {
        return winner;
    }

    public int[] getFirstMoves() {
        int[] result = new int[first_moves.size()];
        for (int i = 0; i < first_moves.size(); i ++) {
            result[i] = first_moves.get(i);
        }
        return result;
    }

    public String getFirstMovesStr() {
        return first_moves.toString().substring(1,
            first_moves.toString().length()-1);
    }

    public int[] getSecondMoves() {
        int[] result = new int[second_moves.size()];
        for (int i = 0; i < second_moves.size(); i ++) {
            result[i] = second_moves.get(i);
        }
        return result;
    }

    public String getSecondMovesStr() {
        return second_moves.toString().substring(1,
            second_moves.toString().length()-1);
    }

    public boolean isGameEnded() {
        // return true whenever the below situation is true
        if (pos == 0) {
            winner = "Player 1";
        } else if (pos == 10) {
            winner = "Player 2";
        }
        return (pos == 0 || pos == 10 || !winner.equals("Tie"));
    }

    public void addPlayer1Moves(int p1bid) {
        first_moves.add(p1bid);
    }

    public void addPlayer2Moves(int p2bid) {
        second_moves.add(p2bid);
    }

    public String doMoves(int p1bid, int p2bid) {
        // return 1 of the following:
        //   1. "Valid" for the valid move
        //   2. "P1-Invalid" for the invalid move from player 1 bid,
        //       also update winner to player2
        //   3. "P2-Invalid" for the invalid move from player 2 bid,
        //       also update winner to player1
        //   4. "Tie-Invalid" for when both player enters invalid moves
        addPlayer1Moves(p1bid);
        addPlayer2Moves(p2bid);

        if (!checkP1Bid(p1bid) && !checkP2Bid(p2bid)) {
            return "Tie-Invalid";
        } else if (!checkP1Bid(p1bid)) {
            winner = "Player 2";
            return "P1-Invalid";
        } else if (!checkP2Bid(p2bid)) {
            winner = "Player 1";
            return "P2-Invalid";
        } else {
            if (p1bid > p2bid) {
                firstMoney -= p1bid;
                pos --;
            } else if (p2bid > p1bid) {
                secondMoney -= p2bid;
                pos ++;
            } else {
                if (draw % 2 == 1) {
                    firstMoney -= p1bid;
                    pos --;
                } else {
                    secondMoney -= p2bid;
                    pos ++;
                }
                draw ++;
            }
        }
        return "";
    }

    public boolean checkP1Bid(int p1bid) {
        if (firstMoney == 0) {
            return p1bid == 0;
        } else {
            return (p1bid <= firstMoney && p1bid > 0);
        }
    }

    public boolean checkP2Bid(int p2bid) {
        if (secondMoney == 0) {
            return p2bid == 0;
        } else {
            return (p2bid <= secondMoney && p2bid > 0);
        }
    }

    public String graphPosition() {
        String result = "";

        for (int i = 0; i <= 10; i ++) {
            if (i == pos) {
                result += "S";
            } else {
                result += i;
            }
            result += "-";
        }

        return result.substring(0, result.length()-1);
    }

    public String toString() {
        return graphPosition() + "\n" +
            "Player 1 moves: " + first_moves.toString().substring(1,
                first_moves.toString().length()-1) + "\n" +
            "Player 2 moves: " + second_moves.toString().substring(1,
                second_moves.toString().length()-1) + "\n" +
            "Player 1 Money: " + firstMoney + "\n" +
            "Player 2 Money: " + secondMoney;
    }

    public static void main(String[] args) {
        // set up initial game state
        BiddingGame game = new BiddingGame();

        Scanner in = new Scanner(System.in);

        // stay alive to track the game state between calls
        while (true) {
            if (in.hasNextLine()) {
                String option = in.nextLine();

                int p1bid = -1;
                int p2bid = -1;

                if (option.equals("-player")) {
                    // receive input as player1's bid and player2's bid
                    p1bid = Integer.parseInt(in.nextLine().replace("\n", ""));
                    p2bid = Integer.parseInt(in.nextLine().replace("\n", ""));

                    // process the bids
                    game.doMoves(p1bid, p2bid);
                } else if (option.equals("+state")) {
                    // return game state in three lines
                    //   1. pos
                    //   2. first_player moves
                    //   3. second_player moves
                    System.out.println(game.getPos());
                    System.out.println(game.getFirstMovesStr());
                    System.out.println(game.getSecondMovesStr());
                }

            }
        }
    }
}
