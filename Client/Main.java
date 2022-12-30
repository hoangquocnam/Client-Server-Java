import java.io.*;
import java.net.*;

public class Main implements Runnable {

  static Socket clientSocket;
  static InputStream is;
  static OutputStream os;
  static BufferedReader br;
  static BufferedWriter bw;
  private static BufferedReader inputLine = null;
  private static boolean isClosed = false;

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
          bw.write(sentMessage);
          bw.newLine();
          bw.flush();
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
        receivedMessage = br.readLine();
        System.out.println(receivedMessage);
        if (receivedMessage == null) {
          break;
        }
      }
    } catch (Exception e) {
      System.out.println("Error in receiving message");
    }
  }
}