/**
 * HTTP Server, Single Threaded,  starter code.  
 * Usage:  java HTTPServer [port#  [http_root_path]]
 * Utorid : seodonal
 * Name: Deokjae Seo
 * Student Number: 997832494
 **/

import java.io.*;
import java.net.*;
import java.util.*;

public final class HTTPServer {
    public static int serverPort = 0;    // default port CHANGE THIS
    public static String http_root_path = null;    // rooted default path in your mathlab area

   
    


    public static void main(String args[]) throws Exception  {


	// display error on server stdout if usage is incorrect
   	
	if (args.length > 2) {
	    System.out.println("usage: java HTTPServer [port_# [http_root_path]]");
	    System.exit(0);
	}
	// assign args[0], args[1] to be serverPort and http_root_path
	serverPort = Integer.parseInt(args[0]);
	http_root_path = args[1];


	ServerSocket welcomeSocket = null; // inital server-socket
	// create server socket 
	try {
		welcomeSocket = new ServerSocket(serverPort);
	} catch (IOException e) {
		System.err.println("couldn't start server:" + e);
		System.exit(0);
	}

	
	// display server stdout message indicating listening
	System.out.println("\n" + "Listening on port " + serverPort + " with path " + http_root_path + "\n"); 
	// on port # with server root path ..

	
	Socket connectionSocket = null;    // connection Socket

	// server runs continuously
	while (true) {
	    try {
		// take a waiting connection from the accepted queue 
	    connectionSocket = welcomeSocket.accept();

		// display on server stdout the request origin
		System.out.println("\n" + "Connection from " 
				+ connectionSocket.getInetAddress() + "." 
				+ connectionSocket.getPort() + "\n");  


		// create buffered reader for client input
		BufferedReader inFromClient = 
             new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));



		String requestLine = null;	// the HTTP request line
		String requestHeader = null;	// HTTP request header line

		/* Read the HTTP request line and display it on Server stdout.
		 * We will handle the request line below, but first, read and
		 * print to stdout any request headers (which we will ignore).
		 */
		requestLine = inFromClient.readLine();
		requestHeader = inFromClient.readLine();

		if (requestLine == null){
			continue;
		}
		while (!requestHeader.equals("")) {
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
		    
		    generateResponse(urlName, connectionSocket);

		} 
		else 
		    System.out.println("Bad Request Message");
	    } catch (Exception e) {
	    	System.err.println(e.getMessage());

		}
	}  // end while true 
	
    } // end main

    private static void generateResponse(String urlName, Socket connectionSocket) throws Exception
    {
	// create an output stream
    DataOutputStream  outToClient = 
     new DataOutputStream(connectionSocket.getOutputStream());

   
    // assign flie location
	String fileLoc = http_root_path + urlName;  
	System.out.println ("\n" + "Request Line: GET " + fileLoc + "\n");

	// create a new file with fileloc
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

	    // ADD_CODE: generate HTTP response line; output to stdout
	    outToClient.writeBytes("HTTP/1.0 200 OK\r\n" +
                   "Content-Type: " + ContentType(fileLoc) + "\r\n" +
                   "Content-Length: " + numOfBytes + "\r\n" +
                   "Date: " + new Date() + "\r\n");

	    System.out.println("HTTP/1.0 200 OK\r\n" +
                   "Content-Type: " + ContentType(fileLoc) + "\r\n" +
                   "Content-Length: " + numOfBytes + "\r\n" +
                   "Date: " + new Date() + "\r\n");
	
	    outToClient.writeBytes("\r\n");
	    // send file content
	    outToClient.write(fileInBytes, 0, numOfBytes);
	}  // end else (file found case)

	// close connectionSocket
	connectionSocket.close();
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
    

} // end of class HTTPServer
	
