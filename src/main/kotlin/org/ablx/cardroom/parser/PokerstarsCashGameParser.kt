package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom


class PokerstarsCashGameParser(override val cardroom: Cardroom, override val filePath: String) : Parser, PokerstarsParser(cardroom, filePath) {

    override fun parseTableId(line: String): String {
        val startPosition = line.indexOf(APOSTROPHE) + 1
        val endPosition = line.lastIndexOf(APOSTROPHE)
        return line.substring(startPosition, endPosition)
    }

    override fun parseBuyIn(line: String): Double {
        return 0.0
    }

    override fun parseFee(line: String): Double {
        return 0.0
    }

    override fun parseSmallBlind(line: String): Double {
        val startPosition = line.indexOf(money.symbol) + 1
        val endPosition = line.indexOf(SLASH)
        var smallBlind = line.substring(startPosition, endPosition)
        smallBlind = smallBlind.replace(money.symbol, EMPTY)
        return smallBlind.toDouble()
    }

    override fun parseBigBlind(line: String): Double {
        val startPosition = line.indexOf(SLASH) + 2
        val endPosition = line.indexOf(money.shortName)
        var bigBlind = line.substring(startPosition, endPosition)
        bigBlind = bigBlind.replace(money.symbol, EMPTY)
        return bigBlind.toDouble()
    }
}