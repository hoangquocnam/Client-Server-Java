package Server;

public class Main {

  static UIServer uiServer;

  public static void main(String[] args) {
    ServerHelper.printInfo("Server starting...");
    uiServer = new UIServer(ServerHelper.port);
  }
}
