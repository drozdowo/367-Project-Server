import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

class ServerSideConnection implements Runnable {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private static int THREAD_DELAY = 50; //msec
    private int pid;
    private String playerName;
    private Server server;
    private Pokemon pokemon;

    public ServerSideConnection(Socket s, int pid, Server server) {
        this.socket = s;
        this.pid = pid;
        try {
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
            this.server = server;
        } catch (IOException ex) { System.out.println("IOException from ssc.");	}
    }

    public void run() {
        try {
        while (true){
            if (this.input.available() > 0){ //data available
                byte[] buffer = new byte[120120];
                int len = this.input.available();
                this.input.read(buffer, 0, this.input.available());
                this.server.onMessageFromPlayer(new String(buffer, 0, len), this);
            }
        }
    } catch (Exception e){
        e.printStackTrace();
    }
    }

    public void sendMessageToPlayer(String msg){
        try {
            this.output.write(msg.getBytes());
            this.output.flush();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void sendPokemonListToPlayers(ArrayList<Pokemon> list){
        try {
            ObjectOutputStream oos = new ObjectOutputStream(this.output);
            oos.writeObject(list);
            oos.flush();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setPlayerName(String playerName){
        this.playerName = playerName;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    public int getPlayerID(){
        return this.pid;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }
}