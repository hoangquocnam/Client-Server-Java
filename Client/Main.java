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
    String receivedMessage = "";
    try {
      while (true) {
        receivedMessage = br.readLine().trim();
        if (receivedMessage.indexOf("%") == -1) {
          System.out.println(receivedMessage);
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
          String selectedDirectoryString = receivedMessage.substring(6);
          watchDirectory(selectedDirectoryString);
        } else if (receivedMessage.indexOf("> remove%") != -1) {
          String[] messageParts = receivedMessage.substring(6).split("\\$");
          String eventKind = messageParts[0];
          String fileName = messageParts[1];
          System.out.println("File " + fileName + " has been " + eventKind);
        } else if (receivedMessage.indexOf("> exit") != -1) {
          System.exit(0);
        }
      }
    } catch (Exception e) {
      System.out.println("Error in receiving message");
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
          sendServer("--f%" + event.kind() + "$" + event.context());
        }
        key.reset();
      }
    } catch (Exception e) {
      System.out.println("Error in watching directory: " + e.getMessage());
    }
  }
}
