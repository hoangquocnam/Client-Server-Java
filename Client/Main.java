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
  static String selectedDirectoryString = "";
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
      is = clientSocket.getInputStream();
      os = clientSocket.getOutputStream();
      br = new BufferedReader(new InputStreamReader(is));
      bw = new BufferedWriter(new OutputStreamWriter(os));
      inputLine = new BufferedReader(new InputStreamReader(System.in));

      String sentMessage = "";

      try {
        // create a new thread to receive messages from the server
        Thread t = new Thread(new Main());
        t.start();
        while (true) {
          sentMessage = inputLine.readLine();
          sendServer(sentMessage);
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

  public void run() {
    // create a new thread to receive messages from the server
    String receivedMessage = "";
    try {
      while (true) {
        receivedMessage = br.readLine().trim();
        System.out.println(receivedMessage);
        if (receivedMessage == null) {
          break;
        }
        if (receivedMessage.equals("> dir%")) {
          // send the root directory
          String currentUserHomeDirectory = System.getProperty("user.home");
          // get folder list from the root directory
          List<Path> filesInFolder = Files
            .list(Paths.get(currentUserHomeDirectory))
            .filter(Files::isDirectory)
            .collect(Collectors.toList());

          String folderList = "dir%";
          for (Path path : filesInFolder) {
            folderList += path.getFileName() + "$";
          }
          sendServer(folderList);
        } else if (receivedMessage.indexOf("> got%") != -1) {
          // get the selected directory
          // remove > got%

          selectedDirectoryString = receivedMessage.substring(6);
          // watch the selected directory
          watchDirectory(selectedDirectoryString);
        }
      }
    } catch (Exception e) {
      System.out.println("Error in receiving message");
    }
  }

  // watch the selected directory
  public static void watchDirectory(String folderPath) {
    try {
      WatchService watcher = FileSystems.getDefault().newWatchService();
      // need to register the directory to watch, the folderPath need to plus with the root directory
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
          sendServer("--f%" + event.kind() + "$" + event.context());
        }
        key.reset();
      }
    } catch (Exception e) {
      System.out.println("Error in watching directory: " + e.getMessage());
    }
  }
}
