package org.ablx.cardroom.commons.enumeration

enum class PokerstarsActions(val value: String, val useful: Boolean) {

    /**
     * Action FOLD.
     */
    FOLDS("folds", true),
    /**
     * Action CALL.
     */
    CALLS("calls", true),
    /**
     * Action RAISE.
     */
    RAISES("raises", true),
    /**
     * Action CHECK.
     */
    CHECKS("checks", true),
    /**
     * Action BET.
     */
    BETS("bets", true),
    /**
     * Action COLLECTED.
     */
    COLLECTED("collected", true),
    /**
     * Action SHOW.
     */
    SHOWS("shows", true),
    /**
     * Action SHOW.
     */

    WILL_BE_ALLOWED_TO_PLAY_AFTER_THE_BUTTON(
            "will be allowed to play after the button", false),
    /**
     * Action SHOW.
     */
    POSTS_SMALL_ET_BIG_BLINDS("posts small & big blinds", false),
    /**
     * Action SHOW.
     */
    POSTS_THE_ANTE("posts the ante", false),
    /**
     * Action SHOW.
     */
    SITS_OUT("sits out ", false),
    /**
     * Action SHOW.
     */
    LEAVES_THE_TABLE("leaves the table", false),
    /**
     * Action SHOW.
     */
    IS_SITTING_OUT("is sitting out ", false),
    /**
     * Action SHOW.
     */
    IS_DISCONNECTED("is disconnected ", false),
    /**
     * Action SHOW.
     */
    IS_CONNECTED("is connected ", false),
    /**
     * Action SHOW.
     */
    SAID("said,", false),
    /**
     * Action SHOW.
     */
    HAS_TIMED_OUT("has timed out", false),
    /**
     * Action SHOW.
     */
    JOINS_THE_TABLE_AT_SEAT("joins the table at seat", false),

    /**
     * Action SHOW.
     */
    UNCALLED_BET("Uncalled bet", false),

    /**
     * Action SHOW.
     */
    HAS_RETURNED("has returned", false),
    /**
     * Action SHOW.
     */
    DOESNT_SHOW_HAND("doesn't show hand", false),
    /**
     * Action SHOW.
     */
    WAS_REMOVED_FROM_THE_TABLE_FOR_FAILING_TO_POST(
            "was removed from the table for failing to post", false),

    /**
     * Action SHOW.
     */
    MUCKS_HAND("mucks hand", false),
    /**
     * Action SHOW.
     */
    FINISHED_THE_TOURNAMENT_IN("finished the tournament in", false);


}