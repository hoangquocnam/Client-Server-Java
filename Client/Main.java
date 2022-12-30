import java.io.*;
import java.net.*;
import javax.swing.*;

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
        if (receivedMessage.equals("> :dir")) {
          JFileChooser chooser = new JFileChooser();
          chooser.setCurrentDirectory(new java.io.File("."));

          chooser.setDialogTitle("Select a directory");
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          chooser.setAcceptAllFileFilterUsed(false);
          if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selectedDirectoryString = chooser.getSelectedFile().toString();
            sendServer(selectedDirectoryString);
          } else {
            System.out.println("No Selection ");
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Error in receiving message");
    }
  }
}
