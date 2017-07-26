package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.enumeration.Action
import org.ablx.cardroom.commons.enumeration.Currency
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
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

}