package org.ablx.cardroom.parser

import org.ablx.cardroom.commons.data.Hand
import org.ablx.cardroom.commons.data.Player
import org.ablx.cardroom.commons.enumeration.Action
import org.ablx.cardroom.commons.enumeration.Card
import org.ablx.cardroom.commons.enumeration.GameType

import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class WinamaxParser : Parser, CardroomParser() {


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
    protected val LEVEL_ESPACE = LEVEL + ESPACE
    protected val PLUS_ESPACE = PLUS + ESPACE
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

    override fun parse(): Map<String, Hand> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseAntesAndBlinds(nextLine: String, input: Scanner, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseBigBlind(chaine: String): Double? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseButtonSeat(chaine: String): Int? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseBuyIn(chaine: String): Double? {
        var startPosition = chaine.indexOf(BUY_IN) + BUY_IN.length
        var endPosition = chaine.indexOf(LEVEL)

        var buyIn = chaine.substring(startPosition, endPosition)

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

    override fun parseCurrency(chaine: String): Currency {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseDealer(nextLine: String, input: Scanner, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseFee(chaine: String): Double? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseGameIdSite(chaine: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseHandDate(chaine: String): Date {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseHandId(chaine: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseLevel(chaine: String): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseNewHandLine(nextLine: String, input: Scanner, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseNumberOfPlayerByTable(chaine: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parsePlayerAccount(chaine: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parsePlayerSeat(chaine: String): Player {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseRake(chaine: String): Double? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseSeatLine(nextLine: String, input: Scanner, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseSmallBlind(chaine: String): Double? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseTableId(chaine: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseTableLine(nextLine: String, input: Scanner, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseTotalPot(chaine: String): Double? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parsing(): Map<String, Hand> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readActionsByPhase(nextLine: String, input: Scanner, hand: Hand, phase: String, nextPhases: Array<String>, actions: List<Action>): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readCards(chaine: String): Array<Card> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readFlop(nextLine: String, input: Scanner, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readHandFile(filePath: String): String {
        var encoded: ByteArray = Files.readAllBytes(Paths.get(filePath))
        return String(encoded, Charsets.UTF_8)
    }

    override fun readPlayer(chaine: String, players: Map<String, Player>): Action {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readPreflop(nextLine: String, input: Scanner, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readRiver(nextLine: String, input: Scanner, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readShowdown(nextLine: String, input: Scanner, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readSummary(nextLine: String, input: Scanner, phase: String, nextPhases: Array<String>, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readTurn(nextLine: String, input: Scanner, hand: Hand): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setDevise(devise: Currency) {
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


}