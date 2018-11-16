package client.net;

import java.io.*;
import java.net.*;
import client.view.*;
import common.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.*;
import java.util.concurrent.ForkJoinPool;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;



/**
 * The ServerConnection class is responsible for communicating the server. Given the ip and port
 * to the <code>connect</code> method, it trys to get connected and then prints the result with
 * <code>client.view.SafeOutput</code> given from the controller. After establishing a connection,
 * We create a <code>client.net.ServerConnection.Listener</code> class on a separete thread in order
 * to take care of the responses of the server and printing them using <code>client.view.SafeOutput</code>.
 * there are 2 staic parameters that should be initialised, <code>TIMEOUT_TIME_HOUR</code> and
 * <code>TIMEOUT_TIME_MIN</code>.
 * @see client.view.SafeOutput
 * @see client.net.ServerConnection.Listener
 */
public class ServerConnection implements Runnable
{

    private final ByteBuffer serverMessage = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
    private final Queue<ByteBuffer> clientMessage = new ArrayDeque<>();

    //HERHERHEHREHRHE
    private final List<Listener> listeners = new ArrayList<>();

    private volatile Boolean isConnected = false;
    private volatile boolean isTimeToSend = false;


    private SocketChannel socketChannel;
    private Selector selector;

    private SafeOutput safeOut;
    private InetSocketAddress serverInetSocketAddress;

    /**
     * It is used by other classes to check whether we are connected to server
     * or not.
     * @return A Bloolean.
     */
    public Boolean getConnected()
    {
      return isConnected;
    }



    /**
     * The communicating thread, all communication is non-blocking. First, server connection is
     * established. Then the thread sends messages submitted via one of the <code>send</code>
     * methods in this class and receives messages from the server.
     */
    @Override
    public void run()
    {
        try
        {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(serverInetSocketAddress);
            isConnected = true;
            Boolean clientMessageIsEmpty = clientMessage.isEmpty();
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);


            while (isConnected || !clientMessageIsEmpty)
            {
                if (isTimeToSend)
                {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    isTimeToSend = false;
                }

                selector.select();
                for (SelectionKey key : selector.selectedKeys())
                {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid())
                    {
                        continue;
                    }
                    if (key.isConnectable())
                    {
                        //handle complete connection
                        socketChannel.finishConnect();
                        key.interestOps(SelectionKey.OP_READ);
                        try
                        {
                            InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                            //say that connection is completed from remote address
                            Executor pool = ForkJoinPool.commonPool();
                            for (Listener listener : listeners)
                            {
                                pool.execute(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        listener.connected(remoteAddress);
                                    }
                                });
                            }
                        } //could not connect to specified address
                        catch (IOException defaultMode)
                        {
                            //say that connection is completed from server address
                            Executor pool = ForkJoinPool.commonPool();
                            for (Listener listener : listeners)
                            {
                                pool.execute(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        listener.connected(serverInetSocketAddress);
                                    }
                                });
                            }
                        }
                    }
                    else if (key.isReadable())
                    {
                        //handle recieve from server
                        serverMessage.clear();
                        if (socketChannel.read(serverMessage) == -1)
                        {
                            System.err.println("Connection terminated.");
                        }

                        //extract message from buffer
                        serverMessage.flip();
                        int cap = serverMessage.remaining();
                        byte[] bytes = new byte[cap];
                        serverMessage.get(bytes);
                        String fromServerString = new String(bytes);


                        //System.out.println(fromServerString);

                        //notifyMsgReceived();
                        Executor pool = ForkJoinPool.commonPool();
                        for (Listener listener : listeners)
                        {
                            pool.execute(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    listener.recievedMessage(fromServerString);
                                }
                            });
                        }

                    }
                    else if (key.isWritable())
                    {
                        //send to server
                        ByteBuffer tMessage;
                        synchronized (clientMessage)
                        {
                            while ((tMessage = clientMessage.peek()) != null)
                            {
                                socketChannel.write(tMessage);
                                if (tMessage.hasRemaining())
                                {
                                    return;
                                }
                                clientMessage.remove();
                            }
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Connection terminated.");
        }

        try
        {
          //close everything and notify listeneres
          socketChannel.close();
          socketChannel.keyFor(selector).cancel();
          Executor pool = ForkJoinPool.commonPool();
          for (Listener listener : listeners)
          {
              pool.execute(new Runnable()
              {
                  @Override
                  public void run()
                  {
                      listener.disconnected();
                  }
              });
          }
        }
        catch (IOException ex)
        {
            System.err.println("Unable to close the socket.");
        }
    }


    /**
     * Starts the communicating thread and connects to the server.
     *
     * @param host Host name or IP address of server.
     * @param port Server's port number.
     * @throws IOException If failed to connect.
     * @param safeOut is the <code>client.view.SafeOutput</code> reference for printing results.
     */
    public void connect(String host, int port, SafeOutput safeOut)
    {
        listeners.add(new Listener(safeOut));
        serverInetSocketAddress = new InetSocketAddress(host, port);
        new Thread(this).start();
    }

    /**
     * Sends the message to the server.
     *
     * @param inp The message wants to be sent.
     */
    public void sendMessage(String inp)
    {
        //String inp2 = MessageSplitter.prependLengthHeader(inp);
        String inp2 = inp;
        synchronized (clientMessage)
        {
            clientMessage.add(ByteBuffer.wrap(inp2.getBytes()));
        }
        selector.wakeup();
        isTimeToSend = true;
    }

    /**
     * It will closes the socket and our state changes to not connected.
     */
    public void disconnect()
    {
        try
        {
          if(isConnected)
          {
              sendMessage("DISCONNECT");
              socketChannel.close();
              socketChannel.keyFor(selector).cancel();

              //Notify Disconnection done
              Executor pool = ForkJoinPool.commonPool();
              for (Listener listener : listeners)
              {
                  pool.execute(new Runnable()
                  {
                      @Override
                      public void run()
                      {
                          listener.disconnected();
                      }
                  });
              }
          }
        }
        catch(IOException ex)
        {
            System.err.println("Unable to close the socket.");
        }
        isConnected = false;
    }


    public void quitIt()
    {
      ExecutorService pool = ForkJoinPool.commonPool();
      pool.shutdownNow();
    }

    /**
    * We create This class on a separete thread in order to take care of the
    * responses of the server and printing them using <code>client.view.SafeOutput</code>.
    * @see client.view.SafeOutput
    */
    private class Listener
    {
        private final SafeOutput safeOut;

        private Listener(SafeOutput safeOut)
        {
            this.safeOut = safeOut;
        }

        /**
        * read line from the ongoing connection and observe the
        * responses of the server and printing them using <code>client.view.SafeOutput</code>.
        * @see client.view.SafeOutput
        */
        public void recievedMessage(String inp)
        {
            try
            {
                    safeOut.printResult(reviseMessage(inp));
            }
            catch (Exception ex)
            {
                safeOut.printResult("Connection terminated.");
            }
        }


        /**
        * This method trims the message comming from the server and prints it.
        * it splits the message <code>Constants.MSG_DELIMETER</code> and shows it.
        */
        private String reviseMessage(String inp)
        {
          String[] msgParts = inp.split(Constants.MSG_DELIMETER);
          if(msgParts.length == 1)
            return inp;
          else if(!msgParts[0].equals(MsgType.RESULT.toString()))
          {
            return msgParts[0] + ": " + msgParts[1];
          }
          else
          {
            String[] msgParts2 = msgParts[1].split("\\s+");
            return msgParts[0] + ": " + msgParts2[0] + " attempts remaining: " + msgParts2[1] + " score: " + msgParts2[2];
          }
        }

        public void disconnected()
        {
            safeOut.printResult("Disconnected from server.");
        }

        public void connected(InetSocketAddress address)
        {
            safeOut.printResult("Successfully connected to the game server : " + serverInetSocketAddress.getHostName() + ":" + serverInetSocketAddress.getPort());
        }


    }
}
