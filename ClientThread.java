import java.io.*;
import java.net.*;
import java.util.*;

class ClientThread extends Thread {

  static Socket clientSocket;
  static InputStream is;
  static OutputStream os;
  static BufferedReader br;
  BufferedWriter bw;

  private static ArrayList<ClientThread> clientList;
  private int id;
  boolean isLoggedIn = false;
  String name = null;

  public ClientThread(Socket s, int id, ArrayList<ClientThread> clients) {
    this.id = id;
    clientSocket = s;
    clientList = clients;
    this.isLoggedIn = true;
    try {
      is = s.getInputStream();
      os = s.getOutputStream();
      br = new BufferedReader(new InputStreamReader(is));
      bw = new BufferedWriter(new OutputStreamWriter(os));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void send(String message) {
    try {
      bw.write(message);
      bw.newLine();
      bw.flush();
    } catch (IOException e) {
      ServerHelper.printError(
        "Error in sending message to client" + id + " " + name
      );
    }
  }

  public void sendAll(String message) {
    try {
      for (ClientThread client : clientList) {
        if (client.id != this.id) {
          client.send(message);
        }
      }
    } catch (Exception e) {
      ServerHelper.printError("Error in sending message to all clients");
    }
  }

  private boolean isValidName(String name) {
    boolean isFormatValid = name.indexOf('@') == -1 && name.indexOf('!') == -1;
    return isFormatValid;
  }

  public void run() {
    String received;
    try {
      while (true) {
        synchronized (this) {
          send("> Please enter your name :");
          received = br.readLine();
          if (isValidName(received)) {
            name = "@" + received;
            send("> Welcome " + name + "!");
            ServerHelper.printInfo(
              "Client #" + id + " -> " + name + " has joined!"
            );
            sendAll(name + " has joined!");
            break;
          } else {
            send("> Invalid name. Please try again.");
          }
        }
      }

      while (true) {
        synchronized (this) {
          try {
            received = br.readLine();
            if (received.equalsIgnoreCase("quit")) {
              close();
              break;
            } else {
              ServerHelper.printInfo(name + ": " + received);
              sendAll(name + ": " + received);
            }
          } catch (IOException e) {
            ServerHelper.printError(e.getMessage());
            close();
            break;
          }
        }
      }

      try {
        br.close();
        bw.close();
      } catch (IOException e) {
        ServerHelper.printError(e.getMessage());
      }
    } catch (Exception e) {
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
