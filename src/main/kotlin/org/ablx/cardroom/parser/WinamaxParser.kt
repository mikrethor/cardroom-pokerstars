package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.Hand
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.enumeration.*
import org.ablx.cardroom.commons.enumeration.Currency
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import jdk.nashorn.tools.ShellFunctions.input
import jdk.nashorn.tools.ShellFunctions.input
import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER
import org.ablx.cardroom.commons.data.HandAction


class WinamaxParser(override val cardroom: Cardroom, override val filePath: String) : Parser, CardroomParser() {


    override var operator: Operator = Operator.WINAMAX
    protected val ANTE_BLIND = "*** ANTE/BLINDS ***"
    protected val PRE_FLOP = "*** PRE-FLOP ***"
    protected val FLOP = "*** FLOP ***"
    protected val TURN = "*** TURN ***"
    protected val RIVER = "*** RIVER ***"
    protected val SHOW_DOWN = "*** SHOW DOWN ***"
    protected val SUMMARY = "*** SUMMARY ***"
    protected val NEW_HAND = "Winamax Poker"
    protected val SEAT = "Seat"
    protected val BOARD = "Board"
    protected val ENCODING = "UTF8"
    protected val TABLE = "Table: "
    protected val NO_RAKE = "No rake"
    protected val RAKE = "Rake "
    protected val TOTAL_POT = "Total pot "
    protected val DEALT_TO = "Dealt to "
    protected val DEALT = "Dealt"
    protected val DENIES = "denies"
    protected val POSTS = "posts"
    protected val SMALL = "small"
    protected val BUY_IN = "buyIn: "
    protected val LEVEL = "level:"
    protected val LEVEL_SPACE = LEVEL + SPACE
    protected val PLUS_SPACE = PLUS + SPACE
    protected val IS_THE_BUTTON = "is the button"
    protected val HANDID_HASHTAG = "HandId: #"
    protected val MINUS_HANDID = " - HandId:"
    protected val MAX = "max"


    override fun fileToMap(): Map<String, StringBuffer> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCards(cards: String): Array<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGameTypeFromFilename(fileName: String): GameType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPlayerBlind(blindDealt: Array<String>): String {
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

    override fun parse(): MutableMap<String, Hand>? {
        val content = readHandFile()
        var map: MutableMap<String, Hand>? = HashMap()
        var hand: Hand = Hand("")
        var currentLine = ""
        content.reader().useLines {
            var iter = it.iterator()
            while (iter.hasNext()) {

                currentLine = iter.next()
                //Check each New Hand Line
                if (currentLine.startsWith(NEW_HAND)) {


                    hand = Hand(parseHandId(currentLine))

                    hand.players = HashMap<Int, Player>()

                    hand.bigBlind = parseBigBlind(currentLine)
                    hand.smallBlind = parseSmallBlind(currentLine)
                    hand.handDate = parseHandDate(currentLine)
                    hand.currency = parseCurrency(currentLine)
                    hand.level = parseLevel(currentLine)
                    hand.fee = parseFee(currentLine)
                    hand.buyIn = parseBuyIn(currentLine)

                    currentLine = iter.next()

                }

                if (currentLine.startsWith(TABLE)) {
                    hand.numberOfPlayerByTable = parseNumberOfPlayerByTable(currentLine)
                    var buttonSeat = parseButtonSeat(currentLine)
                    var tableId = parseTableId(currentLine)


                }

                if (currentLine.startsWith(SEAT)) {
                    while (iter.hasNext()) {
                        parsePlayerSeat(currentLine)
                        currentLine = iter.next()
                        if (!currentLine.startsWith(SEAT)) {
                            break;
                        }
                    }

                }


            }


        }

        return map

    }


    override fun parseBigBlind(line: String): Double {
        var startPosition = line.indexOf(PARENTHESEGAUCHE) + 1
        var endPosition = line.indexOf(PARENTHESEDROITE)
        val blinds = line.substring(startPosition, endPosition)

        if (blinds.indexOf(SLASH) != blinds.lastIndexOf(SLASH)) {
            startPosition = blinds.lastIndexOf(SLASH) + 1
            endPosition = blinds.length
        } else {
            startPosition = blinds.indexOf(SLASH) + 1
            endPosition = blinds.length
        }

        return blinds.substring(startPosition, endPosition).toDouble()
    }

    override fun parseButtonSeat(line: String): Int? {
        val startPosition = line.lastIndexOf(HASHTAG) + 1
        val endPosition = line.indexOf(IS_THE_BUTTON) - 1
        return Integer.parseInt(line.substring(startPosition, endPosition))
    }

    override fun parseBuyIn(line: String): Double {
        var startPosition = line.indexOf(BUY_IN) + BUY_IN.length
        var endPosition = line.indexOf(LEVEL)

        var buyIn = line.substring(startPosition, endPosition)

        // Cas 1: 0,90eee+ 0,10e
        // Cas 2: Ticket only
        if (buyIn.contains(PLUS)) {
            startPosition = 0
            if (buyIn.contains(money.symbol)) {
                endPosition = buyIn.indexOf(money.symbol)
            } else {
                endPosition = buyIn.indexOf(PLUS)
            }
            buyIn = buyIn.substring(startPosition, endPosition)
            buyIn = buyIn.replace(VIRGULE, POINT)
            buyIn = buyIn.replace(money.symbol, VIDE)
            return java.lang.Double.parseDouble(buyIn)
        }
        return 0.0
    }

    override fun parseCurrency(line: String): org.ablx.cardroom.commons.enumeration.Currency {
        //The Winamax currency is only euro
        return Currency.EURO
    }


    override fun parseFee(line: String): Double {
        val tab = line.split(" - ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var startPosition = tab[1].indexOf(BUY_IN) + BUY_IN.length
        var endPosition = tab[1].indexOf(LEVEL)

        var fee = tab[1].substring(startPosition, endPosition)
        // Cas 1: 0,90eee+ 0,10e
        // Cas 2: Ticket only
        if (fee.contains(PLUS)) {
            startPosition = fee.indexOf(PLUS_SPACE) + PLUS_SPACE.length
            if (fee.contains(money.symbol)) {
                endPosition = fee.lastIndexOf(money.symbol)

            } else {
                endPosition = fee.length

            }
            fee = fee.substring(startPosition, endPosition)
            fee = fee.replace(VIRGULE, POINT)
            return java.lang.Double.parseDouble(fee)
        }
        return 0.0
    }

    override fun parseGameIdCardroom(line: String): String {
        val startPosition = line.indexOf(PARENTHESEGAUCHE) + 1
        val endPosition = line.indexOf(PARENTHESEDROITE, startPosition)
        return line.substring(startPosition, endPosition)
    }

    override fun parseHandDate(line: String): Date {
        val startPosition = line.lastIndexOf(DASH) + 2
        val endPosition = line.lastIndexOf(SPACE)

        try {
            //TODO Externalize date handling
            val sdf: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val date: Date = sdf.parse(line.substring(startPosition, endPosition))
            return date
        } catch (e: ParseException) {

            return Date()
        }

    }

    override fun parseHandId(line: String): String {
        val startPosition = line.indexOf(HANDID_HASHTAG) + HANDID_HASHTAG.length
        val endPosition = line.indexOf(DASH, line.indexOf(DASH,
                line.indexOf(HANDID_HASHTAG) + HANDID_HASHTAG.length) + 1)
        return line.substring(startPosition, endPosition)
    }

    override fun parseLevel(line: String): Int {
        val startPosition = line.indexOf(LEVEL_SPACE) + LEVEL_SPACE.length
        val endPosition = line.indexOf(MINUS_HANDID)
        return Integer.parseInt(line.substring(startPosition, endPosition))
    }

    override fun parseNewHandLine(line: String, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseNumberOfPlayerByTable(line: String): Int {
        val startPosition = line.lastIndexOf(APOSTROPHE) + 2
        val endPosition = line.lastIndexOf(MAX) - 1
        return line.substring(startPosition, endPosition).toInt()
    }

    override fun parsePlayerAccount(line: String): String {
        val startPosition: Int = DEALT_TO.length
        val endPosition: Int = line.lastIndexOf(CROCHETOUVRANT) - 1
        return line.substring(startPosition, endPosition)
    }

    override fun parsePlayerSeat(line: String): Player {
        val space = line.indexOf(SPACE)
        val deuxpoints = line.indexOf(DEUXPOINTS)
        val parenthesegauche = line.indexOf(PARENTHESEGAUCHE)
        val parenthesedroite = line.indexOf(PARENTHESEDROITE)

        val seat = line.substring(space + 1, deuxpoints)
        val playerName = line.substring(deuxpoints + 2,
                parenthesegauche - 1)
        var stack = line.substring(parenthesegauche + 1, parenthesedroite)
        stack = stack.replace(money.symbol, VIDE)
        val player = Player(null, playerName, cardroom)


        player.seat = Integer.parseInt(seat)
        player.on = true
        player.stack = java.lang.Double.parseDouble(stack)
        return player
    }

    override fun parseRake(line: String): Double? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun parseSmallBlind(line: String): Double {
        var startPosition = line.indexOf(PARENTHESEGAUCHE) + 1
        var endPosition = line.indexOf(PARENTHESEDROITE)
        val blinds = line.substring(startPosition, endPosition)

        if (blinds.indexOf(SLASH) != blinds.lastIndexOf(SLASH)) {
            startPosition = blinds.indexOf(SLASH) + 1
            endPosition = blinds.lastIndexOf(SLASH)
        } else {
            startPosition = 0
            endPosition = blinds.indexOf(SLASH)
        }


        return blinds.substring(startPosition, endPosition).toDouble()

    }

    override fun parseTableId(line: String): String {
        val startPosition = line.indexOf(HASHTAG) + 1
        val endPosition = line.lastIndexOf(APOSTROPHE)

        return line.substring(startPosition, endPosition)
    }


    override fun parseTotalPot(line: String): Double? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parsing(): Map<String, Hand> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun readCards(line: String): Array<Card> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun readHandFile(): String {
        val encoded: ByteArray = Files.readAllBytes(Paths.get(filePath))
        return String(encoded, Charsets.UTF_8)
    }

    override fun readAction(line: String, players: Map<String, Player>): HandAction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun stringToECards(card: String): Card {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun textToHandDto(text: StringBuffer): Hand {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toCards(cards: Array<String>): Array<Card> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseAntesAndBlinds(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseDealer(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseSeatLine(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseTableLine(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readActionsByPhase(currentLine: String, iterator: Iterator<String>, hand: Hand, phase: String, nextPhases: Array<String>, actions: MutableList<HandAction>): String {
        var nextL = currentLine

        if (nextL.startsWith(phase)) {
            // Demarrage de la lecture de la phase
            while (iterator.hasNext()) {
                nextL = iterator.next()
                // Check si on tombe sur la prochaine phase
                if (startsWith(nextL, nextPhases)) {
                    break
                } else {
                    // Ajout des actions ela phase dans le HanDTO
                    val action = this.readAction(nextL,
                            hand.playersByName)

                    var round: Round? = null

                    when (phase) {
                        PRE_FLOP -> round = Round.PRE_FLOP
                        FLOP -> round = Round.FLOP
                        TURN -> round = Round.TURN
                        RIVER -> round = Round.RIVER
                        SHOW_DOWN -> round = Round.SHOWDOWN
                        else -> round = null
                    }
                    action.phase = round
                    actions.add(action)
                }
            }

        }
        // Retourne le nextLine pour pouvoir continuer l'itteration du scanner
        // comme il faut.
        return nextL
    }

    override fun readFlop(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
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
}