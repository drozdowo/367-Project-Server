import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

class Server {
	private ServerSocket socket;
	private int playerCount; //limiting to 2

	private ServerSideConnection playerOne;
	private ServerSideConnection playerTwo;
	private int roundCount = 0;

	private enum SERVER_STATE {WAITING_ON_PLAYERS, START, PLAYER_1_TURN, PLAYER_2_TURN, CALCULATING, END}
	private SERVER_STATE myState;

	//default constructor
	public Server() {
		System.out.println("----------------------\n\tServer\n----------------------");
		playerCount = 0;
		this.myState = SERVER_STATE.WAITING_ON_PLAYERS;
		try {
			socket = new ServerSocket(25565);
		} catch (IOException ex) { System.out.println("IO Exception from default constructor."); }
	}

	private void broadcastMessageToAllPlayers(String msg){
		try {
			this.playerTwo.sendMessageToPlayer(msg);
			this.playerOne.sendMessageToPlayer(msg);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//Called when all players ready.
	private void mainGameLoop(){
		while (true) {

		}
	}

	private void stateHandler(String message, String option, ServerSideConnection scc){
		//here we'll handle the state transitions based on our current state
		if (this.myState == SERVER_STATE.WAITING_ON_PLAYERS){
			//if we're waiting on players...
			if (message.equals("ALL_READY")){
				//Waiting on players and we're all ready...
				this.myState = SERVER_STATE.START;
				this.broadcastMessageToAllPlayers("ALL_READY -1");
				this.randomizePlayerTurn();
				return;
			}
		}
		if(this.myState == SERVER_STATE.START){
			if (message.equals("PLAYER_1_TURN") && this.playerOne == scc){
				this.myState = SERVER_STATE.PLAYER_1_TURN;
				return;
			} else {
				this.myState = SERVER_STATE.PLAYER_2_TURN;
				return;
			}
		}
		if (this.myState == SERVER_STATE.PLAYER_1_TURN){
			if (message.equals("SEND_TURN") && this.playerOne == scc){
				System.out.println("Player 1 move: " + option);
			} else {
				System.out.println("The fuck");
			}
		}
		if (this.myState == SERVER_STATE.PLAYER_2_TURN){

		}
		if (this.myState == SERVER_STATE.CALCULATING){

		}
		if (this.myState == SERVER_STATE.END){

		}
		System.out.println("|||StateHandler|||");
		System.out.println("curState: " + this.myState + " | Input: " + message + " " + option);
	}

	private void randomizePlayerTurn(){
		Random rand = new Random();
		int num = rand.nextInt(1);
		if (num == 0){
			this.playerOne.sendMessageToPlayer("YOUR_TURN " + ++roundCount);
			this.stateHandler("PLAYER_1_TURN", "", this.playerOne);
		} else {
			this.playerTwo.sendMessageToPlayer("YOUR_TURN " + ++roundCount);
			this.stateHandler("PLAYER_2_TURN", "", this.playerTwo);
		}
	}

	public void onMessageFromPlayer(String msg, ServerSideConnection scc){
		//Whenever we get a message from a player, we'll decode it here.
		//Messages from players will come in very similar to the client
		//But it will be identified by the Socket/ServerSideConnection that it
		//came from:
		String message = msg.split(" ")[0];
		String option = msg.split(" ")[1];
		if (message.equals("NAME_REG")) { //name registration
			scc.setPlayerName(option);
			scc.sendMessageToPlayer("NAME_ACCEPT " + option);
		} else {
			stateHandler(message, option, scc);
		}
	}


	public void acceptConnections() throws InterruptedException{
		try {
			System.out.println("Awaiting connection...");
			while (playerCount < 2) {
				Socket s = socket.accept();
				playerCount++;
				System.out.println("Player #" + playerCount + " connected.");
				ServerSideConnection ssc = new ServerSideConnection(s, playerCount, this);

				// assign correct ssc to correct field.
				if (playerCount == 1)
					playerOne = ssc;
				else
					playerTwo = ssc;
				Thread thread = new Thread(ssc);
				thread.start();
			}
			System.out.println(playerCount + " player(s) joined.");
			Thread.sleep(1000); 	//Wait one second to start the game to ensure
										//client caught the NAME_ACCEPT
			stateHandler("ALL_READY", "-1", null); //Use main thread to sit in the game loop, separate threads will
		} catch (IOException ex) {  System.out.println("IO Exception from acceptConnection()."); }
	}

	public static void main(String[] args) {
		Server server = new Server();
		try {
			server.acceptConnections();
		} catch (Exception e){
			e.printStackTrace();
		}
	}



}
