
case class Bid(amount: Int) {

  def this(money: Money) { this(money.amount) }

  def +(x: Int): Bid = Bid(amount + x)

  def -(x: Int): Bid = Bid(amount - x)

  def >(that: Bid): Boolean = this.amount > that.amount

  def <(that: Bid): Boolean = this.amount < that.amount

  def asMoney: Money = Money(amount)

  def max(x: Int): Bid = Bid(amount max x)

  def min(bid: Bid): Bid = Bid(amount min bid.amount)

  def min(x: Int): Bid = Bid(amount min x)

  /**
   * If a player bids bid:Bid and hasTB:Boolean, what should the other player's
   * best bid:Bid be so that the first player wins:Boolean the auction.
   */
  def otherBid(hasTB: Boolean, wins: Boolean, otherMoney: Money): Bid =
    (hasTB, wins) match {
      case (true, true) => this min otherMoney.asBid
      case (true, false) => this + 1
      case (false, true) => (this - 1) min otherMoney.asBid
      case (false, false) => (this max 1)
    }

  /**
   * Create a Bid range from this Bid to high
   */
  def to(high: Bid): Seq[Bid] = (amount to high.amount) map (Bid(_))

  override def toString = amount.toString

  def validatedBid(player: Player, state: State): Bid = {
    val myMoney = state.playerMoney(player)
    val otherMoneyAsBid = state.playerMoney(player.otherPlayer).asBid
    // maxWinningBid is the most that is needed to win. So never bid more.
    val maxWinningBid: Bid = if (player == state.tieBreaker) otherMoneyAsBid else otherMoneyAsBid + 1
    // Bid no more the maxWinningBid
    val adjustedBid: Bid = this min maxWinningBid
    // maxWinningBid will be 0 if otherMoney == 0 and I have the TieBreaker
    // Bid at least 1 but no more that myMoney if I have 0.
    (adjustedBid max 1) min myMoney.asBid
  }

}

case class Money(amount: Int) {

  def +(value: Double): Double = amount + value

  def +(value: Int): Money = Money(amount + value)

  def +(money: Money): Money = Money(amount + money.amount)

  def -(money: Money): Money = Money(amount - money.amount)

  def asBid: Bid = Bid(amount)

  def min(money: Money): Money = Money(amount min money.amount)

  override def toString = amount.toString

}

case class Player(val id: Int) extends Ordered[Player] {
  import Parameters._

  def compare(that: Player): Int = this.id compare that.id

  def otherPlayer: Player = select(P2, P1)

  def select[A](choice1: => A, choice2: => A): A = if (id == 1) choice1 else choice2

  def select[A](choices: (A, A)): A = select(choices._1, choices._2)

  def tbValues: (Double, Double) = select((tbValue, 0), (0, tbValue))

  override def toString: String = s"P$id"
}

object P1 extends Player(1)

object P2 extends Player(2)

object Poorman {
  import Parameters._

  val magicNumbers: Vector[Double] = Vector(0.00, 2.68, 3.66, 4.23, 4.64, 5.00, 5.36, 5.77, 6.34, 7.32, 10.00).map(_ * 0.1)
  val magicNumbersReverse: Vector[Double] = magicNumbers.reverse

  def basicRatio(pMoney: Money, pTieAdvantage: Double, totalMoney: Double, magicNumber: Double): Double =
    if (magicNumber == 0) 100
    else if (magicNumber == 1) 0.01
    else ((pMoney + pTieAdvantage) / totalMoney) / magicNumber

  def bids(state: State, depth: Int = defaultDepth, width: Int = defaultWidth): (Bid, Bid) = {
    val (p1SortedBids, p2SortedBids) = (bidDoublePairs(state, P1, depth, width), bidDoublePairs(state, P2, depth, width))
    val (p1Bid, p2Bid) = (p1SortedBids.last._1, p2SortedBids.last._1)
    (p1Bid, p2Bid)
  }

  def bidTriples(state: State, player: Player, depth: Int = defaultDepth, width: Int = defaultWidth): Seq[(Bid, State, Double)] =
    state match {
      case State(0, _, _, _) | State(10, _, _, _) => Seq()
      case _ => {

        val pSortedBids: Seq[(Bid, State, Double)] = player.select(sortedBidOptions(state))
        if (depth == 0) pSortedBids
        else {
          val pExpanded: Seq[(Bid, Seq[(Bid, State, Double)])] =
            pSortedBids.map {
              case (b, st, d) => {
                val bidTrips: Seq[(Bid, State, Double)] = bidTriples(st, player, depth - 1, width)
                if (bidTrips.isEmpty) (b, Seq((b, st, d)))
                else (b, bidTrips)
              }
            }
          val pExpandedFlattened: Seq[(Bid, State, Double)] = pExpanded.flatMap { case (b, bsds) => bsds.map { case (_, st, d) => (b, st, d) } }
          val pExpandedSorted: Seq[(Bid, State, Double)] = pExpandedFlattened.sortBy { case (_, _, d: Double) => d }
          val answer: Seq[(Bid, State, Double)] = pExpandedSorted.drop(pExpandedSorted.length - width)
          answer
        }
      }
    }

  /**
   * Returns the bid with the best worst case starting from this state.
   */
  def bidDoublePairs(state: State, player: Player, depth: Int = defaultDepth, width: Int = defaultWidth): Seq[(Bid, Double)] =
    state match {
      case State(0, _, _, _) | State(10, _, _, _) => Seq()
      case _ => {

        val pSortedBids: Seq[(Bid, State, Double)] = player.select(sortedBidOptions(state))
        if (depth == 0) pSortedBids.map { case (b, s, d) => (b, d) }
        else {
          val pPropagated: Seq[(Bid, Double)] =
            pSortedBids.map {
              case (b, st, d) => {
                val bidDoubs: Seq[Double] = bidDoublePairs(st, player, depth - 1, width).map { case (b, d) => d }
                if (bidDoubs.isEmpty) (b, d) else (b, bidDoubs.max)
              }
            }
          val pPropagatedSorted: Seq[(Bid, Double)] = pPropagated.sortBy { case (_, d) => d }
          pPropagatedSorted.drop(pPropagatedSorted.length - width)
        }
      }
    }

  def formulaAR(myMoney: Money, otherMoney: Money, tb: Player, lowPos: Int, highPos: Int): (Double, Double) = {
    val magic: Vector[Double] = if (lowPos < highPos) magicNumbers else magicNumbersReverse
    val (lowMagic, highMagic): (Double, Double) = (magic(lowPos), magic(highPos))
    val (myTB, otherTB): (Int, Int) = tb.select((1, 0), (0, 1))
    val totalMoney: Double = myMoney + otherMoney + tbValue
    val advance: Double = (myMoney.amount - tbValue * myTB - lowMagic * totalMoney) / (1 - lowMagic)
    val retreat: Double = (highMagic * totalMoney - (myMoney + otherTB * tbValue)) / highMagic
    (advance, retreat)
  }

  val sortedOptionArchive: collection.mutable.Map[State, (Seq[(Bid, State, Double)], Seq[(Bid, State, Double)])] =
    collection.mutable.Map[State, (Seq[(Bid, State, Double)], Seq[(Bid, State, Double)])]()

  def sortedBidOptions(state: State): (Seq[(Bid, State, Double)], Seq[(Bid, State, Double)]) = {
    sortedOptionArchive.getOrElse(state, {

      def sortBidOptions(results: Seq[(Bid, Bid, State)], bidSelector: (((Bid, Bid, State)) => Bid), f: (State => Double)): Seq[(Bid, State, Double)] = {
        // Put the Ratio before the State for comparison purposes when taking the min of (Ratio, State) pairs.
        val bidsRatios: Seq[(Bid, Seq[(Double, State)])] =
          results.groupBy(bidSelector).mapValues(_.map { case (_, _, state) => (f(state), state) }).toSeq

        // Associate a bid with either its worst case outcome.
        val bidWorstCases: Seq[(Bid, State, Double)] =
          bidsRatios.map {
            case (bid, ratioStates) =>
              val (low, state) = ratioStates.min
              // Here's where we switch the Ratio and State
              (bid, state, low)
          }
        bidWorstCases.sortBy { case (_, _, ratio) => ratio }
      }

      val results: Seq[(Bid, Bid, State)] = state.possibleBidResults

      val p1SortedBids = sortBidOptions(results, _._1, _.ratioForPlayer(P1))
      val p2SortedBids = sortBidOptions(results, _._2, _.ratioForPlayer(P2))
      sortedOptionArchive(state) = (p1SortedBids, p2SortedBids)
      sortedOptionArchive(state)
    })
  }

}

case class State(pos: Int, p1Money: Money, p2Money: Money, tieBreaker: Player) extends Ordered[State] {
  import Poorman._
  import Parameters._

  def this(pos: Int, money1: Int, money2: Int, tieBreaker: Player) {
    this(pos, Money(money1), Money(money2), tieBreaker)
  }

  def bidForPlayer(player: Player): Bid = {
    if (basicTrace) Console.err.println(logString)
    val (p1BidStateDouble, p2BidStateDouble) = bids(this)
    val (p1Bid, p2Bid) = (p1BidStateDouble, p2BidStateDouble)
    if (basicTrace) Console.err.println(f"Best bids: ${(p1Bid, p2Bid)}")
    val bid: Bid = player.select(p1Bid, p2Bid)
    val vBid = bid.validatedBid(player, this)
    if (basicTrace) Console.err.println(s"Validated bid for $player: $bid -> $vBid")
    vBid
  }

  /**
   * The more totalMoney a state has the earlier it is.
   */
  def compare(that: State): Int = (that.totalMoney) compare (this.totalMoney)

  def get1To2Ratio: Double = {
    val (p1TieAdvantage, p2TieAdvantage) = tieBreaker.tbValues
    val p1Ratio = basicRatio(p1Money, p1TieAdvantage, totalMoney, magicNumbers(pos))
    val p2Ratio = basicRatio(p2Money, p2TieAdvantage, totalMoney, magicNumbersReverse(pos))
    p1Ratio / p2Ratio
  }

  /**
   * The overall framework for determining the bid.
   *   o If there is a book bid, use it.
   *   o If search finds a bid use it.
   *   o Use the formula bid.
   * Whichever approach is used, validate the bid by comparison with 1, myMoney, and otherMoney
   * taking the TieBreaker into account.
   */
  def getComponents = (pos, p1Money, p2Money, tieBreaker)

  def logString = {
    val ratio = get1To2Ratio
    val scr = if (ratio > 100 || ratio < 0.001) "  ---" else f"${(((ratio max 1d / ratio) - 1) * 100).toInt}%5d"
    val scoreString = if (ratio > 1) f"<<$scr   " else if (ratio < 1) f"  $scr   >>" else f"   $scr   "
    s"$this${if (p1Money + p2Money == Money(200)) "" else " "}\t$scoreString"
  }

  /**
   * If player bids bid, what happens if the player does or does not have the tiebreaker and if the player does or does not win.
   */
  def nextState(player: Player, bid: Bid, wins: Boolean): State = {
    val otherPlayerBid = bid.otherBid(player == tieBreaker, wins, playerMoney(player.otherPlayer))
    val (p1Bid, p2Bid) = player.select((bid, otherPlayerBid), (otherPlayerBid, bid))
    successor(p1Bid, p2Bid)
  }

  def playerMoney(player: Player): Money = player.select(p1Money, p2Money)

  /**
   * Produces a List[Bid1, Bid2, State]: for each bid level what are the two possible state?
   * One outcomes will correspond to P1(P2) winning(losing); the other to P1(P2) losing(winning).
   */
  def possibleBidResults: Seq[(Bid, Bid, State)] = {
    val (p1Advance, p1Retreat): (Double, Double) = formulaAR(p1Money, p2Money, tieBreaker, pos - 1, pos + 1)
    val (p2Advance, p2Retreat): (Double, Double) = formulaAR(p2Money, p1Money, tieBreaker, pos + 1, pos - 1)
    val ends: Vector[Double] = Vector(p1Advance, p1Retreat, p2Advance, p2Retreat)
    val minMoneyAsBid = (p1Money min p2Money).asBid
    val low = Bid(Math.floor(ends.min - 1).toInt max 1) min minMoneyAsBid
    val high = Bid(Math.ceil(ends.max + 1).toInt) min (minMoneyAsBid + 1)
    for {
      p1Bid <- low to high
      if (p1Bid == p1Bid.validatedBid(P1, this))
      wins <- Vector(true, false)
      val p2Bid = p1Bid.otherBid(P1 == tieBreaker, wins, p2Money)
      if (p2Bid == p2Bid.validatedBid(P2, this))
    } yield (p1Bid, p2Bid, successor(p1Bid, p2Bid))
  }

  def ratioForPlayer(player: Player): Double = player.select(get1To2Ratio, 1 / get1To2Ratio)

  /**
   * Propagate the state by one bidding turn
   */
  def successor(bids: (Bid, Bid)): State = successor(bids._1, bids._2)

  def successor(p1Bid: Bid, p2Bid: Bid): State =
    if (p1Bid > p2Bid) State(pos - 1, p1Money - p1Bid.asMoney, p2Money, tieBreaker)
    else if (p1Bid < p2Bid) State(pos + 1, p1Money, p2Money - p2Bid.asMoney, tieBreaker)
    else tieBreaker.select(State(pos - 1, p1Money - p1Bid.asMoney, p2Money, P2),
      /*                */ State(pos + 1, p1Money, p2Money - p2Bid.asMoney, P1))

  def totalMoney = p1Money + p2Money + tbValue

}

object Solution {
  import Parameters._

  def intToPlayer(i: Int): Player = List(P1, P2)(i - 1)

  def main(args: Array[String]) = {
    val myPlayer = intToPlayer(Console.readLine.toInt)
    val _scotch_pos_ignored = Console.readLine
    val p1Bids = stringToBidArray(Console.readLine)
    val p2Bids = stringToBidArray(Console.readLine)
    val state = propagateBids(new State(5, 100, 100, P1), p1Bids, p2Bids, myPlayer)
    println(state.bidForPlayer(myPlayer))
  }

  /**
   * Propagate the moves from the start state and return the final State
   */
  def propagateBids(startState: State, p1Bids: Array[Bid], p2Bids: Array[Bid], myPlayer: Player = Player(0)): State =
    (p1Bids zip p2Bids).foldLeft {
      startState
    } {
      case (state, (bid1, bid2)) => {
        if (tracePropagate) {
          state.bidForPlayer(myPlayer)
          if (basicTrace) Console.err.println(s"Actual bids: ${(bid1, bid2)}\n")
        }
        state.successor(bid1, bid2)
      }
    }

  def stringToBidArray(s: String): Array[Bid] = s.trim.split(" +").filter(_ != "").map(b => Bid(b.toInt))

}

object Parameters {
  val basicTrace = true // false // 
  val defaultDepth = 3
  val defaultWidth = 2
  val tbValue = 0.06 // 0.5 // 0.13
  val tracePropagate = true // false //  
}


