package server.controller;

import java.io.*;
import common.*;
import server.model.*;

/**
* This class designed in order to completely handle string messages between clinets and their game
* and It will handle possible exceptions and prepares a nice and easy response to all messages from clients.
* There is a <code>server.controller.Controller.Message</code> private class for
* converting string to a class that can be handled much easier. Controller also handles exceptions
* comming from Hangman class.
* @see server.controller.Controller.Message
* @see common.MsgType
* @see server.model.Hangman
*/

public class Controller
{
  private static final String WELCOME_MESSAGE = MsgType.WELCOME + Constants.MSG_DELIMETER + "You are connected to the game and good to go. Commands description: \n 1) Start \n 2) Guess (followed by a word or character) \n 3) Finish \n 4) Disconnect \n"; //the welcome message sent to client.
  private Hangman hangman;
  private String result;
  /**
   * Constructs the controller object
   * @param wordsFilePath Reference file path containing words
   * @throws IOException in case of file not found
   */
  public Controller(String wordsFilePath) throws IOException
  {
    hangman = new Hangman(wordsFilePath);
    result = WELCOME_MESSAGE;
  }

  /**
   * Translates incomming strings to commands for the game
   * @param cmd is the raaw string comming from the client
   *
   */
  public void askTheGame(String cmd)
  {
    Message msg = new Message(cmd);
    try
    {
      switch (msg.msgType)
      {
      case START:
        hangman.startGame();
        result =  MsgType.RESULT + Constants.MSG_DELIMETER + hangman.getWord() + " " + hangman.getRemainingFailedAttemptsString() + " " + hangman.getScore();
        break;
      case GUESS:
        hangman.guess(msg.message);
        result =  MsgType.RESULT + Constants.MSG_DELIMETER + hangman.getWord() + " " + hangman.getRemainingFailedAttemptsString() + " " + hangman.getScore();
        break;
      case FINISH:
        hangman.finishGame();
        result =  MsgType.RESULT + Constants.MSG_DELIMETER + hangman.getWord() + " " + hangman.getRemainingFailedAttemptsString() + " " + hangman.getScore();
        break;
      default:
        result =  MsgType.ERROR + Constants.MSG_DELIMETER + "corrupt command.";

      }
    }
    catch(Exception ex)
    {
      result = MsgType.ERROR + Constants.MSG_DELIMETER + ex.getMessage();
    }
  }

  private static class Message
  {
      private MsgType msgType;
      private String message;

      private Message(String inp)
      {
          String[] splitted = inp.split(Constants.MSG_DELIMETER);
          msgType = MsgType.valueOf(splitted[0].toUpperCase());
          if(splitted.length>1)
            message = splitted[1].toLowerCase();
          else
            message = null;
      }
  }

  /**
  * When any function gets invoked in this class, there is an answer for that. this function will
  * hand it.
  * @return will be the response from the game. It may be an error.
  */
  public String getResult()
  {
    return result;
  }
}
