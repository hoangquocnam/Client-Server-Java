import java.io.*;
import java.net.*;

public class Client implements Runnable {

  static Socket clientSocket;
  static InputStream is;
  static OutputStream os;
  static BufferedReader br;
  static BufferedWriter bw;
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
      InputStream is = clientSocket.getInputStream();
      OutputStream os = clientSocket.getOutputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

      String sentMessage = "";
      String receivedMessage;

      System.out.println("Talking to Server");

      do {
        DataInputStream din = new DataInputStream(System.in);
        sentMessage = din.readLine();
        bw.write(sentMessage);
        bw.newLine();
        bw.flush();

        if (sentMessage.equalsIgnoreCase("quit")) break; else {
          receivedMessage = br.readLine();
          System.out.println("Received : " + receivedMessage);
        }
      } while (true);

      bw.close();
      br.close();
      clientSocket.close();
    } catch (IOException e) {
      System.out.println("");
    }
  }

  public void run() {
    try {
      while (!isClosed) {
        String receivedMessage = br.readLine();
        System.out.println("Received : " + receivedMessage);
      }
    } catch (IOException e) {
      System.out.println("Error in receiving message");
    }
  }
}
