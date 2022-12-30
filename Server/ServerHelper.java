package Server;

import java.util.ArrayList;
import java.util.TreeMap;

public class ServerHelper {

  public enum LogType {
    INFO,
    ERROR,
    WARNING,
  }

  public enum Action {
    CREATE,
    DELETE,
    RENAME,
    MODIFY,
    START,
  }

  public static int port = 3200;

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

  static String[] TABLE_HEADERS = new String[] {
    "Time",
    "Monitoring directory",
    "Client",
    "Action",
  };

  public static boolean isValidName(String received) {
    boolean isFormatValid =
      received.indexOf('@') == -1 && received.indexOf('!') == -1;
    return isFormatValid;
  }

  static ArrayList<String> encodeMessage(String message) {
    ArrayList<String> chunks = new ArrayList<String>();
    String[] paths = message.split("\\$");
    for (String p : paths) {
      if (p.length() > 0) {
        chunks.add(p);
      }
    }
    return chunks;
  }

  static String getActionKind(String type) {
    switch (type) {
      case "ENTRY_MODIFY":
        return "MODIFY";
      case "ENTRY_CREATE":
        return "CREATE";
      case "ENTRY_DELETE":
        return "DELETE";
      default:
        return "Unknown";
    }
  }
}
