package server.model;

import java.util.*;
import server.controller.*;

public class HangmanTest
{
  public static void main(String[] args)
  {
    try
    {
      /*Hangman ins = new Hangman("../../words.txt");
      System.out.println(ins);*/

      Controller controller = new Controller("words.txt");
      System.out.println(controller.getResult());

      Scanner scanner = new Scanner(System.in);

      while(true)
      {
        String line = scanner.nextLine();
        controller.askTheGame(line);
        System.out.println(controller.getResult());
        /*if(line.equals("exit"))
          break;
        else if(line.equals("start game"))
        {
          ins.startGame();
          System.out.println(ins);
        }
        else if(line.equals("finish game"))
        {
          ins.finishGame();
          System.out.println(ins);
        }
        else
        {
          String[] splited = line.split(" ");
          if(!splited[0].equals("guess"))
          {
            System.out.println("non sense: -" + line + "-");
            continue;
          }
          ins.guess(splited[1]);
          System.out.println(ins);
          */
        }




    }
    catch (Exception e)
    {
      System.out.println(e);
    }
  }
}
