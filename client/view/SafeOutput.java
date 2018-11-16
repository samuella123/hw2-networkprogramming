package client.view;

/**
 * It is a class for writing into console and it is completely threadsafe.
 */
public class SafeOutput
{
    private static final String PROMPT = "> ";
    /**
     * Prints the output
     * @param output The variable to print.
     */
    public synchronized void print(String output)
    {
        System.out.print(output);
    }

    /**
     * Synchronized method prints the output as a line
     * @param output The variable to print.
     */
    public synchronized void println(String output)
    {
        System.out.println(output);
    }

    /**
     * Prints the output as a line with <code>PROMP</code>
     * @param output The variable to print.
     */
    public synchronized void printResult(String output)
    {
        System.out.println(output);
        System.out.print(PROMPT);
    }

    /**
     * Prints the PROMP char
     */
    public synchronized void printPrompt()
    {
        System.out.print(SafeOutput.PROMPT);
    }

}
