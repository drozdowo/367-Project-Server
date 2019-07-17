import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {
	private ServerSocket socket;
	private int playerCount; //limiting to 2

	private ServerSideConnection playerOne;
	private ServerSideConnection playerTwo;

	//default constructor
	public Server() {
		System.out.println("----------------------\n\tServer\n----------------------");
		playerCount = 0;

		try {
			socket = new ServerSocket(51467);
		} catch (IOException ex) { System.out.println("IO Exception from default constructor."); }
	}

	public void acceptConnection() {
		try {
			System.out.println("Awaiting connection...");
			while (playerCount < 2) {
				Socket s = socket.accept();
				playerCount++;
				System.out.println("Player #" + playerCount + " connected.");
				ServerSideConnection ssc = new ServerSideConnection(s, playerCount);

				// assign correct ssc to correct field.
				if (playerCount == 1)
					playerOne = ssc;
				else
					playerTwo = ssc;
				Thread thread = new Thread(ssc);
				thread.start();
			}
			System.out.println(playerCount + " player(s) joined.");
		} catch (IOException ex) {  System.out.println("IO Exception from acceptConnection()."); }
	}

	private class ServerSideConnection implements Runnable {
		private Socket socket;
		private DataInputStream input;
		private DataOutputStream output;
		private int pid; //i know.

		public ServerSideConnection(Socket s, int pid) {
			this.socket = s;
			this.pid = pid;
			try {
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
			} catch (IOException ex) { System.out.println("IOException from ssc.");	}
		}

		public void run() {
			try {
				while (true) {
					if (pid == 1) {
						System.out.println("PID 1 Connected");
					}
				}
			} catch (Exception e) { System.out.println("Exception from run(), which is in ssc."); }
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.acceptConnection();
	}



}
