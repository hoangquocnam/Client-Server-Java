package Server;

import java.io.*;
import java.net.*;
import java.util.*;

class ClientThread extends Thread {

  Socket _clientSocket;
  static InputStream _is;
  static OutputStream _os;
  static BufferedReader _br;
  BufferedWriter _bw;

  boolean isLoggedIn = false;
  String _name = null;
  int _id;

  public ClientThread(Socket s, int id) {
    _clientSocket = s;
    this._id = id;
    this.isLoggedIn = true;
    try {
      _is = s.getInputStream();
      _os = s.getOutputStream();
      _br = new BufferedReader(new InputStreamReader(_is));
      _bw = new BufferedWriter(new OutputStreamWriter(_os));
    } catch (IOException e) {
      e.printStackTrace();
    }

    while (true) {
      if (getNameFromClient()) {
        break;
      }
    }
  }

  public void send(String message) {
    try {
      _bw.write("> " + message);
      _bw.newLine();
      _bw.flush();
    } catch (IOException e) {
      ServerHelper.printError("Error in sending message to client" + _id);
    }
  }

  public void handleReceivedMessage(String received) {}

  public void run() {
    String received;
    try {
      while (true) {
        synchronized (this) {
          try {
            received = _br.readLine();
            if (received != null) {
              handleReceivedMessage(received);
            }
          } catch (IOException e) {
            ServerHelper.printError(e.getMessage());
            close();
            break;
          }
        }
      }

      try {
        _br.close();
        _bw.close();
      } catch (IOException e) {
        ServerHelper.printError(e.getMessage());
      }
    } catch (Exception e) {
      ServerHelper.printError("nam");
      e.printStackTrace();
    }
  }

  void close() {
    send(":exit");
    try {
      _clientSocket.close();
    } catch (IOException e) {
      ServerHelper.printError(e.getMessage());
    }
  }

  public int getClientId() {
    return _id;
  }

  public boolean getClientStatus() {
    return this.isLoggedIn;
  }

  boolean getNameFromClient() {
    try {
      send("Please enter your name :");
      String received = _br.readLine();
      if (ServerHelper.isValidName(received)) {
        _name = received;
        send("Welcome " + _name + "!");
        // _client.setName(name);
        return true;
      } else {
        send("Invalid name. Please try again.");
        return false;
      }
    } catch (IOException e) {
      ServerHelper.printError(e.getMessage());
      return false;
    }
  }

  boolean requestDirectory() {
    send(":dir");
    return true;
  }
}
