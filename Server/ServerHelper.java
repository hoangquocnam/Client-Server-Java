package Server;

import java.util.*;

class File {

  String name;
  int id;
  int clientId;
  int folderId;
  String content;

  public File(int id, int clientId, int folderId, String name, String content) {
    this.id = id;
    this.clientId = clientId;
    this.folderId = folderId;
    this.name = name;
    this.content = content;
  }
}

class Folder {

  String name;
  int id;
  int clientId;
  ArrayList<File> files = new ArrayList<>();

  public Folder(int clientId, String name) {
    this.clientId = clientId;
    this.name = name;
    this.id = ServerStateManage.getClientFolders(clientId).size();
  }
}

public class ServerHelper {

  public enum LogType {
    INFO,
    ERROR,
    WARNING,
  }

  public static int port = 3200;
  private static String CLIENT_DATABASE = "client_database.txt";
  private static String FOLDER_DATABASE = "folder_database.txt";
  private static String FILE_DATABASE = "file_database.txt";

  static void print(String s, LogType type) {
    String message = "";
    switch (type) {
      case INFO:
        message += "[INFO]";
        break;
      case ERROR:
        message += "[ERROR]";
        break;
      case WARNING:
        message += "[WARNING]";
        break;
      default:
        message += "[INFO]";
        break;
    }
    System.out.println(message + ": " + s);
  }

  static void printError(String s) {
    print(s, LogType.ERROR);
  }

  static void printInfo(String s) {
    print(s, LogType.INFO);
  }

  static void printWarning(String s) {
    print(s, LogType.WARNING);
  }

  static boolean isValidName(String name) {
    boolean isFormatValid = name.indexOf('@') == -1 && name.indexOf('!') == -1;
    return isFormatValid;
  }
}
