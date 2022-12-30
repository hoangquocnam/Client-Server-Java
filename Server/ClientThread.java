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

  boolean _isLoggedIn = false;
  String _name = null;
  ArrayList<String> _foldersPath = new ArrayList<String>();
  int _selectedPathIndex = -1;
  int _id;

  public ClientThread(Socket s, int id) {
    _clientSocket = s;
    this._id = id;
    this._isLoggedIn = true;
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

    while (true) {
      if (requestDirectory()) {
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

  public void handleReceivedMessage(String received) {
    // separate the first 4 characters command and the message
    String command = received.substring(0, 4);
    String message = received.substring(4);
    switch (command) {
      case "dir%":
        _foldersPath = ServerHelper.encodeMessage(message);
        break;
      case "--f%":
        ArrayList<String> encodeString = ServerHelper.encodeMessage(message);
        // get the kind changing
        String kind = encodeString.get(0);
        String fileName = encodeString.get(1);
        UIServer.insertTable(fileName, ServerHelper.getActionKind(kind), _name);
        break;
    }
  }

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
    try {
      send("dir%");
      String received = _br.readLine();
      handleReceivedMessage(received);
      return true;
    } catch (Exception e) {
      ServerHelper.printError(e.getMessage());
      return false;
    }
  }

  void setSelectedPathIndex(int index) {
    _selectedPathIndex = index;
  }
}
