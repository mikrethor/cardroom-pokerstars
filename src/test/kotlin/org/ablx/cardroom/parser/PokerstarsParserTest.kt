package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.enumeration.Currency
import org.ablx.cardroom.commons.enumeration.Domain
import org.ablx.cardroom.commons.enumeration.Operator
import org.junit.Test
import java.io.File
import java.util.*
import kotlin.test.assertEquals


class PokerstarsParserTest {


    private fun createParser(): Parser {
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
    fun testRealBuyIn() {
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
    fun testSmallBlind() {
        val parser: Parser = createParser()
        parser.setCurrency(Currency.EURO)
        var result: Double = parser.parseSmallBlind("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(10.0, result)

        result = parser.parseSmallBlind("PokerStars Hand #103356159434: Tournament #780452500, $0.89+$0.11 USD Hold'em No Limit - Level I (10/20) - 2015/12/12 20:09:03 ET]")
        assertEquals(10.0, result)
    }

    @Test
    fun testBigBlind() {
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
    fun testTableId() {
        val parser: Parser = createParser()
        assertEquals("Freeroll", parser.parseTableId("Table: 'Super Freeroll Stade 2(55153749)#0' 6-max (real money) Seat #5 is the button"))
    }

    @Test
    fun testParse() {
        val parser: Parser = createParser()
        val hands=parser.parse()

       assertEquals(97, hands.values.size)
    }


}