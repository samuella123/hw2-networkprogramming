package server.model;

/**
 * Defines all statuses of the hangman class
 */
public enum HangmanStatus {
    /**
     * When the object is created but not initiated. (after calling finishgame and constructor)
     */
    EMPTY,
    /**
     * After startGame command, player is geussing
     */
    ONGOING,
    /**
     * After one round finishes. successful or not.
     */
    FINISHED;
}
