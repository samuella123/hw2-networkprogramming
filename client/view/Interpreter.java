package client.view;

import java.util.*;
import java.util.concurrent.*;
import client.controller.*;


/**
 * The interpreter class is responsible for reading client's commands. It creates an
 * instance of <code>client.controller.Controller</code> class and runs its
 * <code>handleCMD</code> mathod in a completable future and submit the task
 * to the common thread pool, provided by <code>ForkJoinPool.commonPool</code>,
 * and then return immediately. Interpreter creates a <code>client.view.SafeOutput</code>
 * and passes it to every task in order to recieve the response and print it.
 * @see client.controller.Controller
 * @see client.view.SafeOutput
 */
public class Interpreter implements Runnable
{
  private final SafeOutput safeOut = new SafeOutput();
  private final Scanner console = new Scanner(System.in);
  private final Controller controller;

  /**
   * Creates the <code>client.controller.Controller</code> and saves it.
   */
  Interpreter()
  {
    controller = new Controller(safeOut);
  }

  /**
   * Reads console input line and trims it and tell <code>client.controller.Controller</code>
   * to handle it.
   */
  @Override
  public void run()
  {
    while (true)
    {
        try
        {
            String cmd = readNextLine();
            String cmd2 = cmd.trim();
            String cmd3 = cmd2.toLowerCase();

            if(cmd3.equals("quit"))
            {
              controller.handleCMD("QUIT");
              System.exit(0);
              return;
            }

            controller.handleCMD(cmd2);
        }
        catch (Exception ex)
        {
            safeOut.printResult(ex.getMessage());
        }

    }
  }

  /**
   * Reads console input line and prints a ">" char.
   */
  private String readNextLine()
  {
      safeOut.printPrompt();
      return console.nextLine();
  }
}
