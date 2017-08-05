package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.HandAction
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.enumeration.*
import org.ablx.cardroom.commons.enumeration.Currency
import org.junit.Test
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


open class PokerstarsParserTest {

    open fun createParser(): Parser {
        val cardroom = Cardroom(1, Operator.POKERSTARS, Domain.COM, "")

        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("HandTournamentTestFile.txt")!!.file)

        val parser: Parser = PokerstarsParser(cardroom, file.absolutePath)
        parser.setCurrency(Currency.EURO)
        return parser
    }

    @Test
    fun testBuyInTicketOnly() {
        val parser: Parser = createParser()

        val buyIn: Double = parser.parseBuyIn("PokerStars Hand #173421589642: Tournament #1952630414, Freeroll  Hold'em No Limit - Level II (30/60) - 2017/07/23 17:12:53 ET")
        assertEquals(0.0, buyIn)

        val fee: Double = parser.parseFee("PokerStars Hand #173421589642: Tournament #1952630414, Freeroll  Hold'em No Limit - Level II (30/60) - 2017/07/23 17:12:53 ET")
        assertEquals(0.0, fee)
    }

    @Test
    open fun testRealBuyIn() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        val result: Double = parser.parseBuyIn("PokerStars Hand #103356159434: Tournament #780452500, €0.89+€0.11 EUR Hold'em No Limit - Level I (10/20) - 2013/08/28 22:01:28 CET [2013/08/28 16:01:28 ET]")
        assertEquals(1.00, result)

        val fee: Double = parser.parseFee("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(0.11, fee)
    }

    @Test
    fun testHandId() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: String = parser.parseHandId("PokerStars Hand #103356159434: Tournament #780452500, €0.89+€0.11 EUR Hold'em No Limit - Level I (10/20) - 2013/08/28 22:01:28 CET [2013/08/28 16:01:28 ET]")
        assertEquals("103356159434", result)

        result = parser.parseHandId("PokerStars Hand #1033561594356: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals("1033561594356", result)


    }

    @Test
    fun testLevel() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Int = parser.parseLevel("PokerStars Hand #103356159434: Tournament #780452500, €0.89+€0.11 EUR Hold'em No Limit - Level I (10/20) - 2013/08/28 22:01:28 CET [2013/08/28 16:01:28 ET]")
        assertEquals(1, result)

        result = parser.parseLevel("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(1, result)
    }

    @Test
    open fun testSmallBlind() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Double = parser.parseSmallBlind("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(10.0, result)

        result = parser.parseSmallBlind("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(10.0, result)
    }

    @Test
    open fun testBigBlind() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Double = parser.parseBigBlind("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(20.0, result)

        result = parser.parseBigBlind("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(20.0, result)

    }

    @Test
    fun testCurrency() {
        val parser: Parser = createParser()

        var result: Currency = parser.parseCurrency("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(Currency.USD, result)

        result = parser.parseCurrency("PokerStars Hand #103356159434: Tournament #780452500, €0.89+€0.11 EUR Hold'em No Limit - Level I (10/20) - 2013/08/28 22:01:28 CET [2013/08/28 16:01:28 ET]")
        assertEquals(Currency.EURO, result)

        result = parser.parseCurrency("PokerStars Hand #173421589642: Tournament #1952630414, Freeroll  Hold'em No Limit - Level II (30/60) - 2017/07/23 17:12:53 ET")
        assertEquals(Currency.PLAY_MONEY, result)
    }

    @Test
    fun testGameIdCardroom() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        val result: String = parser.parseGameIdCardroom("HH20130828 T780452500 Hold'em No Limit 0,89 € + 0,11 €.txt")
        assertEquals("780452500", result)
    }

    @Test
    fun testNumberOfPalyersByTable() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Int = parser.parseNumberOfPlayerByTable("Table: 'Qualif. Ticket 5€(201796103)#005' 9-max (real money) Seat #9 is the button")
        assertEquals(9, result)

        result = parser.parseNumberOfPlayerByTable(" Table: 'Nice 05' 5-max (real money) Seat #2 is the button")
        assertEquals(5, result)

    }

    @Test
    fun testPlayerSeat() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var player: Player = parser.parsePlayerSeat("Seat 1: dragoonnhead (1500 in chips))")

        assertEquals("dragoonnhead", player.name)
        assertEquals(true, player.on)
        assertEquals(1, player.seat)
        assertEquals(1500.0, player.stack)

        player = parser.parsePlayerSeat("Seat 2: Sirius369 (1500 in chips)")

        assertEquals("Sirius369", player.name)
        assertEquals(true, player.on)
        assertEquals(2, player.seat)
        assertEquals(1500.0, player.stack)

        player = parser.parsePlayerSeat("Seat 3: acrisdu11 (1500 in chips)")

        assertEquals("acrisdu11", player.name)
        assertEquals(true, player.on)
        assertEquals(3, player.seat)
        assertEquals(1500.0, player.stack)

        player = parser.parsePlayerSeat("Seat 4: polo21544 (1500 in chips)")

        assertEquals("polo21544", player.name)
        assertEquals(true, player.on)
        assertEquals(4, player.seat)
        assertEquals(1500.0, player.stack)


        player = parser.parsePlayerSeat("Seat 5: backsidair (1500 in chips)")

        assertEquals("backsidair", player.name)
        assertEquals(true, player.on)
        assertEquals(5, player.seat)
        assertEquals(1500.0, player.stack)

        player = parser.parsePlayerSeat("Seat 6: nivekash (1500 in chips)")

        assertEquals("nivekash", player.name)
        assertEquals(true, player.on)
        assertEquals(6, player.seat)
        assertEquals(1500.0, player.stack)

        player = parser.parsePlayerSeat("Seat 7: mikrethor (1500 in chips)")

        assertEquals("mikrethor", player.name)
        assertEquals(true, player.on)
        assertEquals(7, player.seat)
        assertEquals(1500.0, player.stack)

        player = parser.parsePlayerSeat("Seat 8: bakoly (1500 in chips)")

        assertEquals("bakoly", player.name)
        assertEquals(true, player.on)
        assertEquals(8, player.seat)
        assertEquals(1500.0, player.stack)

        player = parser.parsePlayerSeat("Seat 9: misterwill8 (1500 in chips) is sitting out")

        assertEquals("misterwill8", player.name)
        assertEquals(true, player.on)
        assertEquals(9, player.seat)
        assertEquals(1500.0, player.stack)
    }

    @Test
    fun testButtonSeat() {
        val parser: Parser = createParser()

        assertEquals(1, parser.parseButtonSeat("Table '780452500 1' 9-max Seat #1 is the button"))

        assertEquals(9, parser.parseButtonSeat("Table '780452500 1' 9-max Seat #9 is the button"))
    }

    @Test
    fun testHandDate() {
        val parser: Parser = createParser()
        val calendar = GregorianCalendar(2015, 11, 12, 20, 9, 3)
        assertEquals(calendar.time, parser.parseHandDate("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]"))
    }

    @Test
    fun testPlayerAccount() {
        val parser: Parser = createParser()
        //TODO parse a player with card
        assertEquals("mikrethor", parser.parsePlayerAccount("Dealt to mikrethor [7s 5s]"))
    }

    @Test
    open fun testTableId() {
        val parser: Parser = createParser()
        assertEquals("Freeroll", parser.parseTableId("Table: 'Super Freeroll Stade 2(55153749)#0' 6-max (real money) Seat #5 is the button"))
    }

    @Test
    open fun testParse() {
        val parser: Parser = createParser()
        val hands = parser.parse()

        assertEquals(97, hands.values.size)
    }

    @Test
    open fun testTextToHand() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        val handText = "PokerStars Hand #103356493800: Tournament #780452500, €0.89+€0.11 EUR Hold'em No Limit - Level I (10/20) - 2013/08/28 22:07:42 CET [2013/08/28 16:07:42 ET]\n" +
                "Table '780452500 1' 9-max Seat #7 is the button\n" +
                "Seat 1: dragoonnhead (1880 in chips)\n" +
                "Seat 2: Sirius369 (1350 in chips)\n" +
                "Seat 3: acrisdu11 (1450 in chips)\n" +
                "Seat 5: backsidair (1640 in chips)\n" +
                "Seat 6: nivekash (1120 in chips)\n" +
                "Seat 7: mikrethor (1820 in chips)\n" +
                "Seat 8: bakoly (1460 in chips)\n" +
                "Seat 9: misterwill8 (2780 in chips)\n" +
                "bakoly: posts small blind 10\n" +
                "misterwill8: posts big blind 20\n" +
                "*** HOLE CARDS ***\n" +
                "Dealt to mikrethor [7s 9c]\n" +
                "dragoonnhead: folds\n" +
                "Sirius369: calls 20\n" +
                "acrisdu11: folds\n" +
                "backsidair: folds\n" +
                "nivekash: calls 20\n" +
                "mikrethor: folds\n" +
                "bakoly: folds\n" +
                "misterwill8: checks\n" +
                "*** FLOP *** [3c Ah Kh]\n" +
                "misterwill8: checks\n" +
                "Sirius369: checks\n" +
                "nivekash: checks\n" +
                "*** TURN *** [3c Ah Kh] [Qs]\n" +
                "misterwill8: checks\n" +
                "Sirius369: checks\n" +
                "nivekash: checks\n" +
                "*** RIVER *** [3c Ah Kh Qs] [3d]\n" +
                "misterwill8: checks\n" +
                "Sirius369: bets 40\n" +
                "nivekash: calls 40\n" +
                "misterwill8: folds\n" +
                "*** SHOW DOWN ***\n" +
                "Sirius369: shows [Qh Js] (two pair, Queens and Threes)\n" +
                "nivekash: shows [Kd 5s] (two pair, Kings and Threes)\n" +
                "nivekash collected 150 from pot\n" +
                "*** SUMMARY ***\n" +
                "Total pot 150 | Rake 0\n" +
                "Board [3c Ah Kh Qs 3d]\n" +
                "Seat 1: dragoonnhead folded before Flop (didn't bet)\n" +
                "Seat 2: Sirius369 showed [Qh Js] and lost with two pair, Queens and Threes\n" +
                "Seat 3: acrisdu11 folded before Flop (didn't bet)\n" +
                "Seat 5: backsidair folded before Flop (didn't bet)\n" +
                "Seat 6: nivekash showed [Kd 5s] and won (150) with two pair, Kings and Threes\n" +
                "Seat 7: mikrethor (button) folded before Flop (didn't bet)\n" +
                "Seat 8: bakoly (small blind) folded before Flop\n" +
                "Seat 9: misterwill8 (big blind) folded on the River"
        val hand = parser.textToHand(handText)

        assertEquals("103356493800", hand.cardroomHandId)
        assertEquals("mikrethor", hand.accountPlayer!!.name)
        assertEquals(20.0, hand.bigBlind)
        assertEquals(10.0, hand.smallBlind)
        assertEquals(7, hand.buttonSeat)
        assertEquals(1.00, hand.buyIn)
        assertEquals(0.11, hand.fee)
        assertEquals(8, hand.players.keys.size)

        assertEquals("dragoonnhead", hand.players[1]!!.name)
        assertEquals(0, hand.players[1]!!.id)
        assertEquals(1880.0, hand.players[1]!!.stack)
        assertEquals(1, hand.players[1]!!.seat)

        assertPlayer(hand.players[1]!!, "dragoonnhead", 0, 1880.0, 1)
        assertPlayer(hand.players[2]!!, "Sirius369", 0, 1350.0, 2)
        assertPlayer(hand.players[3]!!, "acrisdu11", 0, 1450.0, 3)

        assertPlayer(hand.players[5]!!, "backsidair", 0, 1640.0, 5)
        assertPlayer(hand.players[6]!!, "nivekash", 0, 1120.0, 6)
        assertPlayer(hand.players[7]!!, "mikrethor", 0, 1820.0, 7)
        assertPlayer(hand.players[8]!!, "bakoly", 0, 1460.0, 8)
        assertPlayer(hand.players[9]!!, "misterwill8", 0, 2780.0, 9)

        assertHandAction(hand.preflopActions[0], Action.FOLDS, 0.0, "dragoonnhead", Round.PRE_FLOP, null)
        assertHandAction(hand.preflopActions[1], Action.CALLS, 20.0, "Sirius369", Round.PRE_FLOP, null)
        assertHandAction(hand.preflopActions[2], Action.FOLDS, 0.0, "acrisdu11", Round.PRE_FLOP, null)
        assertHandAction(hand.preflopActions[3], Action.FOLDS, 0.0, "backsidair", Round.PRE_FLOP, null)
        assertHandAction(hand.preflopActions[4], Action.CALLS, 20.0, "nivekash", Round.PRE_FLOP, null)
        assertHandAction(hand.preflopActions[5], Action.FOLDS, 0.0, "mikrethor", Round.PRE_FLOP, null)
        assertHandAction(hand.preflopActions[6], Action.FOLDS, 0.0, "bakoly", Round.PRE_FLOP, null)
        assertHandAction(hand.preflopActions[7], Action.CHECKS, 0.0, "misterwill8", Round.PRE_FLOP, null)


        assertHandAction(hand.flopActions[0], Action.CHECKS, 0.0, "misterwill8", Round.FLOP, null)
        assertHandAction(hand.flopActions[1], Action.CHECKS, 0.0, "Sirius369", Round.FLOP, null)
        assertHandAction(hand.flopActions[2], Action.CHECKS, 0.0, "nivekash", Round.FLOP, null)

        assertHandAction(hand.turnActions[0], Action.CHECKS, 0.0, "misterwill8", Round.TURN, null)
        assertHandAction(hand.turnActions[1], Action.CHECKS, 0.0, "Sirius369", Round.TURN, null)
        assertHandAction(hand.turnActions[2], Action.CHECKS, 0.0, "nivekash", Round.TURN, null)


        assertHandAction(hand.riverActions[0], Action.CHECKS, 0.0, "misterwill8", Round.RIVER, null)
        assertHandAction(hand.riverActions[1], Action.BETS, 40.0, "Sirius369", Round.RIVER, null)
        assertHandAction(hand.riverActions[2], Action.CALLS, 40.0, "nivekash", Round.RIVER, null)
        assertHandAction(hand.riverActions[3], Action.FOLDS, 0.0, "misterwill8", Round.RIVER, null)

        var cards = arrayOf(Card.C_QH, Card.C_JS)
        assertHandAction(hand.showdownActions[0], Action.SHOWS, 0.0, "Sirius369", Round.SHOWDOWN, cards)
        cards = arrayOf(Card.C_KD, Card.C_5S)
        assertHandAction(hand.showdownActions[1], Action.SHOWS, 0.0, "nivekash", Round.SHOWDOWN, cards)
        assertHandAction(hand.showdownActions[2], Action.COLLECTED, 150.0, "nivekash", Round.SHOWDOWN, null)

        assertHandAction(hand.actions[0], Action.FOLDS, 0.0, "dragoonnhead", Round.PRE_FLOP, null)
        assertHandAction(hand.actions[1], Action.CALLS, 20.0, "Sirius369", Round.PRE_FLOP, null)
        assertHandAction(hand.actions[2], Action.FOLDS, 0.0, "acrisdu11", Round.PRE_FLOP, null)
        assertHandAction(hand.actions[3], Action.FOLDS, 0.0, "backsidair", Round.PRE_FLOP, null)
        assertHandAction(hand.actions[4], Action.CALLS, 20.0, "nivekash", Round.PRE_FLOP, null)
        assertHandAction(hand.actions[5], Action.FOLDS, 0.0, "mikrethor", Round.PRE_FLOP, null)
        assertHandAction(hand.actions[6], Action.FOLDS, 0.0, "bakoly", Round.PRE_FLOP, null)
        assertHandAction(hand.actions[7], Action.CHECKS, 0.0, "misterwill8", Round.PRE_FLOP, null)
        assertHandAction(hand.actions[8], Action.CHECKS, 0.0, "misterwill8", Round.FLOP, null)
        assertHandAction(hand.actions[9], Action.CHECKS, 0.0, "Sirius369", Round.FLOP, null)
        assertHandAction(hand.actions[10], Action.CHECKS, 0.0, "nivekash", Round.FLOP, null)
        assertHandAction(hand.actions[11], Action.CHECKS, 0.0, "misterwill8", Round.TURN, null)
        assertHandAction(hand.actions[12], Action.CHECKS, 0.0, "Sirius369", Round.TURN, null)
        assertHandAction(hand.actions[13], Action.CHECKS, 0.0, "nivekash", Round.TURN, null)
        assertHandAction(hand.actions[14], Action.CHECKS, 0.0, "misterwill8", Round.RIVER, null)
        assertHandAction(hand.actions[15], Action.BETS, 40.0, "Sirius369", Round.RIVER, null)
        assertHandAction(hand.actions[16], Action.CALLS, 40.0, "nivekash", Round.RIVER, null)
        assertHandAction(hand.actions[17], Action.FOLDS, 0.0, "misterwill8", Round.RIVER, null)

        cards = arrayOf(Card.C_QH, Card.C_JS)
        assertHandAction(hand.actions[18], Action.SHOWS, 0.0, "Sirius369", Round.SHOWDOWN, cards)
        cards = arrayOf(Card.C_KD, Card.C_5S)

        assertHandAction(hand.actions[19], Action.SHOWS, 0.0, "nivekash", Round.SHOWDOWN, cards)
        assertHandAction(hand.actions[20], Action.COLLECTED, 150.0, "nivekash", Round.SHOWDOWN, null)

    }

    private fun assertPlayer(player: Player, name: String, id: Long, stack: Double, seat: Int) {
        assertEquals(name, player.name)
        assertEquals(id, player.id)
        assertEquals(stack, player.stack)
        assertEquals(seat, player.seat)
    }

    private fun assertHandAction(handAction: HandAction, action: Action, amount: Double, name: String, round: Round, cards: Array<Card>?) {
        assertEquals(action, handAction.action)
        assertEquals(amount, handAction.amount)
        handAction.cards
        assertEquals(name, handAction.player!!.name)
        assertEquals(round, handAction.round)
        if (cards != null && handAction.cards != null) {
            var card: Card? = handAction.cards!![0]
            assertEquals(cards[0], card)
            card = handAction.cards!![1]
            assertEquals(cards[1], card)
        }
    }

    @Test
    fun testGameTypeFromFilename() {
        val parser: Parser = createParser()
        assertEquals(GameType.TOURNAMENT, parser.getGameTypeFromFilename("HH20140116 T850475657 Hold'em No Limit 2,68 € + 0,32 €.txt"))

        assertEquals(GameType.CASH, parser.getGameTypeFromFilename("HH20151212 Tethys II - 0,01 \$-0,02 \$ - USD Hold'em No Limit.txt"))
    }

    @Test
    fun testIsUselessLine() {
        val parser: Parser = createParser()
        assertTrue("DoBrazil: sits out ".endsWith(PokerstarsActions.SITS_OUT.value))

        assertEquals(true, parser.isUselessLine("DoBrazil: sits out "))

    }
}