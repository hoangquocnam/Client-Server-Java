import java.io.*;
import java.net.*;
import java.util.*;

// ClientHandler class
class ClientThread extends Thread {

  static Socket clientSocket;
  BufferedReader br;
  BufferedWriter bw;
  InputStream is;
  OutputStream os;

  private static ArrayList<ClientThread> clientList;
  private int id;
  boolean isLoggedIn;

  public ClientThread(Socket s, int id, ArrayList<ClientThread> clients) {
    this.id = id;
    clientSocket = s;
    clientList = clients;
    this.isLoggedIn = true;
    try {
      this.is = s.getInputStream();
      this.os = s.getOutputStream();
      this.br = new BufferedReader(new InputStreamReader(is));
      this.bw = new BufferedWriter(new OutputStreamWriter(os));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    String received;
    try {
      this.bw.write("Welcome to the chat room!");
      this.bw.newLine();
      this.bw.flush();
    } catch (IOException e) {
      ServerHelper.printError(e.getMessage());
    }
    while (true) {
      try {
        received = br.readLine();
        if (received.equalsIgnoreCase("quit")) {
          close();
          break;
        } else {
          ServerHelper.printInfo("Client #" + id + ": " + received);
          for (ClientThread client : clientList) {
            if (client.id != this.id) {
              client.bw.write("Client #" + id + ": " + received);
              client.bw.newLine();
              client.bw.flush();
            }
          }
        }
      } catch (IOException e) {
        ServerHelper.printError(e.getMessage());
        close();
        break;
      }
    }

    try {
      // closing resources
      this.br.close();
      this.bw.close();
    } catch (IOException e) {
      ServerHelper.printError(e.getMessage());
    }
  }

  static void close() {
    try {
      clientSocket.close();
      ClientThread foundClient = clientList
        .stream()
        .filter(c -> c.id == clientSocket.getPort())
        .findFirst()
        .get();
      clientList.remove(foundClient);
      ServerHelper.printInfo("Client #" + foundClient.id + " has left!");
    } catch (IOException e) {
      ServerHelper.printError(e.getMessage());
    }
  }
}
