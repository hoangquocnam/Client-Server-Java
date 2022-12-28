public class ServerHelper {

  public enum LogType {
    INFO,
    ERROR,
    WARNING,
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
}
