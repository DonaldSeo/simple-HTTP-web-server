/**
 *   HTTP Server, Multi Threaded
 *   Usage:  java ThreadHTTPServer [port#  [http_root_path]]
 *  
 *   UTORid: seodonal
 *  Name: Deokjae Seo
 *  Student number: 997832494
 *
 **/

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;


class HTTPThread implements Runnable {

 private Socket connectionSocket = null;
 private String http_root_path = null;
 private boolean close_conn;

    // constructor to instantiate the HTTPThread object
    public HTTPThread(Socket connectionSocket, String http_root_path) {
     this.connectionSocket = connectionSocket;
     this.http_root_path = http_root_path;
     this.close_conn = false;
    }

    public void run() {
 // invoke processRequest() to process the client request and then generateResponse()
 // to output the response message
     try {
      processRequest(connectionSocket);
     } catch (SocketTimeoutException toe) {
      System.err.println(toe.getMessage());
      // when we get Socket time out exception, close connection
      try {
       connectionSocket.close();
      } catch (IOException ioe) {
       System.err.println("IOException" + ioe);
      }

     } catch (Exception e) {
      System.err.println("could not process request " + e);
     }
     

    } //end of run 

    private void processRequest(Socket connectionSocket) throws Exception {
 // same as in single-threaded (this code is inline in the starter code)
     // create buffered reader for client input
  BufferedReader inFromClient = 
             new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));



  String requestLine = null; // the HTTP request line
  String requestHeader = null; // HTTP request header line

  /* Read the HTTP request line and display it on Server stdout.
   * We will handle the request line below, but first, read and
   * print to stdout any request headers (which we will ignore).
   */
  //readline from client


  while ((requestLine = inFromClient.readLine()) != null && !connectionSocket.isClosed() && !close_conn){
   //initial first close flag is true
   close_conn = true;
   //readHeader from client
   requestHeader = inFromClient.readLine();
   
   while (!requestHeader.equals("")) {
    System.out.println("Request header: " + requestHeader);
    if (requestHeader.toLowerCase().equals("connection: keep-alive")) {
     close_conn = false; //set to false when we get "connection: keep-alive"
    }
    else if(requestHeader.toLowerCase().equals("connection: close")) {
     close_conn = true;
    }
    requestHeader = inFromClient.readLine();
   }
   
   // now back to the request line; tokenize the request
   StringTokenizer tokenizedLine = new StringTokenizer(requestLine);
   // process the request
   if (tokenizedLine.nextToken().equals("GET")) {
       String urlName = null;     
       // parse URL to retrieve file name
       urlName = tokenizedLine.nextToken();
      
       if (urlName.startsWith("/") == true )
    urlName  = urlName.substring(1);

       // if false, then generate response and set timeout
    if (!close_conn){
        generateResponse(urlName, connectionSocket); 
        //set timeout for 10000 sec
        connectionSocket.setSoTimeout(7000);
    }
	//if close_conn is true, generate response and close connection right away
    else if (close_conn){
     generateResponse(urlName, connectionSocket); 
     connectionSocket.close();
    }

   } 
   else {
       System.out.println("Bad Request Message");
   }

  }
  //close connection after all request processed
  connectionSocket.close();
 } //end of processRequest
 
    

    private void generateResponse(String urlName, Socket connectionSocket) throws Exception 
    {
 // create an output stream
    DataOutputStream  outToClient = 
     new DataOutputStream(connectionSocket.getOutputStream());
   
    // link file loc with path + urlname
 String fileLoc = http_root_path + urlName;  
 System.out.println ("\n" + "Request Line: GET " + fileLoc + "\n");

 // create a file with the location
 File file = new File( fileLoc );
 if (!file.isFile())
 {

     // generate 404 File Not Found response header
     outToClient.writeBytes("HTTP/1.0 404 File Not Found\r\n");
     // and output a copy to server's stdout
     System.out.println ("HTTP/1.0 404 File Not Found\r\n");
 } else {
     // get the requested file content
     int numOfBytes = (int) file.length();
     
     FileInputStream inFile  = new FileInputStream (fileLoc);
 
     byte[] fileInBytes = new byte[numOfBytes];
     inFile.read(fileInBytes);
  
  // get date
  SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
  Date date = new Date(file.lastModified());


     // generate HTTP response line; output to stdout
     outToClient.writeBytes("HTTP/1.0 200 OK\r\n" +
                   "Content-Type: " + ContentType(fileLoc) + "\r\n" +
                   "Content-Length: " + numOfBytes + "\r\n" +
                   "Date: " + new Date() + "\r\n" + 
                   "Last-Modified: " + dateFormat.format(date) + "\r\n" + 
                   checkConnection(close_conn) + "\r\n");

     System.out.println("Response line: HTTP/1.0 200 OK\r\n" +
                   "Response line: Content-Type: " + ContentType(fileLoc) + "\r\n" +
                   "Response line: Content-Length: " + numOfBytes + "\r\n" +
                   "Response line: Date: " + new Date() + "\r\n" + 
                   "Response line: Last-Modified: " + dateFormat.format(date) + "\r\n" + 
                   checkConnection(close_conn) + "\r\n");
     outToClient.writeBytes("\r\n");
     // send file content
     outToClient.write(fileInBytes, 0, numOfBytes);
  
  }  // end else 
  // after generating responses, connection will be closed if keep-alive is false.

 
    } // end of generateResponse

    private static String ContentType(String path)
    {
     //printing out different types of contents in HTTP response line

        if (path.endsWith(".html") || path.endsWith(".htm")){
            return "text/html";
        } 
        else if (path.endsWith(".gif")){
            return "image/gif";
        } 
        else if (path.endsWith(".txt")){
            return "text/plain";
        } 
        else if (path.endsWith(".jpg") || path.endsWith(".jpeg")){
            return "image/jpeg";
        }
        else if (path.endsWith(".css")){
            return "text/css";
        }
        else{
            return "text/plain";
        }    
    } // end of ContentType

    private static String checkConnection(boolean close_conn)
    {
     if (!close_conn) {
      return "Response line: connection: keep-alive";
     } else {
      return "";
     }
     
    }
} // end of HTTPThread

public final class ThreadPipeHTTPServer {
 public static int serverPort = 0;    // default port 
    public static String http_root_path = null;    // http_root_path
    
    public static void main(String args[]) throws Exception  {

     // process command-line options
 if (args.length > 2) {
     System.out.println("usage: java ThreadHTTPServer [port_# [http_root_path]]");
     System.exit(0);
 }

 // assign user command input to serverPort and http_root_path
 serverPort = Integer.parseInt(args[0]);
 http_root_path = args[1];
     
 ServerSocket welcomeSocket = null; // inital server-socket
 // create server socket 
 try {
  welcomeSocket = new ServerSocket(serverPort);
 } catch (IOException e) {
  System.err.println("couldn't start server:" + e);
  System.exit(1);
 }
 // display server stdout message indicating listening
 System.out.println("\n" + "Listening on port " + serverPort + " with path " + http_root_path + "\n"); 
 // on port # with server root path ..

 
 Socket connectionSocket = null;    // connection Socket

 while (true) {
     // accept a connection
     try {

      try {
   // take a waiting connection from the accepted queue 
       connectionSocket = welcomeSocket.accept();
   } catch (IOException e) {
    System.err.println("socket connection error: " + e);
    System.exit(1);
   }

      // Construct an HTTPThread object to process the accepted connection
   HTTPThread httpthread = new HTTPThread(connectionSocket, http_root_path);
   
      // Wrap the HTTPThread in a Thread object
      Thread thread = new Thread(httpthread);

      // Start the thread.
      thread.start();

      } catch (Exception e) {
       System.err.println("thread error: " + e);
   }

  } //end of while loop accepting connection
 
    } //end of main

} //end of HTTPThreadServer
