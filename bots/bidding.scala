    object Solution {
        def main(args: Array[String]) = { 
            val player = Console.readLine.toInt
            val scotch_pos = Console.readLine.toInt
            val first_moves = Console.readLine
            val second_moves = Console.readLine
            val ratios = List(0.00, 0.268, 0.366, 0.423, 0.464, 0.500, .536, .577, .634, .732, 1.000)


            def suggestBid(pos: Int, myBalance: Int, oppBalance: Int): Double = {
                //(Rt - m) / (R - 1)
                val total = myBalance + oppBalance
                System.err.println("Total = " + total)
                val ratio = ratios(pos)
                System.err.println("Ratio = " + ratio)
                (myBalance - ratio * total) / (1 - ratio)
                /*
                if (pos > 5) {
                    val ratio = ratios(10 - pos)
                    System.err.println("Ratio = " + ratio)
                    (myBalance - ratio * total) / (1 - ratio)
                }
                else {
                    val ratio = ratios(pos)
                    System.err.println("Ratio = " + ratio)
                    (myBalance - ratio * total) / (1 - ratio)
                }
                */
            }
            def betterBid(mySuggestion: Double, oppSuggestion: Double, tiebreaker: Boolean, pos: Int): Int = {
                if(mySuggestion > oppSuggestion) {
                    if(pos > 7 && oppSuggestion < 1) mySuggestion.toInt
                    else if (tiebreaker) oppSuggestion.toInt
                    else (oppSuggestion + 1).toInt
                }
                else mySuggestion.toInt
            }
            def bestBid(suggested: Int, myBalance: Int, oppBalance: Int): Int = {
                if (suggested < 1 && myBalance > 0) 1
                else if (myBalance == 0) 0
                else if (oppBalance == 0) 1
                else suggested
            }

            def tiebreaker(moves1: List[Int], moves2: List[Int], player: Boolean): Boolean = {
                if(moves1.isEmpty) player
                else if (moves1.head == moves2.head) tiebreaker(moves1.tail, moves2.tail, !player)
                else tiebreaker(moves1.tail, moves2.tail, player)
            }

            if (first_moves != "") {


                def Balance(moves1: List[Int], moves2: List[Int], balance: Int, tiebreaker: Boolean): Int = {
                    if(moves1.isEmpty) 100 - balance
                    else if(moves1.head == moves2.head) {
                        if(tiebreaker) Balance(moves1.tail, moves2.tail, balance + moves1.head, !tiebreaker)
                        else Balance(moves1.tail, moves2.tail, balance, !tiebreaker)
                    }
                    else if(moves1.head > moves2.head) Balance(moves1.tail, moves2.tail, balance + moves1.head, tiebreaker)
                    else Balance(moves1.tail, moves2.tail, balance, tiebreaker)
                }


                def Solve(pos: Int, myBalance: Int, oppBalance: Int, tiebreaker: Boolean): Int = {
                    System.err.println("My Balance = " + myBalance + " opBalance = " + oppBalance)
                    val myRatio = myBalance.toDouble / (myBalance.toDouble + oppBalance.toDouble)
                    val oppRatio = oppBalance.toDouble / (myBalance.toDouble + oppBalance.toDouble)
                    System.err.println("My Ratio = " + myRatio + " Op Ratio = " + oppRatio)
                    if(myBalance == 0) 0
                    else if(pos == 1) myBalance
                    else if(pos == 9) oppBalance + 1
                    else {
                        val mySuggest = suggestBid(pos - 1, myBalance, oppBalance)
                        val oppSuggest = suggestBid(pos + 1, oppBalance, myBalance)
                        System.err.println("My Suggest = " + mySuggest + " opSuggest = " + oppSuggest)
                        bestBid(betterBid(mySuggest, oppSuggest, tiebreaker, pos), myBalance, oppBalance)
                    }

                }
                val moves1 = (first_moves.split(" ") map(_.toInt)).toList
                val moves2 = (second_moves.split(" ") map(_.toInt)).toList

                val balance1 = Balance(moves1, moves2, 0, true)
                val balance2 = Balance(moves2, moves1, 0, false)


                if (player == 1) println(Solve(scotch_pos, balance1, balance2, tiebreaker(moves1, moves2, true)))
                else println(Solve(10 - scotch_pos, balance2, balance1, tiebreaker(moves2, moves1, false)))

            }
            else println(13)
        }

    }