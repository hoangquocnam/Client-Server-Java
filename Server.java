import java.io.*;
import java.net.*;
import java.util.*;

// Server class
public class Server {

  static ArrayList<ClientThread> clientList = new ArrayList<>();
  static int COUNTER_CLIENT = 0;
  static ServerSocket serverSocket;

  public static void main(String[] args) throws IOException {
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
          clientSocket.getPort(),
          clientList
        );
        clientList.add(client);
        client.start();
        ServerHelper.printInfo(
          "Client " + clientSocket.getPort() + " connected!"
        );
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
