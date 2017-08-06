package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.enumeration.Currency
import org.ablx.cardroom.commons.enumeration.Domain
import org.ablx.cardroom.commons.enumeration.Operator
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals


class PokerstarsCashGameParserTest : PokerstarsParserTest() {

    override fun createParser(): Parser {
        val cardroom = Cardroom(1, Operator.POKERSTARS, Domain.COM, "")

        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("HandCashGameTestFile.txt")!!.file)

        val parser: Parser = PokerstarsCashGameParser(cardroom, file.absolutePath)
        parser.setCurrency(Currency.EURO)
        return parser
    }

    @Test
    override fun testTableId() {
        val parser: Parser = createParser()
        assertEquals("Ambrosia III", parser.parseTableId("Table 'Ambrosia III' 6-max Seat #3 is the button"))
    }


    @Test
    override fun testRealBuyIn() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        val result: Double = parser.parseBuyIn("PokerStars Hand #103356159434: Tournament #780452500, €0.89+€0.11 EUR Hold'em No Limit - Level I (10/20) - 2013/08/28 22:01:28 CET [2013/08/28 16:01:28 ET]")
        assertEquals(0.0, result)

        val fee: Double = parser.parseFee("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(0.0, fee)
    }


    @Test
    override fun testSmallBlind() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Double = parser.parseSmallBlind("PokerStars Hand #109569046051:  Hold'em No Limit (€0.01/€0.02 EUR) - 2014/01/04 14:52:30 CET [2014/01/04 8:52:30 ET]")
        assertEquals(0.01, result)
        parser.setCurrency(Currency.USD)
        result = parser.parseSmallBlind("PokerStars Hand #109569063776:  Hold'em No Limit ($0.01/$0.02 USD) - 2014/01/04 14:52:54 CET [2014/01/04 8:52:54 ET]")
        assertEquals(0.01, result)
    }

    @Test
    override fun testBigBlind() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Double = parser.parseBigBlind("PokerStars Hand #109569046051:  Hold'em No Limit (€0.01/€0.02 EUR) - 2014/01/04 14:52:30 CET [2014/01/04 8:52:30 ET]")
        assertEquals(0.02, result)
        parser.setCurrency(Currency.USD)
        result = parser.parseBigBlind("PokerStars Hand #109569063776:  Hold'em No Limit ($0.01/$0.02 USD) - 2014/01/04 14:52:54 CET [2014/01/04 8:52:54 ET]")
        assertEquals(0.02, result)

    }


    @Test
    override fun testTextToHand() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        val handText = "PokerStars Hand #109569063776:  Hold'em No Limit (€0.01/€0.02 EUR) - 2014/01/04 14:52:54 CET [2014/01/04 8:52:54 ET]\n" +
                "Table 'Ambrosia III' 6-max Seat #4 is the button\n" +
                "Seat 1: Prana_Bindu (€2.06 in chips)\n" +
                "Seat 3: ROGER20220 (€1.86 in chips)\n" +
                "Seat 4: Safakab (€2.87 in chips)\n" +
                "Seat 5: mikrethor (€3.28 in chips)\n" +
                "Seat 6: labayts (€2 in chips)\n" +
                "mikrethor: posts small blind €0.01\n" +
                "labayts: posts big blind €0.02\n" +
                "*** HOLE CARDS ***\n" +
                "Dealt to mikrethor [9d As]\n" +
                "Prana_Bindu: raises €0.04 to €0.06\n" +
                "ROGER20220: folds\n" +
                "Safakab: folds\n" +
                "mikrethor: calls €0.05\n" +
                "labayts: folds\n" +
                "*** FLOP *** [7c Qs 9c]\n" +
                "mikrethor: bets €0.06\n" +
                "Prana_Bindu: calls €0.06\n" +
                "*** TURN *** [7c Qs 9c] [6d]\n" +
                "mikrethor: bets €0.12\n" +
                "Prana_Bindu: folds\n" +
                "Uncalled bet (€0.12) returned to mikrethor\n" +
                "mikrethor collected €0.24 from pot\n" +
                "mikrethor: doesn't show hand\n" +
                "*** SUMMARY ***\n" +
                "Total pot €0.26 | Rake €0.02\n" +
                "Board [7c Qs 9c 6d]\n" +
                "Seat 1: Prana_Bindu folded on the Turn\n" +
                "Seat 3: ROGER20220 folded before Flop (didn't bet)\n" +
                "Seat 4: Safakab (button) folded before Flop (didn't bet)\n" +
                "Seat 5: mikrethor (small blind) collected (€0.24)\n" +
                "Seat 6: labayts (big blind) folded before Flop"
        val hand = parser.textToHand(handText)

        assertEquals("109569063776", hand.cardroomHandId)
        assertEquals("mikrethor", hand.accountPlayer!!.name)
    }

    @Test
    override fun testParse() {
        val parser: Parser = createParser()
        val hands = parser.parse()

        assertEquals(224, hands.values.size)
    }

}
