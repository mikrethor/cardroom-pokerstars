package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.Hand
import org.ablx.cardroom.commons.data.HandAction
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.enumeration.Currency
import org.ablx.cardroom.commons.enumeration.GameType
import org.ablx.cardroom.commons.enumeration.Operator
import org.ablx.cardroom.commons.utils.RomanNumeralUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class PokerstarsParser(override val cardroom: Cardroom, override val filePath: String) : Parser, CardroomParser() {

    protected val DEALT_TO = "Dealt to "

    override fun fileToMap(): Map<String, StringBuffer> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGameTypeFromFilename(fileName: String): GameType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTournamentId(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isHandFile(filePath: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isUselesLine(line: String): Boolean? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nextLine(scanner: Scanner): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parse(): MutableMap<String, Hand> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseAntesAndBlinds(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        return ""
    }

    override fun parseBigBlind(line: String): Double {
        val startPosition = line.indexOf(SLASH) + 1
        val endPosition = line.indexOf(RIGHT_PARENTHESIS)
        val bigBlind = line.substring(startPosition, endPosition)
        return java.lang.Double.parseDouble(bigBlind)
    }


    override fun parseButtonSeat(line: String): Int? {
        val startPosition = line.indexOf(HASHTAG) + 1
        val endPosition = line.indexOf("is the button") - 1
        return Integer.parseInt(line.substring(startPosition, endPosition))
    }

    override fun parseBuyIn(line: String): Double {
        val tab = line.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val buyIn = tab[5]
        var startPosition: Int
        val endPosition = buyIn.indexOf(PLUS)
        if (buyIn.contains(money.symbol)) {
            startPosition = buyIn.indexOf(money.symbol) + 1
        } else {
            startPosition = 0
        }

        if ("Freeroll" == buyIn) {
            return 0.0
        }
        if (buyIn.contains(PLUS)) {
            val realBuyIn = buyIn.substring(startPosition, endPosition)
            val fee = buyIn.substring(buyIn.lastIndexOf(PLUS) + 2, buyIn.length)

            return java.lang.Double.parseDouble(realBuyIn) + java.lang.Double.parseDouble(fee)
        }
        return java.lang.Double.parseDouble(buyIn)
    }

    override fun parseCurrency(line: String): Currency {
        // TODO si currency null error ?;
        return Arrays.stream(Currency.values()).filter { currency -> line.indexOf(currency.symbol) > 0 }.findFirst()
                .get()
    }

    override fun parseDealer(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseFee(line: String): Double {
        val tab = line.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val buyIn = tab[5]
        var startPosition: Int
        val endPosition = buyIn.indexOf(PLUS)

        if (buyIn.contains(money.symbol)) {
            startPosition = buyIn.indexOf(money.symbol) + 1
        } else {
            startPosition = 0
        }
        if ("Freeroll" == buyIn) {
            return 0.0
        }
        if (buyIn.contains(PLUS)) {
            val realBuyIn = buyIn.substring(startPosition, endPosition)
            val fee = buyIn.substring(buyIn.lastIndexOf(PLUS) + 2, buyIn.length)


            return java.lang.Double.parseDouble(fee)
        }
        return 0.0
    }

    override fun parseGameIdCardroom(line: String): String {
        val startPosition = line.lastIndexOf(HASHTAG) + 1
        val endPosition = line.indexOf(VIRGULE)
        return line.substring(startPosition, endPosition)
    }


    override fun parseHandDate(line: String): Date {

        val startPosition = line.lastIndexOf(DASH) + 1
        var endPosition = line.lastIndexOf(SPACE)
        if (line.lastIndexOf(CLOSING_SQUARE_BRACKET) > 0) {
            endPosition = line.lastIndexOf(OPENNING_SQUARE_BRACKET)
        }
        val sdf: DateFormat = SimpleDateFormat(" yyyy/MM/dd HH:mm:ss z")
        val date: Date = sdf.parse(line.substring(startPosition, endPosition))
        return date


    }

    override fun parseHandId(line: String): String {
        val startPosition = line.indexOf("Hand #") + "Hand #".length
        val endPosition = line.indexOf(COLON)
        return line.substring(startPosition, endPosition)
    }

    override fun parseLevel(line: String): Int {
        val tab = line.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var level:Int
        // Case heads-up with rebuy
        if ("Round" == tab[12]) {
            level = RomanNumeralUtils.toInt(tab[15])
        } else {
            level = RomanNumeralUtils.toInt(tab[12])
        }

        return level
    }


    override fun parseNewHandLine(line: String, phase: String, nextPhases: Array<String>, hand: Hand): String {
        val nextL = line
        if (nextL.startsWith(phase)) {

            val tab = nextL.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var gameType = GameType.CASH
            if (GameType.TOURNAMENT.type == tab[3]) {
                gameType = GameType.TOURNAMENT
            }

            if (GameType.TOURNAMENT == gameType) {
                hand.level = parseLevel(nextL)
                hand.handDate = parseHandDate(nextL)
            }

            hand.cardroomHandId = parseHandId(nextL)


            hand.handDate = parseHandDate(nextL)
            hand.bigBlind = parseBigBlind(nextL)
            hand.smallBlind = parseSmallBlind(nextL)
            hand.currency = parseCurrency(nextL)

        }
        return nextL
    }


    override fun parseNumberOfPlayerByTable(line: String): Int {
        val startPosition = line.lastIndexOf(APOSTROPHE) + 2
        val endPosition = line.indexOf("max") - 1
        return line.substring(startPosition, endPosition).toInt()
    }

    override fun parsePlayerAccount(line: String): String {
        val startPosition: Int = DEALT_TO.length
        val endPosition: Int = line.lastIndexOf(OPENNING_SQUARE_BRACKET) - 1
        return line.substring(startPosition, endPosition)
    }

    override fun parsePlayerSeat(line: String): Player {
        val espace = line.indexOf(SPACE)
        val deuxpoints = line.indexOf(COLON)
        val parenthesegauche = line.indexOf(LEFT_PARENTHESIS)
        val inchips = line.indexOf(" in chips)")

        val seat = line.substring(espace + 1, deuxpoints)
        val playerName = line.substring(deuxpoints + 2, parenthesegauche - 1)
        var stack = line.substring(parenthesegauche + 1, inchips)
        stack = stack.replace(money.symbol, EMPTY)

        val player = Player(0, playerName, cardroom)
        player.stack = java.lang.Double.parseDouble(stack)
        player.seat = Integer.parseInt(seat)
        player.on = true

        return player
    }

    override fun parseRake(line: String): Double {
        val startPosition = line.indexOf("| Rake") + "| Rake".length + 1
        val endPosition = line.length
        var rake = line.substring(startPosition, endPosition)
        rake = rake.replace(money.symbol, EMPTY)

        return java.lang.Double.parseDouble(rake)
    }

    override fun parseSeatLine(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseSmallBlind(line: String): Double {
        val startPosition = line.indexOf(LEFT_PARENTHESIS) + 1
        val endPosition = line.indexOf(SLASH)
        val smallBlind = line.substring(startPosition, endPosition)
        return java.lang.Double.parseDouble(smallBlind)
    }

    override fun parseTableId(line: String): String {
        val startPosition = line.indexOf(APOSTROPHE) + 1
        val endPosition = line.lastIndexOf(APOSTROPHE)
        val sousChaine = line.substring(startPosition, endPosition)
        val tab = sousChaine.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return tab[1]
    }

    override fun parseTableLine(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseTotalPot(line: String): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parsing(): Map<String, Hand> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readAction(line: String, players: Map<String, Player>): HandAction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readActionsByPhase(currentLine: String, iterator: Iterator<String>, hand: Hand, phase: String, nextPhases: Array<String>, actions: MutableList<HandAction>?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readFlop(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readHandFile(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readPreflop(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readRiver(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readShowdown(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readSummary(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readTurn(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun textToHand(text: StringBuffer): Hand {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override var operator: Operator = Operator.POKERSTARS
    val UTF8_BOM = "\uFEFF"
    val HOLE_CARDS = "*** HOLE CARDS ***"
    val FLOP = "*** FLOP ***"
    val TURN = "*** TURN ***"
    val RIVER = "*** RIVER ***"
    val SHOW_DOWN = "*** SHOW DOWN ***"
    val SUMMARY = "*** SUMMARY ***"
    val NEW_HAND = "PokerStars Hand"
    val SEAT = "Seat"
    val BOARD = "Board"
    val CASH_GAME = "CashGame"

    val TABLE = "Table"
    val ENCODING = "UTF8"


}