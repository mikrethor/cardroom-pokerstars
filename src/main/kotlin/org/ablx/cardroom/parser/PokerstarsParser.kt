package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Cardroom
import org.ablx.cardroom.commons.data.Hand
import org.ablx.cardroom.commons.data.HandAction
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.enumeration.*
import org.ablx.cardroom.commons.enumeration.Currency
import org.ablx.cardroom.commons.utils.RomanNumeralUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap


open class PokerstarsParser(override val cardroom: Cardroom, override val filePath: String) : Parser, CardroomParser() {

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

    override fun fileToMap(): Map<String, String> {

        var map = HashMap<String, String>()
        val parts = readHandFile().split(NEW_HAND)

        var index = 0
        for (s in parts) {
            index++
            if ("" != s && UTF8_BOM != s) {
                map.put(parseHandId(s), NEW_HAND + s)
            }
        }

        return map
    }

    override fun getGameTypeFromFilename(fileName: String): GameType {
        if (fileName.contains(" $DASH ")) {
            return GameType.CASH
        } else {
            return GameType.TOURNAMENT
        }
    }

    override fun isHandFile(filePath: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isUselessLine(line: String): Boolean {
        System.out.println(line)
        return (line.endsWith(PokerstarsActions.WILL_BE_ALLOWED_TO_PLAY_AFTER_THE_BUTTON.value)
                || line.contains(PokerstarsActions.POSTS_SMALL_ET_BIG_BLINDS.value)
                || line.contains(PokerstarsActions.POSTS_THE_ANTE.value) || line.endsWith(PokerstarsActions.SITS_OUT.value)
                || line.endsWith(PokerstarsActions.LEAVES_THE_TABLE.value)
                || line.endsWith(PokerstarsActions.IS_SITTING_OUT.value) || line.endsWith(PokerstarsActions.IS_DISCONNECTED.value)
                || line.endsWith(PokerstarsActions.IS_CONNECTED.value) || line.contains(PokerstarsActions.SAID.value)
                || line.endsWith(PokerstarsActions.HAS_TIMED_OUT.value)
                || line.contains(PokerstarsActions.JOINS_THE_TABLE_AT_SEAT.value)
                || line.contains(PokerstarsActions.UNCALLED_BET.value) || line.endsWith(PokerstarsActions.HAS_RETURNED.value)
                || line.contains(PokerstarsActions.DOESNT_SHOW_HAND.value)
                || line.endsWith(PokerstarsActions.WAS_REMOVED_FROM_THE_TABLE_FOR_FAILING_TO_POST.value)
                || line.endsWith(PokerstarsActions.MUCKS_HAND.value)
                || line.contains(PokerstarsActions.FINISHED_THE_TOURNAMENT_IN.value)) && (!line.startsWith("Seat"))
    }

    override fun parse(): MutableMap<String, Hand> {
        val mapHands: MutableMap<String, Hand> = HashMap()
        val mapFilePart: Map<String, String> = fileToMap()

        for (key in mapFilePart.keys) {
            mapHands.put(key, textToHand(mapFilePart[key]!!))
        }
        return mapHands
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

                nextL = getNextUseFulLine(iterator)
                if (nextL.startsWith("Dealt")) {

                    val crochetouvrant = nextL.lastIndexOf(OPENNING_SQUARE_BRACKET)

                    val name = nextL.substring("Dealt to ".length, crochetouvrant - 1)

                    val cards = readCards(nextL)
                    //hand.getMapPlayerCards().put(joueur, cartes)
                    hand.accountPlayer = hand.playersByName.get(name)

                    nextL = getNextUseFulLine(iterator)
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
        hand.actions = ArrayList<HandAction>()
        hand.players = HashMap<Int, Player>()
        hand.playersSeatByName = HashMap<String, Int>()
        hand.preflopActions = ArrayList<HandAction>()
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
        val space = line.indexOf(SPACE)
        val colon = line.indexOf(COLON)
        val leftParenthesis = line.indexOf(LEFT_PARENTHESIS)
        val inchips = line.indexOf(" in chips)")

        val seat = line.substring(space + 1, colon)
        val playerName = line.substring(colon + 2, leftParenthesis - 1)
        var stack = line.substring(leftParenthesis + 1, inchips)
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
        var curLine = currentLine
        if (curLine.startsWith(SEAT)) {

            while (iterator.hasNext()) {
                if (curLine.startsWith(SEAT)) {
                    val playerInGame = parsePlayerSeat(curLine)
                    hand.addPlayer(playerInGame)
                    if (hand.buttonSeat == playerInGame.seat) {

                        hand.dealerPlayer = playerInGame
                    }
                    curLine = getNextUseFulLine(iterator)
                }
                if (curLine.contains("posts small blind")) {
                    val smallBlindTab = curLine.split(SPACE.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val smallBlindPlayer = this.getPlayerBlind(smallBlindTab).replace(COLON, EMPTY)
                    val smallBlind = smallBlindTab[smallBlindTab.size - 1]

                    hand.smallBlindPlayer = hand.playersByName.get(smallBlindPlayer)
                    curLine = getNextUseFulLine(iterator)
                }

                if (curLine.contains("posts big blind")) {
                    val bigBlindTab = curLine.split(SPACE.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val bigBlindPlayer = this.getPlayerBlind(bigBlindTab).replace(COLON, EMPTY)
                    val bigBlind = bigBlindTab[bigBlindTab.size - 1]
                    hand.bigBlindPlayer = hand.playersByName.get(bigBlindPlayer)

                    curLine = getNextUseFulLine(iterator)
                }
                if (curLine.startsWith(HOLE_CARDS)) {

                    break
                }
            }
        }
        return curLine
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

        val curLine = currentLine

        if (curLine.startsWith(TABLE)) {
            val numeroTable = parseTableId(curLine)

            val nombreJoueur = parseNumberOfPlayerByTable(curLine)
            val buttonSeat = parseButtonSeat(curLine)
            hand.buttonSeat = buttonSeat

            hand.numberOfPlayerByTable = nombreJoueur
            hand.cardroomTableId = numeroTable

        }
        return curLine
    }

    override fun parseTotalPot(line: String): Double {
        val tabTotalPot = line.split(SPACE)
        var totalPot = tabTotalPot[2].replace(money.symbol, EMPTY)
        totalPot = totalPot.replace(money.symbol, EMPTY)

        return java.lang.Double.parseDouble(totalPot)
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
                    amount.toDouble(), hand)
        }
    }

    override fun readActionsByPhase(currentLine: String, iterator: Iterator<String>, hand: Hand, phase: String, nextPhases: Array<String>, actions: MutableList<HandAction>?): String {
        var curLine = currentLine

        if (curLine.startsWith(phase)) {

            while (iterator.hasNext()) {
                curLine = getNextUseFulLine(iterator)
                if (startsWith(curLine, nextPhases)) {
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
                    val action = this.readAction(curLine, hand.playersByName)

                    if (action != null) {
                        action.round = round
                        actions!!.add(action)
                    }
                }
            }
        }
        return curLine
    }

    override fun readFlop(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        hand.flopActions = ArrayList<HandAction>()
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
        hand.riverActions = ArrayList<HandAction>()
        return readActionsByPhase(currentLine, iterator, hand, RIVER, arrayOf(SHOW_DOWN, SUMMARY), hand.riverActions)
    }

    override fun readShowdown(currentLine: String, iterator: Iterator<String>, hand: Hand): String {
        hand.showdownActions = ArrayList<HandAction>()
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
                    line = getNextUseFulLine(iterator)
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
        hand.turnActions = ArrayList<HandAction>()
        return readActionsByPhase(currentLine, iterator, hand, TURN, arrayOf(RIVER, SUMMARY), hand.turnActions)
    }


    override fun textToHand(text: String): Hand {
        var currentLine: String = ""
        var firstIteration = true
        val iter = text.lines().asIterable().iterator()
        var hand: Hand = Hand("")

        while (iter.hasNext()) {
            currentLine = getNextUseFulLine(iter)
            if (currentLine.startsWith(NEW_HAND)) {
                hand = Hand("")
                parseNewHandLine(currentLine, NEW_HAND, arrayOf(EMPTY), hand)
            }
            currentLine = getNextUseFulLine(iter)
            parseTableLine(currentLine, iter, TABLE, arrayOf(EMPTY), hand)
            currentLine = getNextUseFulLine(iter)
            currentLine = parseSeatLine(currentLine, iter, SEAT, arrayOf<String>(HOLE_CARDS), hand)

            // Renommer cette methode
            currentLine = parseDealer(currentLine, iter, HOLE_CARDS, arrayOf<String>(FLOP, SUMMARY), hand)

            // Lecture des actions du coup
            currentLine = readPreflop(currentLine, iter, hand)

            currentLine = readFlop(currentLine, iter, hand)

            currentLine = readTurn(currentLine, iter, hand)

            currentLine = readRiver(currentLine, iter, hand)

            currentLine = readShowdown(currentLine, iter, hand)

            readSummary(currentLine, iter, SUMMARY, arrayOf(NEW_HAND), hand)

            hand.cardroom = cardroom

        }

        return hand
    }

    fun parsePlayerSummary(line: String): Player {
        val space = line.indexOf(SPACE)
        val colon = line.indexOf(COLON)

        val seat = line.substring(space + 1, colon)
        val playerName = line.substring(colon + 2, line.indexOf(SPACE, colon + 2))
        var cards: Array<Card?>?
        val player = Player(null, playerName, cardroom)
        if (line.indexOf(OPENNING_SQUARE_BRACKET) > 0) {
            cards = readCards(line)
            player.cards = cards
        }
        player.seat = Integer.parseInt(seat)
        return player
    }

    override fun getNextUseFulLine(iterator: Iterator<String>): String {

        var line = iterator.next()
        while (isUselessLine(line)) {
            line = iterator.next()
        }
        return line
    }


}