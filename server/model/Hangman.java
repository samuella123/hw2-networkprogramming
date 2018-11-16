package server.model;

import java.math.*;
import java.util.*;
import java.io.*;

/**
 * This class is only for handling the game. The rules, words, and other stuff is defined
 * here. The <code>controller</code> class calls its methods. It throws exceptions to be handled by
 * controller.
 @see server.controller.Controller
 */
public class Hangman
{
  public final String FREE_WORD_CHAR = "_";

  private String wordsFilePath; // Reference file path containing words
  private Integer score; //Current score of the player
  private Integer remainingFailedAttempts; //Remaining failed attempts
  private String word; // The word that changes by true guesses and finally will be the chosen word
  private String chosenWord; // The word chosen by server
  private HangmanStatus status; //Status of the game

  /**
  * Getter for Reference file path containing words
  * @return Return Reference file path containing words
  */
  public String getWordsFilePath()
  {
    return this.wordsFilePath;
  }

  /**
  * Getter for current score of the player
  * @return Return current score of the player
  */
  public Integer getScore()
  {
    return this.score;
  }

  /**
  * Getter for remaining failed attempts
  * @return Return remaining failed attempts
  */
  public Integer getRemainingFailedAttempts()
  {
    if(this.remainingFailedAttempts!= null)
      return this.remainingFailedAttempts;
    else
      return -1;
  }

  /**
  * Getter for remaining failed attempts
  * @return Return remaining failed attempts
  */
  public String getRemainingFailedAttemptsString()
  {
    if(this.remainingFailedAttempts!= null)
      return Integer.toString(this.remainingFailedAttempts);
    else
      return "novalue";
  }

  /**
  * Getter for the word chosen by server
  * @return Return the word chosen by server
  */
  public String getWord()
  {
    if(this.word!=null)
      return this.word;
    else
      return "novalue";
  }

  /**
  * Getter for the word that changes by true guesses and finally will be the chosen word
  * @return Return the word that changes by true guesses and finally will be the chosen word
  */
  public String getChosenWord()
  {
    if(this.chosenWord != null)
      return this.chosenWord;
    else
      return "nowordchosen";
  }

  /**
  * Getter for the status of the game
  * @return Return the status of the game
  */
  public HangmanStatus getStatus()
  {
    return this.status;
  }

  /**
  * This constructor is initiating variables and words. We choose MAX_WORDS randomly from the file.
  * @param wfp The complete path to the file containing the refrence words
  * for the hangmant game. Every line, 1 word.
  * @exception IOException It will be sent in case of file location problem.
  * @exception IOException It will be sent in case of out of bound (bigger than 0) afa parameter.
  */
  public Hangman(String wfp) throws IOException
  {

    if(!(new File(wfp).canRead()))
      throw new IOException("The specified file cannot be read.");

    this.wordsFilePath = wfp;
    this.score = 0;
    this.status = HangmanStatus.EMPTY;
  }

  /**
  * Runs the game by initiating word, chosen word, and remainingFailedAttempts.
  * @exception Exception In case of worng status invoking or IOException in case of file problems.
  */
  public void startGame() throws Exception
  {
    if(this.status == HangmanStatus.ONGOING)
      throw new Exception("In this status a new game cannot be started. The ongoing game should be finished first.");

    this.chosenWord = readRandomWord();
    this.remainingFailedAttempts = this.chosenWord.length();
    String tmp = FREE_WORD_CHAR;
    for(int i=0;i<this.chosenWord.length()-1;i++)
      tmp = tmp + FREE_WORD_CHAR;

    this.word = tmp;
    this.status = HangmanStatus.ONGOING;
  }

  /**
  * If the client makes a guess on a char, then server looks into the chosen word and replaces the free places with the guessed char.
  * in a successful try, remainingFailedAttempts wont change.
  * If the client makes a guess on a word, server checks that and in a successful one game status will change to finished.
  * @param inp What client guessed.
  * @exception Exception In case of worng status invoking
  */
  public void guess(String inp) throws Exception
  {
    if(this.status != HangmanStatus.ONGOING)
      throw new Exception("In this status a guess cannot be made. The game should be initiated first.");

    Boolean wrongGuess = true;

    if(inp.length()>1)
    {
      //Guessing the whole word
      if(this.chosenWord.equals(inp))
      {
        wrongGuess = false;
        this.word = inp;
      }
    }
    else if(inp.length() == 1)
    {
      //Guessing only one char
      if(this.chosenWord.contains(inp))
      {
        wrongGuess = false;
        for(int i=0;i<this.chosenWord.length();i++)
        {
          if(this.chosenWord.charAt(i)==inp.charAt(0))
          {
            StringBuilder myTMP = new StringBuilder(this.word);
            myTMP.setCharAt(i, inp.charAt(0));
            this.word = myTMP.toString();
          }
        }
      }
    }
    else
      throw new Exception("Unknown input guess string.");

    if(this.chosenWord.equals(this.word))
    {
      //won! go to finished state with +1 score
      this.score++;
      this.chosenWord = null;
      this.remainingFailedAttempts = null;
      this.status = HangmanStatus.FINISHED;
    }
    if(wrongGuess)
    {
      //made a wrong guess, -1 remainingFailedAttempts
      this.remainingFailedAttempts--;
      if(this.remainingFailedAttempts==0)
      {
        //If remainingFailedAttempts is 0 now, go  to finished state with a -1 score.
        this.score--;
        this.word = this.chosenWord;
        this.chosenWord = null;
        this.status = HangmanStatus.FINISHED;
      }
    }
  }

  /**
  * Finish the game, the scores will be 0 and no words selected. (file is still onboard)
  * @throws Exception in case of wrong command
  */
  public void finishGame() throws Exception
  {
    this.score = 0;
    this.chosenWord = null;
    this.word = null;
    this.remainingFailedAttempts = null;
    this.status = HangmanStatus.EMPTY;
  }

  /**
  * Reads through the file and selects a word randomly.
  * @return Selected random word
  * @throws IOException in case of file not found
  */
  public String readRandomWord() throws IOException
  {
    BufferedReader reader = new BufferedReader(new FileReader(wordsFilePath));
    ArrayList<String> words = new ArrayList<String>();
    String wInp;
    while((wInp = reader.readLine())!=null)
      words.add(wInp);

    reader.close();
    return words.get((int)(Math.random()*words.size()));
  }

  /**
  * Converts the whole object to string
  * @return String variable summary of the status of the game
  */
  public String toString() {
      return this.getWord() + " | " + getRemainingFailedAttemptsString() + " | " + this.getScore() + " | " + this.getChosenWord();
  }

}
