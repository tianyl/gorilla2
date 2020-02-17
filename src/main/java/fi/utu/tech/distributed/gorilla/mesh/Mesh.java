package fi.utu.tech.distributed.gorilla.mesh;

import fi.utu.tech.distributed.gorilla.logic.ChatMessage;
import fi.utu.tech.distributed.gorilla.logic.GameConfiguration;
import fi.utu.tech.distributed.gorilla.logic.MoveThrowBanana;
import fi.utu.tech.distributed.gorilla.logic.Player;
import fi.utu.tech.distributed.gorilla.logic.GameState;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.management.PlatformLoggingMXBean;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Mesh extends Thread {
	
	
    final boolean verboseMode;
    private int yourPort;
    public String yourName;
    private String remoteServerName;
    boolean outwardsConnection = false;
    private Vector<Serializable> moveBuffer;
    // varastoi ja välittää viestejä
    private Vector<Serializable> messageBuffer = new Vector<>();
    // All client names, so we can check for duplicates upon registration.
    protected final Set<String> names = new HashSet<>();
    // The set of all the vectors for all the clients, used for broadcast.
    private final Set<Vector<Serializable>> vectors = new HashSet<>();
	private Vector<Player> otherPlayers;

    public Mesh(final int port, final boolean verboseMode, String name, Vector<Serializable> moveBuffer, Vector<Player> otherPlayers){
        this.verboseMode = verboseMode;
        this.yourPort = port;
        this.yourName = name;
        this.moveBuffer = moveBuffer;
        this.otherPlayers = otherPlayers;
    }
    
    /**
     * Metodi alkaa kuunnella annettua porttia ja käynnistää handler-säikeitä uusille sisääntuleville yhteyksille
     * @throws Exception
     */
    public void startListening() throws Exception {
    	if (verboseMode) System.out.print("Starting the server... ");
    	var pool = Executors.newFixedThreadPool(500);
    	try (var listener = new ServerSocket(yourPort)) {
    		System.out.println("Listening at " + InetAddress.getLocalHost() + ":" + yourPort);
    		System.out.println("Ready to connect. Use command: connect server:port");
    		
    		while (true) {
    			try {
    				pool.execute(new Handler(listener.accept()));
    			} catch (Exception e) {
    				break;
    			}
    		}
    	}
    }    	

    /**
     * Käynnistää ClientHandlerin joka liittyy serveriin
     * @param remoteServerAddress
     * @param remoteServerPort
     */
    public void connectToServer(String remoteServerAddress, int remoteServerPort) {
        System.out.println("Connecting to server at " + remoteServerAddress + " port " + remoteServerPort);
		Socket sucket;
		try {
			sucket = new Socket(remoteServerAddress, remoteServerPort);
			new ClientHandler(sucket).run();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    // sends message to broadcast method
    public void sendObject(ChatMessage msg) {
    	// print for yourself
    	System.out.println(msg.sender + ": " + msg.contents);
        broadcast(msg);

    }

    public void sendObject(MoveThrowBanana mtb){
        broadcast(mtb);
    }
    
    public void sendObject(Player plo) {
    	broadcast(plo);
    }

    public void sendObject(String blayer){
        broadcast(blayer);
    }
    
    public void sendObject(GameConfiguration conffi){
        broadcast(conffi);
    }
    
    // adds message or other serializable object to client vectors
    void broadcast(final Serializable msg) {
        if(!vectors.isEmpty()) {
        	if(!messageBuffer.contains(msg)) {
        		messageBuffer.add(msg);
		    	synchronized (vectors) {
		            for (Vector<Serializable> vector : vectors) vector.add(msg);
		        }
        	}
        if (verboseMode) System.out.println("Broadcasting");
        }
    }

    // lisätään uuden yhteyden tiedot
     boolean add(final String name, final Vector<Serializable> v) {
        synchronized (vectors) {
            if (name.isBlank() || names.contains(name)) return false;
            names.add(name);
            vectors.add(v);
        }
        return true;
    }
    
     //poistetaan lähtevän clientin tiedot
    void remove(final String name, final Vector<Serializable> vector) {
        synchronized (vectors) {
            if (name != null) names.remove(name);
            if (vector != null) vectors.remove(vector);
        }
    }
    
    /**
     * Välittää tiedon mihin serveriin on yhdistetty
     * @return
     */

    public String getConnectionInfo() {
    	//if(outwardsConnection) {
    		return remoteServerName;
    	//} else return "none";
    }

    public int getPort() {
        return yourPort;
    }
    
    
    // // '* ~ -. HANDLERS .- ~ *' // //
    
    /**
     * The server handler task.
     */
    private class Handler implements Runnable {
        private String anonName;
        private String trueName;
        private final Socket socket;
        private Vector<Serializable> sendToClient;
		protected String serverHelloMessage;
        
        /**
         * Constructs a handler thread, squirreling away the socket. All the interesting
         * work is done in the run method. Remember the constructor is called from the
         * server's main method, so this has to be as short as possible.
         */
        public Handler(final Socket socket) {
        	this.anonName = "anon" + (new Random().nextInt(8999) + 1000);
        	this.trueName = anonName;
            this.socket = socket;
            this.sendToClient = new Vector<Serializable>();
            serverHelloMessage ="tervetuloa serverilleni";
        }

        /**
         * Services this thread's client and registers 
         *  the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        public void run() {
            try {
                // ulospäin lähtevän liikenteen säie
                new Thread(() -> { 
	                try (OutputStream os = socket.getOutputStream()){
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
                    objectOutputStream.writeObject(new ChatMessage(yourName, InetAddress.getLocalHost() + ":" + yourPort, serverHelloMessage));
                    
		            while (true) { 

		                    if (sendToClient.size() > 0) {
		                        objectOutputStream.writeObject(sendToClient.firstElement());
		                        sendToClient.remove(0);
		                    }
		            }
		                } catch (Exception e) {System.out.println("virhe"); 
		                e.printStackTrace();}
                }).start();
            	
                // sisäänpäin tulevan liikenteen säie
                new Thread(() -> {
                	try(InputStream is = socket.getInputStream();){
                		ObjectInputStream objectInputStream = new ObjectInputStream(is);
                		while (true) {
                            try {
                            	Object object = objectInputStream.readObject();
                                if (object instanceof ChatMessage) {
                                	ChatMessage thisMessage = (ChatMessage)object;
                                	handleChatMessage(thisMessage);
                                	if ((thisMessage.contents).equals("joined") || (thisMessage.contents).equals("tervetuloa serverilleni")) {
                                		remoteServerName = thisMessage.recipient;
                                		handleAddPlayer(thisMessage.sender);
                                	}
                                }
                                if (object instanceof MoveThrowBanana) handleThrowBanana((MoveThrowBanana)object);
                                if (object instanceof String) handleAddPlayer((String) object);
                            	if (object instanceof Player) {
                            		handleAddPlayer((Player) object);
                            		if (verboseMode) System.out.println("received a player");
                            	}
                            	if (object instanceof GameConfiguration) handleGameConfiguration((GameConfiguration)object);
                            } catch (Exception e) {
                                System.out.println("Connection forcefully terminated by client");
                                remove(anonName, sendToClient);
                                System.out.println(trueName + " is leaving");
                                System.out.println(new ChatMessage(yourName, "all", trueName + " ei ole enää keskuudessamme. Miau!"));
                                break;
                            }
                        }
                	} catch (Exception e) {
                	}
                }).start();
            } catch (Exception e) {
                System.out.println("yhteysvirhe");
                e.printStackTrace();
            }
        }
        private void handleChatMessage(ChatMessage incomingMessage) {
        	System.out.println(incomingMessage.sender + ": " +incomingMessage.contents);
        	if (!names.contains(anonName)) { // tarkistetaan onko tunnus jo listassa
        		add(anonName, sendToClient); // lisätään tunnus listaan
        		trueName = incomingMessage.sender; // vaihdetaan nimi lähettäjän oikeaksi nimeksi
        		// TODO: lisää pelaaja
        		otherPlayers.add(new Player(trueName, new LinkedBlockingQueue<>(), false));
        	}
        	// tähän viestin edelleenlähetys
        	// broadcast()
        }

        private void handleThrowBanana(MoveThrowBanana incomingMove){
            //incomingMove.playerName = anonName;
            moveBuffer.add(incomingMove);
        }

        private void handleAddPlayer(Player playa){
            moveBuffer.add(playa);
        }
        
        private void handleAddPlayer(String ployer) {
        	moveBuffer.add(new Player(ployer, new LinkedBlockingQueue<>(), false));
        }

        private void handleGameConfiguration(GameConfiguration gamecong){
            moveBuffer.add(gamecong);
        }
    }
    
    /**
     * Perii perus handler-luokan. käytetään kun otetaan yhteys toiseen serveriin
     * @author utu
     *
     */
    private class ClientHandler extends Handler {
    	
    	public ClientHandler(final Socket socket) {
    		super(socket);
    		super.serverHelloMessage = "joined";
    	}
    }
}