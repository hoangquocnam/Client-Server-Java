package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Main {

  static ServerSocket serverSocket;
  static TreeMap<Socket, ClientThread> socketThreadMap = new TreeMap<>();
  static int COUNTER_CLIENT = 0;
  static UIServer uiServer;

  public static void main(String[] args) {
    ServerHelper.printInfo("Server starting...");
    uiServer = new UIServer(ServerHelper.port);
    openSocket();
  }

  static void openSocket() {
    try {
      serverSocket = new ServerSocket(ServerHelper.port);
    } catch (IOException e) {
      ServerHelper.printError("Could not listen on port: " + ServerHelper.port);
    }

    ServerHelper.printInfo("Server started on port " + ServerHelper.port);
    Socket clientSocket;
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        ServerHelper.printInfo(
          "New client " + clientSocket.getPort() + " connected!"
        );
        ClientThread client = new ClientThread(clientSocket, COUNTER_CLIENT);
        socketThreadMap.put(clientSocket, client);
        client.start();
        COUNTER_CLIENT++;
      } catch (Exception e) {
        ServerHelper.printError(e.getMessage());
        close();
      }
    }
  }

  static void close() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      ServerHelper.printError(e.getMessage());
    }
  }
}
