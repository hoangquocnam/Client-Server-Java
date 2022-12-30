package Server;

import java.util.ArrayList;

public class ServerStateManage {

  static ArrayList<ClientThread> clientThreadList = new ArrayList<>();
  static ArrayList<Folder> folderList = new ArrayList<>();
  static ArrayList<File> fileList = new ArrayList<>();
  static Server _server;


  // create 
  public static void addClient(ClientThread client) {
    clientThreadList.add(client);
    Server.updateUI();
  }

  public static void addFolder(Folder folder) {
    folderList.add(folder);
    Server.updateUI();
  }

  public static void addFile(File file) {
    fileList.add(file);
    Server.updateUI();
  }

  // remove
  public static void removeClient(ClientThread client) {
    clientThreadList.remove(client);
    Server.updateUI();
  }

  public static void removeFolder(Folder folder) {
    folderList.remove(folder);
    Server.updateUI();
  }

  public static void removeFile(File file) {
    fileList.remove(file);
    Server.updateUI();
  }

  // get all

  static ArrayList<File> getFiles() {
    return fileList;
  }

  ArrayList<Folder> getFolders() {
    return folderList;
  }

  static ArrayList<Folder> getClientFolders(int clientId) {
    ArrayList<Folder> folders = new ArrayList<>();
    for (Folder folder : folderList) {
      if (folder.clientId == clientId) {
        folders.add(folder);
      }
    }
    return folders;
  }

  Folder getFolder(int id) {
    for (Folder folder : folderList) {
      if (folder.id == id) {
        return folder;
      }
    }
    return null;
  }

  ArrayList<ClientThread> getClients() {
    return clientThreadList;
  }

}
