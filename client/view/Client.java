package client.view;


/**
 * This Client class contains the main function for the client side of the
 * program. Types a welcome message which is a static param and calls the interpreter class to handle
 * clients' commands. We start a new thread for running interpreter class.
 * @see client.view.Interpreter
 */
public class Client
{
    public static String WELCOME_MESSAGE = "\n-------------------------\n| Hangman Client V0.1.0 |\n| Commands description: |\n-------------------------\n>\n>  1) Connect <ip> <port> \n>  2) Quit \n>"; //the welcome message
    /**
     * Prints the welcome message to the client and creates an interpreter class to handle client's
     * commands.
     * @param args There is no input args from CMD
     */
    public static void main(String[] args)
    {
      //print a welcome message and commands description
      System.out.println(WELCOME_MESSAGE);

      Interpreter intr = new Interpreter();
      new Thread(intr).start();
    }
}
