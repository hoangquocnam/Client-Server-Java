package Server;

import java.io.*;
import java.net.*;
import java.util.*;

class ClientThread extends Thread {

  static Socket _clientSocket;
  static InputStream _is;
  static OutputStream _os;
  static BufferedReader _br;
  BufferedWriter _bw;

  public int _id;
  public int _port;

  private ArrayList<ClientThread> _clientThreadList;
  private static ArrayList<Folder> folders;

  boolean isLoggedIn = false;
  String name = null;

  public ClientThread(
    Socket s,
    int id,
    ArrayList<ClientThread> serverClients,
    ArrayList<Folder> serverFolders
  ) {
    _clientSocket = s;
    _clientThreadList = serverClients;
    folders = serverFolders;
    this._id = id;
    _port = s.getPort();
    this.isLoggedIn = true;
    try {
      _is = s.getInputStream();
      _os = s.getOutputStream();
      _br = new BufferedReader(new InputStreamReader(_is));
      _bw = new BufferedWriter(new OutputStreamWriter(_os));
    } catch (IOException e) {
      e.printStackTrace();
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
    switch (received) {
      case ":exit":
        close();
        break;
      case ":list":
        for (ClientThread clientThread : _clientThreadList) {
          send(clientThread.name);
        }
        break;
      case ":list-files":
        for (Folder folder : folders) {
          send(folder.name);
          for (File file : folder.files) {
            send(file.name);
          }
        }
        break;
      case ":create-folder":
        send("Enter folder name:");
        try {
          String folderName = _br.readLine();
          if (createFolder(folderName)) {
            send("Folder created successfully!");
          } else {
            send("Folder creation failed!");
          }
        } catch (IOException e) {
          ServerHelper.printError(e.getMessage());
        }
        break;
      default:
        break;
    }
  }

  public void run() {
    String received;
    try {
      while (true) {
        if (getNameFromClient()) {
          break;
        }
      }

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

  public int getClientPort() {
    return _port;
  }

  public boolean getClientStatus() {
    return this.isLoggedIn;
  }

  boolean getNameFromClient() {
    try {
      send("Please enter your name :");
      String received = _br.readLine();
      if (ServerHelper.isValidName(received)) {
        name = "@" + received;
        send("Welcome " + name + "!");
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

  boolean createFolder(String folderName) {
    Folder folder = new Folder(_id, folderName);
    ServerStateManage.addFolder(folder);
    return true;
  }
}
