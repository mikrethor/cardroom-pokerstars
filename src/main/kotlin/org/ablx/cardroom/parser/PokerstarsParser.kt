package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.Hand
import org.ablx.cardroom.commons.data.HandAction
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.utils.RomanNumeralUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER
import jdk.nashorn.tools.ShellFunctions.input
import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER
import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER
import jdk.nashorn.tools.ShellFunctions.input
import org.ablx.cardroom.commons.enumeration.*
import org.ablx.cardroom.commons.enumeration.Currency
import jdk.nashorn.tools.ShellFunctions.input
import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER
import kotlin.collections.HashMap
import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER


class PokerstarsParser(override val cardroom: Cardroom, override val filePath: String) : Parser, CardroomParser() {

    protected val DEALT_TO = "Dealt to "
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

    override var operator: Operator = Operator.POKERSTARS


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
        val content = readHandFile()
        val map: MutableMap<String, Hand> = HashMap()
        var hand: Hand = Hand("")

        hand.actions = ArrayList<HandAction>()
        //TODO put in the right phase

        hand.preflopActions = ArrayList<HandAction>()
        hand.flopActions = ArrayList<HandAction>()
        hand.turnActions = ArrayList<HandAction>()
        hand.riverActions = ArrayList<HandAction>()
        hand.showdownActions = ArrayList<HandAction>()
        hand.players = HashMap<Int, Player>()
        hand.playersSeatByName = HashMap<String, Int>()
        var currentLine = ""

        content.reader().useLines {
            val iter = it.iterator()
            while (iter.hasNext()) {

                if (currentLine.startsWith(NEW_HAND)) {

                    currentLine = parseNewHandLine(currentLine, NEW_HAND, arrayOf(EMPTY), hand)
                }
                currentLine = iter.next()

                currentLine = parseTableLine(currentLine, iter, TABLE, arrayOf(EMPTY), hand)
                currentLine = iter.next()

                currentLine = parseSeatLine(currentLine, iter, SEAT, arrayOf<String>(HOLE_CARDS), hand)

                // Renommer cette methode
                currentLine = parseDealer(currentLine, iter, HOLE_CARDS, arrayOf<String>(FLOP, SUMMARY), hand)

                // Lecture des actions du coup
                currentLine = readPreflop(currentLine, iter, hand)

                currentLine = readFlop(currentLine, iter, hand)

                currentLine = readTurn(currentLine, iter, hand)

                currentLine = readRiver(currentLine, iter, hand)

                currentLine = readShowdown(currentLine, iter, hand)

                currentLine = readSummary(currentLine, iter, SUMMARY, arrayOf<String>(NEW_HAND), hand)

                hand.cardroom = cardroom
                map.put(hand.cardroomHandId, hand)
            }
            return map
        }
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


    override fun parseButtonSeat(line: String): Int {
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
        try {
            return Arrays.stream(Currency.values()).filter { currency -> line.indexOf(currency.symbol) > 0 }.findFirst()
                    .get()
        } catch (e: NoSuchElementException) {
            return Currency.PLAY_MONEY
        }

    }

    override fun parseDealer(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {
        var nextL = currentLine
        if (nextL.startsWith(HOLE_CARDS)) {

            while (iterator.hasNext()) {

                nextL = iterator.next()
                if (nextL.startsWith("Dealt")) {

                    val crochetouvrant = nextL.lastIndexOf(OPENNING_SQUARE_BRACKET)

                    val name = nextL.substring("Dealt to ".length, crochetouvrant - 1)

                    val cards = readCards(nextL)
                    //hand.getMapPlayerCards().put(joueur, cartes)
                    hand.accountPlayer = hand.playersByName.get(name)

                    nextL = iterator.next()
                }
                if (nextL.startsWith(FLOP) || nextL.startsWith(SUMMARY)) {
                    break
                } else {

                    val action = this.readAction(nextL, hand.playersByName)

                    if (action != null) {
                        action!!.round = Round.PRE_FLOP
                        hand.preflopActions.add(action)
                    }

                }
            }
        }
        return nextL
    }

    override fun parseFee(line: String): Double {
        val tab = line.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val buyIn = tab[5]
        val startPosition: Int
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

    override fun parseGameIdCardroom(fileName: String): String {


        val pattern = Pattern.compile("HH[0-9]{8} T[0-9]{8}")
        val matcher = pattern.matcher(fileName)

        var temp: String = fileName

        if (matcher.find()) {
            val startPosition = temp.indexOf("T") + 1
            var endPosition = temp.indexOf(" ", temp.indexOf(" ") + 1)
            temp = temp.substring(startPosition, endPosition)
            return temp
        }
        return EMPTY
    }


    override fun parseHandDate(line: String): Date {

        val startPosition = line.lastIndexOf(DASH) + 1
        var endPosition = line.lastIndexOf(SPACE)

        var currentLine = line.substring(startPosition, endPosition)
        if (currentLine.lastIndexOf(CLOSING_SQUARE_BRACKET) > 0) {
            endPosition = currentLine.lastIndexOf(OPENNING_SQUARE_BRACKET)
            currentLine = currentLine.substring(0, endPosition)
        }
        val sdf: DateFormat = SimpleDateFormat(" yyyy/MM/dd HH:mm:ss")
        val date: Date = sdf.parse(currentLine)
        return date


    }

    override fun parseHandId(line: String): String {
        val startPosition = line.indexOf("Hand #") + "Hand #".length
        val endPosition = line.indexOf(COLON)
        return line.substring(startPosition, endPosition)
    }

    override fun parseLevel(line: String): Int {
        val tab = line.split(SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var level: Int
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
        var nextL = currentLine
        if (nextL.startsWith(SEAT)) {

            while (iterator.hasNext()) {
                if (nextL.startsWith(SEAT)) {


                    val playerInGame = parsePlayerSeat(nextL)
                    hand.addPlayer(playerInGame)


                    if (hand.buttonSeat == playerInGame.seat) {

                        hand.dealerPlayer = playerInGame
                    }
                    nextL = iterator.next()
                }
                if (nextL.contains("posts small blind")) {
                    val smallBlindTab = nextL.split(SPACE.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val smallBlindPlayer = this.getPlayerBlind(smallBlindTab).replace(COLON, EMPTY)
                    val smallBlind = smallBlindTab[smallBlindTab.size - 1]

                    hand.smallBlindPlayer = hand.playersByName.get(smallBlindPlayer)
                    nextL = iterator.next()
                }

                if (nextL.contains("posts big blind")) {
                    val bigBlindTab = nextL.split(SPACE.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val bigBlindPlayer = this.getPlayerBlind(bigBlindTab).replace(COLON, EMPTY)
                    val bigBlind = bigBlindTab[bigBlindTab.size - 1]
                    hand.bigBlindPlayer = hand.playersByName.get(bigBlindPlayer)

                    nextL = iterator.next()
                }
                if (nextL.startsWith(HOLE_CARDS)) {

                    break
                }
            }
        }
        return nextL
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

        val nextL = currentLine

        if (nextL.startsWith(TABLE)) {

            val numeroTable = parseTableId(nextL)

            val nombreJoueur = parseNumberOfPlayerByTable(nextL)
            val buttonSeat = parseButtonSeat(nextL)
            hand.buttonSeat = buttonSeat



            hand.numberOfPlayerByTable = nombreJoueur
            hand.cardroomTableId = numeroTable

        }
        return nextL
    }


    override fun parseTotalPot(line: String): Double {
        val tabTotalPot = line.split(SPACE)
        var totalPot = tabTotalPot[2].replace(money.symbol, EMPTY)
        totalPot = totalPot.replace(money.symbol, EMPTY)

        return java.lang.Double.parseDouble(totalPot)
    }

    override fun parsing(): Map<String, Hand> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readAction(line: String, players: Map<String, Player>): HandAction? {

        val tab = line.split(SPACE)
        var action = ""
        var playerName = ""
        var between = ""
        var amount = "0"
        var hand: Array<Card?>? = null

        for (i in tab.indices) {
            if (Action.FOLDS.action.equals(tab[i]) || Action.CALLS.action.equals(tab[i])
                    || Action.RAISES.action.equals(tab[i]) || Action.CHECKS.action.equals(tab[i])
                    || Action.COLLECTED.action.equals(tab[i]) || Action.BETS.action.equals(tab[i])
                    || Action.SHOWS.action.equals(tab[i]) || "has" == tab[i]) {
                playerName = ""

                action = tab[i]

                if (Action.CALLS.action.equals(tab[i]) || Action.RAISES.action.equals(tab[i])
                        || Action.COLLECTED.action.equals(tab[i]) || Action.BETS.action.equals(tab[i])) {
                    amount = tab[i + 1]
                    amount = amount.replace(money.symbol, EMPTY)
                }

                for (j in 0..i - 1) {
                    if (j == 0) {
                        between = ""
                    } else {
                        between = SPACE
                    }
                    playerName = playerName + between + tab[j]

                }
                if (Action.SHOWS.action.equals(tab[i])) {
                    hand = readCards(line)
                }

            }
        }
        playerName = playerName.replace(COLON, EMPTY)



        if ("has" == action || "" == action) {
            return null
        } else {


            return HandAction(players[playerName], Action.valueOf(action.toUpperCase()),
                    java.lang.Double.parseDouble(amount), hand)
        }
    }

    override fun readActionsByPhase(currentLine: String, iterator: Iterator<String>, hand: Hand, phase: String, nextPhases: Array<String>, actions: MutableList<HandAction>?): String {
        var nextL = currentLine

        if (nextL.startsWith(phase)) {

            while (iterator.hasNext()) {
                nextL = iterator.next()
                if (startsWith(nextL, nextPhases)) {
                    break
                } else {
                    var round: Round?

                    when (phase) {
                        HOLE_CARDS -> round = Round.PRE_FLOP
                        FLOP -> {
                            round = Round.FLOP
                            hand.flop = readCards(currentLine)
                        }
                        TURN -> {
                            round = Round.TURN
                            hand.turn = readCards(currentLine)!![0]
                        }
                        RIVER -> {
                            round = Round.RIVER
                            hand.river = readCards(currentLine)!![0]
                        }
                        SHOW_DOWN -> round = Round.SHOWDOWN
                        else -> round = null
                    }
                    val action = this.readAction(nextL, hand.playersByName)

                    if (action != null) {
                        action.round = round
                        actions!!.add(action)
                    }
                }
            }
        }
        return nextL
    }

    override fun readFlop(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        return readActionsByPhase(currentLine, iterator, hand, FLOP, arrayOf(TURN, SUMMARY), hand.flopActions)
    }

    override fun readHandFile(): String {
        val encoded: ByteArray = Files.readAllBytes(Paths.get(filePath))
        return String(encoded, Charsets.UTF_8)
    }

    override fun readPreflop(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        return currentLine
    }

    override fun readRiver(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        return readActionsByPhase(currentLine, iterator, hand, RIVER, arrayOf(SHOW_DOWN, SUMMARY), hand.riverActions)
    }

    override fun readShowdown(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        return readActionsByPhase(currentLine, iterator, hand, SHOW_DOWN, arrayOf(SUMMARY), hand.showdownActions)
    }

    override fun readSummary(currentLine: String, iterator: Iterator<String>, phase: String, nextPhases: Array<String>, hand: Hand): String {

        var line = currentLine
        if (line.startsWith(phase)) {

            var player: Player?
            while (iterator.hasNext()) {

                // Total pot 180 | No rake
                if (line.startsWith("Total pot ")) {

                    // Total pot �3.45 Main pot �2.38. Side pot �0.86. |
                    // Rake �0.21
                    hand.totalPot = parseTotalPot(line)
                    hand.rake = parseRake(line)
                }

                if (line.startsWith(BOARD)) {

                    this.readCards(line)

                }

                if (startsWith(line, nextPhases)) {
                    break
                } else {
                    line = iterator.next()
                }

                if (line.startsWith(SEAT)) {
                    player = parsePlayerSummary(line)
                    if (player!!.cards != null) {
                        //  hand.getMapPlayerCards().put(player!!.name, player!!.cards)
                    }


                }
            }
            hand.actions.addAll(hand.preflopActions)
            hand.actions.addAll(hand.flopActions)
            hand.actions.addAll(hand.turnActions)
            hand.actions.addAll(hand.riverActions)
            hand.actions.addAll(hand.showdownActions)
            // game.addHand(hand);
        }
        return line
    }

    override fun readTurn(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        return readActionsByPhase(currentLine, iterator, hand, TURN, arrayOf(RIVER, SUMMARY), hand.turnActions)
    }

    override fun textToHand(text: StringBuffer): Hand {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun parsePlayerSummary(line: String): Player {
        val espace = line.indexOf(SPACE)
        val deuxpoints = line.indexOf(COLON)

        val seat = line.substring(espace + 1, deuxpoints)
        val playerName = line.substring(deuxpoints + 2, line.indexOf(SPACE, deuxpoints + 2))
        var cards: Array<Card?>?
        val player = Player(null, playerName, cardroom)
        if (line.indexOf(OPENNING_SQUARE_BRACKET) > 0) {
            cards = readCards(line)
            player.cards = cards
        }
        player.seat = Integer.parseInt(seat)



        return player
    }


}