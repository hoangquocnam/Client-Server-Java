import java.io.*;
import java.net.*;
import java.util.*;

class ClientThread extends Thread {

  static Socket clientSocket;
  static InputStream is;
  static OutputStream os;
  static BufferedReader br;
  static BufferedWriter bw;

  private static ArrayList<ClientThread> clientList;
  private int id;
  boolean isLoggedIn;

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

  public void run() {
    String received;
    try {
      String name;
      while (true) {
        synchronized (this) {
          bw.write("Please enter your name :");
          bw.newLine();
          bw.flush();

          name = ((String) br.readLine()).trim();
          ServerHelper.printInfo("Client #" + id + "'s name is: " + name);

          if ((name.indexOf('@') == -1) || (name.indexOf('!') == -1)) {
            break;
          } else {
            bw.write("The name should not contain '@' or '!'");
            bw.newLine();
            bw.flush();
          }
        }
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
