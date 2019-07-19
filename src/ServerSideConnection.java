import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ServerSideConnection implements Runnable {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private static int THREAD_DELAY = 50; //msec
    private int pid;
    private String playerName;
    private Server server;
    private int playerHp;

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
                byte[] buffer = this.input.readNBytes(this.input.available());
                int len = buffer.length;
                this.server.onMessageFromPlayer(new String(buffer, 0, len), this);
            }
            Thread.sleep(THREAD_DELAY);
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

    public void setPlayerName(String playerName){
        this.playerName = playerName;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    public int getPlayerID(){
        return this.pid;
    }
}