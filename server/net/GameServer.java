package server.net;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;


/**
 * The GameServer class is responsible for communicating the clients. Given the port
 * to the main function argument, it listens to all incomming connections and creates
 * a <code>server.net.Player</code> object for each of them and runs it on a seperate thread.
 * @see server.net.Player
 */
public class GameServer
{
  public static int idNumerator = 0;

  private static final int LINGER_TIME = 5000; //socket linger time
  private static final int TIMEOUT_TIME = 1500000; //socket timeout time
  public static final String WORDS_FILE_PATH = "words.txt";

  private final Queue<MessageToSend> messagesToSend = new ArrayDeque<>();

  private int portNo = 8080;
  private Selector selector;
  private ServerSocketChannel listeningSocketChannel;
  private volatile boolean isTimeToSend = false;




  /**
   * Sends the specified message to all connected clients
   *
   * @param msg The message to be sent to player with id
   * @param id Send to player with id
   */
  public void sendMessage(String msg,int id)
  {
      isTimeToSend = true;
      ByteBuffer completeMsg = ByteBuffer.wrap(msg.getBytes());
      synchronized (messagesToSend)
      {
          messagesToSend.add(new MessageToSend(completeMsg,id));
      }
      selector.wakeup();
  }



  /**
   * @param args uses one cmd argument, the port number. by default it is 8080
   */
  public static void main(String[] args)
  {
      GameServer server = new GameServer();

      if(server.validatePort(args))
        server.portNo = Integer.parseInt(args[0]);

      server.run();
  }


  private void run()
  {
      try
      {
          selector = Selector.open();
          listeningSocketChannel = ServerSocketChannel.open();
          listeningSocketChannel.configureBlocking(false);
          listeningSocketChannel.bind(new InetSocketAddress(portNo));
          listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

          while (true)
          {
              if (isTimeToSend)
              {
                  synchronized (messagesToSend)
                  {
                      MessageToSend msgToSend;
                      while ((msgToSend = messagesToSend.poll()) != null)
                      {
                        //writeOperationForTheClient
                        for (SelectionKey key : selector.keys())
                        {
                            Player player = (Player) key.attachment();
                            if ((key.channel() instanceof SocketChannel) && (key.isValid()) && (player!=null))
                            {
                                if(player.getId() == msgToSend.getId())
                                {
                                  key.interestOps(SelectionKey.OP_WRITE);
                                }
                            }
                        }

                        //appendMsgToClientQueue
                        for (SelectionKey key : selector.keys())
                        {
                            Player player = (Player) key.attachment();
                            if (player == null)
                            {
                                continue;
                            }
                            if(player.getId() == msgToSend.getId())
                            {
                              synchronized (player.messagesToSend)
                              {
                                  player.queueMsgToSend(msgToSend.getMessage());
                              }
                            }
                        }
                      }
                  }
                  isTimeToSend = false;
              }


              selector.select();
              Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
              while (iterator.hasNext())
              {
                  SelectionKey key = iterator.next();
                  iterator.remove();
                  if (!key.isValid())
                  {
                      continue;
                  }
                  if (key.isAcceptable())
                  {
                      //startHandler(key)
                      ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                      SocketChannel clientChannel = serverSocketChannel.accept();
                      clientChannel.configureBlocking(false);

                      GameServer.idNumerator++;
                      Player player = new Player(this, clientChannel,GameServer.idNumerator);

                      clientChannel.register(selector, SelectionKey.OP_WRITE, player);
                      clientChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
                  }
                  else if (key.isReadable())
                  {
                      //recvFromClient(key)
                      Player player = (Player) key.attachment();
                      try
                      {
                          player.recieveMessage();
                      }
                      catch (IOException clientHasClosedConnection)
                      {
                          removePlayer(key);
                      }
                  }
                  else if (key.isWritable())
                  {
                      //sendToClient(key)
                      Player player = (Player) key.attachment();
                      try
                      {
                          player.sendAll();
                          key.interestOps(SelectionKey.OP_READ);
                      }
                      catch (Exception ex)
                      {
                          removePlayer(key);
                      }
                  }
              }
          }
      }
      catch (Exception e)
      {
          System.err.println("Server failure.");
      }
  }


  public void removePlayer(SelectionKey clientKey) throws IOException
  {
      Player player = (Player) clientKey.attachment();
      player.disconnect();
      clientKey.cancel();
  }

  public void removePlayerById(int id)
  {
    try
    {
        selector = Selector.open();
        for (SelectionKey key : selector.keys())
        {
            Player player = (Player) key.attachment();
            if ((key.channel() instanceof SocketChannel) && (key.isValid()) && (player!=null))
            {
                if(player.getId() == id)
                {
                  key.cancel();
                }
            }
        }
    }
    catch(Exception ex)
    {
      
    }
  }


  private Boolean validatePort(String[] arg)
  {
      if(arg.length==0)
        return false;

      try
      {
        int prt = Integer.parseInt(arg[0]);
        if( (prt < 1024) || (prt > 65535) )
          throw new Exception();
      }
      catch (Exception e)
      {
        System.err.println("The entered port number is corrupt. Going on with the default.");
        return false;
      }

      return true;
  }


  private class MessageToSend
  {
    private int id;
    private ByteBuffer message;
    public MessageToSend(ByteBuffer m,int i)
    {
      id = i;
      message = m;
    }
    public int getId()
    {
      return id;
    }
    public ByteBuffer getMessage()
    {
      return message;
    }
  }

}
