import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    try {
       serverSocket = new ServerSocket(4221);
    
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
    
      //serverSocket.accept(); // Wait for connection from client.
      clientSocket = serverSocket.accept();
      System.out.println("accepted new connection");

      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      String requestLine = in.readLine();
      if (requestLine != null && !requestLine.isEmpty()) {
        String[] requestParts = requestLine.split(" ");
        System.out.println(requestLine);
        // if (requestParts[1].equals("/")) {
        //   clientSocket.getOutputStream().write(
        //   "HTTP/1.1 200 OK\r\n\r\n".getBytes());
        // }else{
        //   clientSocket.getOutputStream().write(
        //   "HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        // }
        if (requestParts[1].startsWith("/echo/")){
          String message = requestParts[1].substring("/echo/".length());
          String contentType = "text/plain"; // Specify the type of content
          int contentLength = message.length(); // Get the length of the response body
          System.out.println(message);
            // Build HTTP response with headers
            String httpResponse = 
              "HTTP/1.1 200 OK\r\n" +
              "Content-Type: " + contentType + "\r\n" +
              "Content-Length: " + contentLength + "\r\n" +
              "\r\n" + 
              message;
  
            clientSocket.getOutputStream().write(
            httpResponse.getBytes());
        }else{
          clientSocket.getOutputStream().write(
          "HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        }

      }
      
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
