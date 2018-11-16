package common;
/**
 * Defines all messages that can be sent between client and server
 */
public enum MsgType {
    /**
     * Starts the game
     */
    START("start"),
    /**
     * Is using for a guess in hangman
     */
    GUESS("guess"),
    /**
     * Finish the round and make the score 0
     */
    FINISH("finish"),
    /**
     * Finish the round and make the score 0
     */
    ERROR("error"),
    /**
     * Finish the round and make the score 0
     */
    WELCOME("welcome"),
    /**
     * Finish the round and make the score 0
     */
    RESULT("result");
    private String name;
    private MsgType(String inp)
    {
      this.name = inp;
    }

    public String toString()
    {
      return this.name;
    }
}
