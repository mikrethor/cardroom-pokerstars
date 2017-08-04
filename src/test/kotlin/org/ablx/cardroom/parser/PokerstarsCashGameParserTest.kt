package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.enumeration.Currency
import org.ablx.cardroom.commons.enumeration.Domain
import org.ablx.cardroom.commons.enumeration.GameType
import org.ablx.cardroom.commons.enumeration.Operator
import org.junit.Test
import java.io.File
import java.util.*
import kotlin.test.assertEquals


class PokerstarsCashGameParserTest :PokerstarsParserTest(){

    override fun createParser(): Parser {
        val cardroom = Cardroom(1, Operator.POKERSTARS, Domain.COM, "")

        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource("HandCashGameTestFile.txt")!!.file)

        val parser: Parser = PokerstarsCashGameParser(cardroom, file.absolutePath)
        parser.setCurrency(Currency.EURO)
        return parser
    }


}