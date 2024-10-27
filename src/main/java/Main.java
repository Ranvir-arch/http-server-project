import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

class ClientHandler implements Runnable {
  private final Socket clientSocket;
  private final String directory;

  public ClientHandler(Socket clientSocket, String directory) {
    this.clientSocket = clientSocket;
    this.directory = directory;
  }

  @Override
  public void run() {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      String requestLine = in.readLine();
      if (requestLine != null && !requestLine.isEmpty()) {
        System.out.println(requestLine);
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
          String[] headerParts = headerLine.split(": ", 2);
          if (headerParts.length == 2) {
            headers.put(headerParts[0], headerParts[1]);
          }
        }
        //reading body
        String bodyLine = "";
        if(headers.containsKey("Content-Length")){
          int len = Integer.parseInt(headers.get("Content-Length"));
          char[] body = new char[len];
          in.read(body, 0, len);
          bodyLine = new String(body); 
        }
        String message = "";

        if(headers.containsKey("Accept-Encoding")){
          String encoding = headers.get("Accept-Encoding");
          if(encoding.equals("gzip")){
            clientSocket.getOutputStream().write(
            "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Encoding: gzip\r\n\r\n".getBytes());
          }else{
            clientSocket.getOutputStream().write(
            "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n".getBytes());
          }
        }

        if (headers.containsKey("User-Agent")) {
          message = headers.get("User-Agent");

        } else {
          String[] requestParts = requestLine.split(" ");

          if (requestParts[1].startsWith("/echo/")) {
            message = requestParts[1].substring("/echo/".length());
          } else if (requestParts[1].equals("/")) {
            message = "000";
          } else if (requestParts[1].startsWith("/files/")) {
            String fileName = requestParts[1].substring("/files/".length());
            System.out.println(fileName);
            Path path = Paths.get(directory+fileName);
            message = "111";
            try{
              String fileContent = new String(Files.readAllBytes(path));
              String contentType = "application/octet-stream"; // Adjust content type based on file type
              System.out.println(fileContent);
              String httpResponse = "HTTP/1.1 200 OK\r\n" +
                  "Content-Type: " + contentType + "\r\n" +
                  "Content-Length: " + fileContent.length() + "\r\n" +
                  "\r\n"+fileContent;
              clientSocket.getOutputStream().write(httpResponse.getBytes());
              clientSocket.getOutputStream().flush();
            } catch(IOException e) {
              if(bodyLine != null && !bodyLine.isEmpty()){
                File file = new File(directory+fileName);
                if (file.createNewFile()) {
                  FileWriter fileWriter = new FileWriter(file);
                  fileWriter.write(bodyLine);
                  fileWriter.close();
                }
                clientSocket.getOutputStream().write(
                  "HTTP/1.1 201 Created\r\n\r\n".getBytes());
              }else{
                clientSocket.getOutputStream().write(
                    "HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
              }
            }
          } else {
            message = "XXX";
          }
        }
        if (message.equals("XXX")) {
          clientSocket.getOutputStream().write(
              "HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        } else if (message.equals("000")) {
          clientSocket.getOutputStream().write(
              "HTTP/1.1 200 OK\r\n\r\n".getBytes());
        } else if (message.equals("111")) {
          System.out.println("Done");
        } else {
          String contentType = "text/plain"; // Specify the type of content
          int contentLength = message.length(); // Get the length of the response body
          System.out.println(message);
          // Build HTTP response with headers
          String httpResponse = "HTTP/1.1 200 OK\r\n" +
              "Content-Type: " + contentType + "\r\n" +
              "Content-Length: " + contentLength + "\r\n" +
              "\r\n" +
              message;
          clientSocket.getOutputStream().write(
              httpResponse.getBytes());

        }
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
    String directory="";
    if (args.length > 1 && args[0].equals("--directory")) {
      directory = args[1];
    }
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
          ClientHandler ch = new ClientHandler(clientSocket, directory);
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
