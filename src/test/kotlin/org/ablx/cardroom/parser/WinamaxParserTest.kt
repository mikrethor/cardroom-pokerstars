package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.enumeration.Currency
import org.junit.Test
import kotlin.test.assertEquals

class WinamaxParserTest {
    @Test
    fun testBuyInTicketOnly() {
        val parser: Parser = WinamaxParser()
        parser.setCurrency(Currency.EURO)

        val buyIn: Double = parser.parseBuyIn("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: Ticket only level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.0, buyIn)

        val fee: Double = parser.parseFee("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: Ticket only level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.0, fee)
    }


    @Test
    fun testRealBuyIn() {
        val parser: Parser = WinamaxParser()
        parser.setCurrency(Currency.EURO)
        val result: Double = parser.parseBuyIn("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.45, result)

        val fee: Double = parser.parseFee("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0.05, fee)
    }


    @Test
    fun testHandId() {
        val parser: Parser = WinamaxParser()
        parser.setCurrency(Currency.EURO)
        var result: String = parser.parseHandId("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals("236883548206792705-2", result)

        result = parser.parseHandId("Winamax Poker - CashGame - HandId: #9004445-57475-1473728657 - Holdem no limit (0.01€/0.02€) - 2016/09/13 01:04:17 UTC")
        assertEquals("9004445-57475", result)


    }


    @Test
    fun testLevel() {
        val parser: Parser = WinamaxParser()
        parser.setCurrency(Currency.EURO)
        var result: Int = parser.parseLevel("Winamax Poker - Tournament \"Super Freeroll Stade 2\" buyIn: 0.45€ + 0.05€ level: 0 - HandId: #236883548206792705-2-1377708866 - Holdem no limit (10/20) - 2013/08/28 16:54:26 UTC")
        assertEquals(0, result)

        result = parser.parseLevel("Winamax Poker - Tournament \"Qualif. Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 6 - HandId: #866707662845247492-25-1500771243 - Holdem no limit (12/50/100) - 2017/07/23 00:54:03 UTC")
        assertEquals(6, result)
    }

    @Test
    fun testSmallBlind() {
        val parser: Parser = WinamaxParser()
        parser.setCurrency(Currency.EURO)
        var result: Double? = parser.parseSmallBlind("Winamax Poker - Tournament \"Qualif. Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 6 - HandId: #866707662845247492-25-1500771243 - Holdem no limit (12/50/100) - 2017/07/23 00:54:03 UTC")
        assertEquals(50.0, result)

        result = parser.parseSmallBlind("Winamax Poker - Tournament \"Qualif . Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 9 - HandId: #866707662845247494-4-1500772425 - Holdem no limit (25/100/200) - 2017/07/23 01:13:45 UTC")
        assertEquals(100.0, result)
    }

    @Test
    fun testBigBlind() {
        val parser: Parser = WinamaxParser()
        parser.setCurrency(Currency.EURO)
        var result: Double? = parser.parseBigBlind("Winamax Poker - Tournament \"Qualif. Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 6 - HandId: #866707662845247492-25-1500771243 - Holdem no limit (12/50/100) - 2017/07/23 00:54:03 UTC")
        assertEquals(100.0, result)

        result = parser.parseBigBlind("Winamax Poker - Tournament \"Qualif . Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 9 - HandId: #866707662845247494-4-1500772425 - Holdem no limit (25/100/200) - 2017/07/23 01:13:45 UTC")
        assertEquals(200.0, result)

    }

    @Test
    fun testCurrency() {
        val parser: Parser = WinamaxParser()
        parser.setCurrency(Currency.EURO)
        var result: Currency = parser.parseCurrency("Winamax Poker - Tournament \"Qualif. Ticket 5€\" buyIn: 0.45€ + 0.05€ level: 6 - HandId: #866707662845247492-25-1500771243 - Holdem no limit (12/50/100) - 2017/07/23 00:54:03 UTC")
        assertEquals(Currency.EURO, result)

        result = parser.parseCurrency("")
        assertEquals(Currency.EURO, result)

    }

    @Test
    fun testGameIdCardroom() {
        val parser: Parser = WinamaxParser()
        parser.setCurrency(Currency.EURO)
        val result: String = parser.parseGameIdCardroom("20170722_Qualif. Ticket 5€(201796103)_real_holdem_no-limit.txt")
        assertEquals("201796103", result)
    }

    @Test
    fun testNumberOfPalyersByTable() {
        val parser: Parser = WinamaxParser()
        parser.setCurrency(Currency.EURO)
        var result: Int = parser.parseNumberOfPlayerByTable("Table: 'Qualif. Ticket 5€(201796103)#005' 9-max (real money) Seat #9 is the button")
        assertEquals(9, result)

        result = parser.parseNumberOfPlayerByTable(" Table: 'Nice 05' 5-max (real money) Seat #2 is the button")
        assertEquals(5, result)

    }

}