/**
 *   SSL HTTP Server, Multi Threaded
 *   Usage:  java ThreadHTTPSServer sslkeystore sslsocketkey [port#  [http_root_path]]
 *	 
 *   UTORid: seodonal
 *	 Name: Deokjae Seo
 *	 Student number: 997832494
 *
 **/

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.net.ssl.*;

class HTTPThread implements Runnable {

	private Socket connectionSocket = null;
	private String http_root_path = null;

    // constructor to instantiate the HTTPThread object
    public HTTPThread(Socket connectionSocket, String http_root_path) {
    	this.connectionSocket = connectionSocket;
    	this.http_root_path = http_root_path;
    }

    public void run() {
	// invoke processRequest() to process the client request and then generateResponse()
	// to output the response message
    	try {
    		processRequest(connectionSocket);

    	} catch (Exception e) {
    		System.err.println("could not process request " + e);
    	}
    } //end of run 

    private void processRequest(Socket connectionSocket) throws Exception {
	// same as in single-threaded (this code is inline in the starter code)
    	// create buffered reader for client input
		BufferedReader inFromClient = 
             new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));



		String requestLine = null;	// the HTTP request line
		String requestHeader = null;	// HTTP request header line

		/* Read the HTTP request line and display it on Server stdout.
		 * We will handle the request line below, but first, read and
		 * print to stdout any request headers (which we will ignore).
		 */

		//readline from client
		requestLine = inFromClient.readLine();
		//if null , keep continue
		while (requestLine == null)
			continue;
		
		//readline from client
		requestHeader = inFromClient.readLine();

		

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
		else {
		    System.out.println("Bad Request Message");
		}
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

	    // generate HTTP response line; output to stdout
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
} // end of HTTPThread

public final class ThreadHTTPSServer {
	public static int serverPort = 0;    // default port 
    public static String http_root_path = null;    // http_root_path
    public static String keystore = "";		//sslkeystore password
    public static String keypass = "";		//sslsocketkey password
    
    public static void main(String args[]) throws Exception  {


   	SSLServerSocket welcomeSocket = null; // inital server-socket with SSL
	SSLSocket connectionSocket = null;    // connection Socket

  	// process command-line options
	// assign user command input to keystore, keypass, serverPort and http_root_path
	if (args.length < 2 || args.length > 4) {
	    System.out.println("usage: java ThreadHTTPSServer sslkeystore sslsocketkey [port_# [http_root_path]]");
	    System.exit(0);
	}

	if (args.length >= 2) {
		keystore = args[0];
		keypass = args[1];
	}

	if (args.length >= 3) {
		serverPort = Integer.parseInt(args[2]);
	}

	if (args.length == 4) { 
		http_root_path = args[3];
	}

	char kpass[] = keystore.toCharArray();
	char cpass[] = keypass.toCharArray();
	String kname = "httpserverkeys";

	//creating new SSL server socket and try connecting it to serverPort   
	try {

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(kname), kpass);

		KeyManagerFactory keymf = KeyManagerFactory.getInstance("SunX509");
		keymf.init(ks, cpass);

		SSLContext sslcontext = SSLContext.getInstance("SSLv3");
		sslcontext.init(keymf.getKeyManagers(), null, null);


		SSLServerSocketFactory sslsocf = sslcontext.getServerSocketFactory();
		welcomeSocket = (SSLServerSocket) sslsocf.createServerSocket(serverPort);
		
	} catch (IOException e) {
		System.err.println("couldn't start server:" + e);
		System.exit(0);
	} 
	// display server stdout message indicating listening
	System.out.println("\n" + "Listening on port " + serverPort + " with path " + http_root_path + "\n"); 
	
	// on port # with server root path ..
	
	//enabled protocols and cipher suites in arrays
	String [] protocols = welcomeSocket.getEnabledProtocols();
	String [] ciphersuites = welcomeSocket.getEnabledCipherSuites();

	welcomeSocket.setEnabledCipherSuites(ciphersuites);
	//print enabled protocols and cipher suites
	System.out.println("### Enabled Protocols: " + Arrays.asList(protocols));
	System.out.println("### Enabled Cipher Suites: " + Arrays.asList(ciphersuites));

	

	while (true) {
	    // accept a connection
	    try {

		    try {
			// take a waiting connection from the accepted queue 
			    connectionSocket = (SSLSocket) welcomeSocket.accept();
			} catch (Exception e) {
				System.err.println("socket connection error: " + e);
			}

			// display on server stdout the request origin
			System.out.println("\n" + "Connection from " 
					+ connectionSocket.getInetAddress() + "." 
					+ connectionSocket.getPort() + "\n");  

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
