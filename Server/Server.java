package Server;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

// Server class
public class Server {

  static int COUNTER_CLIENT = 0;
  static ServerSocket serverSocket;

  // UI
  static UIServer uiServer;
  static JFrame frame = new JFrame("Server");

  static void createUIServer() {
    uiServer = new UIServer(ServerStateManage.clientThreadList);
    frame.add(uiServer);
  }

  // create UI for server
  public static void createUI() {
    synchronized (ServerStateManage.clientThreadList) {
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(1500, 1000);
      frame.setLayout(new BorderLayout());
      createUIServer();
      frame.setVisible(true);
    }
  }

  public static void main(String[] args) throws IOException {
    createUI();
    start();
  }

  public static void updateUI() {
    uiServer.updateClientList();
    uiServer.updateFolderList();
  }

  static void start() {
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
        ClientThread client = new ClientThread(
          clientSocket,
          COUNTER_CLIENT,
          ServerStateManage.clientThreadList,
          ServerStateManage.folderList
        );
        ServerStateManage.addClient(client);
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
