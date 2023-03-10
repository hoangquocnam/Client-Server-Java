import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class Main implements Runnable {

  static Socket clientSocket;
  static InputStream is;
  static OutputStream os;
  static BufferedReader br;
  static BufferedWriter bw;
  private static BufferedReader inputLine = null;

  static void sendServer(String message) {
    try {
      bw.write(message);
      bw.newLine();
      bw.flush();
    } catch (IOException e) {
      System.out.println("Error in sending message");
    }
  }

  public static void main(String arg[]) {
    try {
      try {
        clientSocket = new Socket(ClientHelper.host, ClientHelper.port);
      } catch (Exception e) {
        System.out.println(
          "No Server found. Please ensure that the Server program is running and try again."
        );
      }
      if (clientSocket == null) {
        return;
      }
      is = clientSocket.getInputStream();
      os = clientSocket.getOutputStream();
      br = new BufferedReader(new InputStreamReader(is));
      bw = new BufferedWriter(new OutputStreamWriter(os));
      inputLine = new BufferedReader(new InputStreamReader(System.in));

      String sentMessage = "";

      try {
        Thread t = new Thread(new Main());
        t.start();
        while (true) {
          sentMessage = inputLine.readLine();
          if (sentMessage.equals("exit") || sentMessage.equals("quit")) {
            sendServer("ext%");
          } else {
            sendServer(sentMessage);
          }
        }
      } catch (Exception e) {
        System.out.println("Error in sending message");
      }

      bw.close();
      br.close();
      clientSocket.close();
    } catch (IOException e) {
      System.out.println("");
    }
  }

  public static void watchDirectory(String folderPath) {
    try {
      WatchService watcher = FileSystems.getDefault().newWatchService();
      String path = System.getProperty("user.home") + "/" + folderPath;
      Path dir = Paths.get(path);
      dir.register(
        watcher,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY,
        StandardWatchEventKinds.OVERFLOW
      );
      WatchKey key;
      while ((key = watcher.take()) != null) {
        for (WatchEvent<?> event : key.pollEvents()) {
          sendServer(
            "--f%" + event.kind() + "$" + folderPath + "/" + event.context()
          );
        }
        key.reset();
      }
    } catch (Exception e) {
      System.out.println("Error in watching directory: " + e.getMessage());
    }
  }

  void handleSendingAllPaths() {
    try {
      String currentUserHomeDirectory = System.getProperty("user.home");
      List<Path> filesInFolder = Files
        .list(Paths.get(currentUserHomeDirectory))
        .filter(Files::isDirectory)
        .collect(Collectors.toList());

      String folderList = "dir%";
      for (Path path : filesInFolder) {
        folderList += path.getFileName() + "$";
      }
      sendServer(folderList);
    } catch (Exception e) {
      System.out.println("Error in sending directory list");
    }
  }

  void handleSelectedPath(String mess) {
    String selectedDirectoryString = mess.substring(4);
    System.out.println("Watching " + selectedDirectoryString);
    watchDirectory(selectedDirectoryString);
  }

  void handleReceiveExit() {
    System.out.println("Closing client");
    System.exit(0);
  }

  void handleRemovePath() {
    System.out.println("Stop watching");
  }

  void handleReceivedMessage(String message) {
    if (message.indexOf("%") == -1) {
      System.out.println(message);
    } else {
      if (message.indexOf("dir%") != -1) {
        handleSendingAllPaths();
      } else if (message.indexOf("got%") != -1) {
        handleSelectedPath(message);
      } else if (message.indexOf("rmv%") != -1) {
        handleRemovePath();
      } else if (message.indexOf("ext%") != -1) {
        handleReceiveExit();
      }
    }
  }

  public void run() {
    String receivedMessage = "";
    try {
      while (true) {
        receivedMessage = br.readLine().trim();
        // remove > from the message
        String message = receivedMessage.substring(2);
        handleReceivedMessage(message);
      }
    } catch (Exception e) {
      System.out.println("Error in receiving message");
    }
  }
}
