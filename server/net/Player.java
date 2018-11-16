package server.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;

import server.controller.*;
import common.*;


/**
 * The Player class is responsible for running the whole game for the assinged
 * client and ask the game the inputs from client connection and send the asnwer
 * back to the client. It creates a controller and passes the string comming from
 * client to it.
 @see server.controller.Controller
 */
public class Player implements Runnable
{
  private final int id;
  private final GameServer server;
  private final SocketChannel playerChannel;

  private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(Constants.MAX_MSG_LENGTH);
  public final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();

  private String receivedString;

  private Controller controller;
  private Boolean isConnected;



  public int getId()
  {
    return id;
  }

  Player(GameServer server, SocketChannel playerSocket, int myId)
  {
      this.server = server;
      this.playerChannel = playerSocket;
      isConnected = true;
      id = myId;

      try
      {
          controller = new Controller(GameServer.WORDS_FILE_PATH);
          server.sendMessage(controller.getResult(),this.id);
          System.out.println("A new player connected: " + id);
      }
      catch (Exception ex)
      {
          System.err.println("Hangman words file initialization failure.");
      }

  }


  @Override
  public void run()
  {
      try
      {
        String inp = this.receivedString;
        if(inp.equals("DISCONNECT"))
        {
          disconnect();
          return;
        }
        controller.askTheGame(inp);
        server.sendMessage(controller.getResult(),this.id);
      }
      catch (Exception ex)
      {
        System.err.println("Player's socket is not working anymore: " + this.id);
        return;
      }
  }


  void sendMessage(ByteBuffer msg) throws Exception
  {
        playerChannel.write(msg);
        if (msg.hasRemaining())
        {
            throw new Exception("Could not send message");
        }
  }


  void recieveMessage() throws IOException
  {
      msgFromClient.clear();
      int numOfReadBytes;
      numOfReadBytes = playerChannel.read(msgFromClient);
      if (numOfReadBytes == -1)
      {
          throw new IOException("Player has closed connection: " + this.id);
      }

      //extractMessageFromBuffer
      msgFromClient.flip();
      byte[] bytes = new byte[msgFromClient.remaining()];
      msgFromClient.get(bytes);
      this.receivedString = new String(bytes);

      ForkJoinPool.commonPool().execute(this);
  }


  public void disconnect() throws IOException
  {
      playerChannel.close();
      isConnected = false;
      System.out.println("Player disconnected: " + this.id);
  }


  public void queueMsgToSend(ByteBuffer msg)
  {
      synchronized (messagesToSend)
      {
          messagesToSend.add(msg.duplicate());
      }
  }

  public void sendAll() throws Exception
  {
      ByteBuffer msg = null;

      synchronized (messagesToSend)
      {
          while ((msg = messagesToSend.peek()) != null)
          {
              this.sendMessage(msg);
              messagesToSend.remove();
          }
      }
  }
}
