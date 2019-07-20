import java.io.*;
import java.net.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.sql.Connection;

import org.sqlite.SQLiteConnection;

class Server {
	private ServerSocket socket;
	private int playerCount; //limiting to 2
	private ArrayList<Pokemon> pokemonList;

	private ServerSideConnection playerOne;
	private ServerSideConnection playerTwo;
	private int roundCount = 0;
	private boolean calculateBool = false;
	private Connection dbconn;

	private enum SERVER_STATE {WAITING_ON_PLAYERS, START, PLAYER_1_TURN, PLAYER_2_TURN, CALCULATING, END}
	private SERVER_STATE myState;

	//default constructor
	public Server() {
		System.out.println("----------------------\n\tServer\n----------------------");
		connectToDatabase();
		playerCount = 0;
		this.myState = SERVER_STATE.WAITING_ON_PLAYERS;
		try {
			socket = new ServerSocket(25565);
		} catch (IOException ex) { System.out.println("IO Exception from default constructor."); }
	}

	private void connectToDatabase(){
		System.out.println("Initializing Database...");
		try {
			dbconn = DriverManager.getConnection("jdbc:sqlite:pokemon.db");
			ResultSet pokemon = dbconn.prepareStatement("SELECT * FROM pokemon").executeQuery();
			this.pokemonList = new ArrayList<Pokemon>();
			while (pokemon.next()){
				Pokemon temp = new Pokemon(pokemon.getInt(1), pokemon.getString(2), pokemon.getString(3));
				this.pokemonList.add(temp);
			}
		} catch (SQLException e){
			System.out.println("Caught SQLException!");
			e.printStackTrace();
			System.exit(1);
		}
	}


	private void broadcastMessageToAllPlayers(String msg){
		try {
			this.playerTwo.sendMessageToPlayer(msg);
			this.playerOne.sendMessageToPlayer(msg);
		} catch (Exception e){
			e.printStackTrace();
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
				this.myState = SERVER_STATE.PLAYER_2_TURN;
				playerTwoTurn();
				return;
			}
		}
		if (this.myState == SERVER_STATE.PLAYER_2_TURN){
			if (message.equals("SEND_TURN") && this.playerTwo == scc){
				if (this.calculateBool){
					this.myState = SERVER_STATE.CALCULATING;

				}
				this.myState = SERVER_STATE.PLAYER_1_TURN;
				playerOneTurn();
				return;
			}//add if statement for if the message is other players turn
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
			playerOneTurn();
		} else {
			playerTwoTurn();
		}
	}

	private void performCalculations() {

	}

	private void playerOneTurn(){
		this.playerOne.sendMessageToPlayer("YOUR_TURN " + ++roundCount);
		this.stateHandler("PLAYER_1_TURN", "", this.playerOne);
	}

	private void playerTwoTurn(){
		this.playerTwo.sendMessageToPlayer("YOUR_TURN " + ++roundCount);
		this.stateHandler("PLAYER_2_TURN", "", this.playerTwo);
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
			sendPokemonToUsers(); //Send them the pokemon so they can make their teams.
		} catch (IOException ex) {  System.out.println("IO Exception from acceptConnection()."); }
	}

	private void sendPokemonToUsers(){
		this.playerOne.sendPokemonListToPlayers(this.pokemonList);
		this.playerTwo.sendPokemonListToPlayers(this.pokemonList);
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
