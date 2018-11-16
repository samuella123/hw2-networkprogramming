package client.controller;

/**
 * All commands that can be types by client
 */
public enum CmdType {
    /**
     * Connect to the server.
     */
    CONNECT("connect"),
    /**
     * This is reserved for game commands.
     */
    START("start"),
    /**
     * This is reserved for game commands.
     */
    GUESS("guess"),
    /**
     * This is reserved for game commands.
     */
    FINISH("finish"),
    /**
     * This is reserved for game commands.
     */
    DISCONNECT("disconnect");

    private String name;
    private CmdType(String inp)
    {
      this.name = inp;
    }

    public String toString()
    {
      return this.name;
    }
}
