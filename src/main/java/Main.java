import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

class ClientHandler implements Runnable {
  private final Socket clientSocket;

  public ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      String requeString = in.readLine();
      if (requeString != null) {
        String httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
        clientSocket.getOutputStream().write(httpResponse.getBytes());
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      try {
        clientSocket.close(); // Ensure the client socket is closed
      } catch (IOException e) {
        System.out.println("IOException while closing clientSocket: " + e.getMessage());
      }
    }
  }

}


public class Main {

  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    ServerSocket serverSocket = null;
    // Socket clientSocket = null;
    try {
      serverSocket = new ServerSocket(4221);
      serverSocket.setReuseAddress(true);
      while (true) {
        try {
          Socket clientSocket = serverSocket.accept(); // wait for client connection
          ClientHandler ch = new ClientHandler(clientSocket);
          new Thread(ch).start();
        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
