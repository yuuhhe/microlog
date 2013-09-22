package net.sf.microlog.server.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 */
public class SocketLogServer implements Runnable {

	private Thread serverThread;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SocketLogServer logServer = new SocketLogServer();
		logServer.startServer();
	}

	/**
	 * Start the server.
	 */
	public void startServer() {
		if (serverThread == null) {
			serverThread = new Thread(this);
		}

		if (!serverThread.isAlive()) {
			serverThread.start();
		}
	}

	public void run() {
		ServerSocket serverSocket = null;
		try {
			System.out.println("Creating server socket.");
			serverSocket = new ServerSocket(1234);
			System.out.println("ServerSocket is created.");
//			int port = 443;
//	        ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
//	        serverSocket = ssocketFactory.createServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: "+e.getMessage());
			throw new RuntimeException("Could not listen on port: "+e.getMessage());
		}

		Socket clientSocket = null;
		try {
			System.err.println("Waiting for client to connect.");
			clientSocket = serverSocket.accept();
			System.err.println("Client is now connected.");
		} catch (IOException e) {
			System.err.println("Accept failed: "+e.getMessage());
			throw new RuntimeException("Failed to connect to the client");
		}

		try {
			DataInputStream dataInputStream = new DataInputStream(clientSocket
					.getInputStream());

			String inputLine = dataInputStream.readUTF();

			System.out.println("Start to read the input from the client.");
			while ((inputLine = dataInputStream.readUTF()) != null) {
				System.out.println(inputLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Client has disconnected. Closing down the server.");
		}

	}
}

