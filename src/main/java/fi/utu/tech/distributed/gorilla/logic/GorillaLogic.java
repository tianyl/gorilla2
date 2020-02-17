package fi.utu.tech.distributed.gorilla.logic;

import fi.utu.tech.distributed.gorilla.views.MainCanvas;
import fi.utu.tech.distributed.gorilla.views.Views;
import fi.utu.tech.oomkit.app.AppConfiguration;
import fi.utu.tech.oomkit.app.GraphicalAppLogic;
import fi.utu.tech.oomkit.canvas.Canvas;
import fi.utu.tech.oomkit.util.Console;
import fi.utu.tech.oomkit.windows.Window;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.robot.Robot;
import fi.utu.tech.distributed.gorilla.mesh.Mesh;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.Vector;
import java.io.Serializable;

/**
 * TODO: Extend this for GorillaMultiplayerLogic and make Overrides there
 * Alternatively this class can be also modified
 */
public class GorillaLogic implements GraphicalAppLogic {
    private Console console;
    private final MainCanvas mainCanvas = new MainCanvas();
    public Views views;

    protected GameState gameState;
    private GameMode gameMode;
    private String hostAddress = "localhost";
    private String hostPort = "1234";
    
    protected Random rnd = new Random();
    protected String[] nimet = new String[]{
    		"Tuisku", "Roosa", "Heikki", "Opiskelija", 
    		"Miisa", "Risto", "Papu", "Heidi", "Tony", "Daniel", "Tia", "Petrus", "Aleksi", "Salla", 
    		"Toffo", "Kisuli", "Miiru", "Maaru", "Monni", "Molli", "Neko", "Niiku", "Mimi", "Muusa"};
    protected String myName = nimet[rnd.nextInt(nimet.length)];
    protected final int gameSeed = 1;
    protected final int maxPlayers = 2;
    // connection luodaan meshserverin sisälllä
    //private MeshClient connection = null;
    private String serverConnectionAddress;
    private boolean host = true;
    private boolean verbose = false;
    private boolean newPlayersUpdated = false;
    private Mesh mesh = null;
    private GameConfiguration gameConfiguration;


    private Vector<Serializable> moveBuffer = new Vector<>();
    // in case the game runs too slow:

    // on Linux/Mac, first try to add the Java VM parameter -Dprism.order=sw
    // JavaFX may have some memory leaks that can crash the whole system

    // true = turns off background levels and fade in/out = faster, but not as pretty
    private final boolean lowendMachine = true;

    // duration between game ticks (in ms). larger number = computationally less demanding game
    private final int tickDuration = 20;

    // no comment
    private final boolean synkistely = true;

    // true = you can check from the text console if the computer is too slow to render all frames
    // the system will display 'Frame skipped!' if the tick() loop takes too long.
    private final boolean verboseMessages = false;

    // List of AI players
    private final Vector<Player> otherPlayers = new Vector<Player>();

    // Helpers for menu system. No need to modify
    private int c = 0;
    private int selectedMenuItem = 0;
    
    
	protected boolean confreceived = false;
    

    // we should return the one we actually use for drawing
    // the others are just proxies that end to drawing here
    // No need to modify
    @Override
    public Canvas getCanvas() {
        return mainCanvas;
    }

    // initializes the game logic
    // No need to modify
    @Override
    public AppConfiguration configuration() {
        return new AppConfiguration(tickDuration, "Gorilla", false, verboseMessages, true, true, true);
    }

    /**
     * Key handling for menu navigation functionality
     * @param k The key pressed
     */
    @Override
    public void handleKey(Key k) {
    // During the game, in order to make the menu work,
    // click the text output area on the right.
    // To enter commands, click the area again.
    	if (confreceived && gameMode != GameMode.Game) {
    		setMode(GameMode.Game);
    		System.out.println("host käynnisti pelin");
    	}
        switch (gameMode) {
            case Intro:
                setMode(GameMode.Menu);
                break;
            case Menu:
                if (k == Key.Up) {
                    if (selectedMenuItem > 0) selectedMenuItem--;
                    else selectedMenuItem = 3;
                    views.setSelectedMenuItem(selectedMenuItem);
                    return;
                }
                if (k == Key.Down) {
                    if (selectedMenuItem < 3) selectedMenuItem++;
                    else selectedMenuItem = 0;
                    views.setSelectedMenuItem(selectedMenuItem);
                    return;
                }
                if (k == Key.Enter) {
                    switch (selectedMenuItem) {
                    	case 0: handleNameChange(nimet[rnd.nextInt(nimet.length)]);
                    		break;
                    	case 1:
                    		handleMultiplayer(hostAddress);
                    		break;
                        case 2:
                            // quit active game
                            if (gameState != null) {
                                resetGame();
                                setMode(GameMode.Menu);
                            }
                            if (host) {
                                try {
                                	setMode(GameMode.Game);
                                	mesh.sendObject((GameConfiguration)gameState.configuration);
                                	if (verbose) System.out.println("configuration sent");
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                            } else {setMode(GameMode.Game);}
                            break;
                        case 3:
                            Platform.exit();
                    }
                }
                break;
            case Game:
                // instead we read with 'handleConsoleInput'
                break;
        }
    }

    /**
     * Reads the commands given by user in GUI and passes them into
     * command parser (parseCommandLine())
     */
    private void handleConsoleInput() {
        if (console != null && console.inputQueue().peek() != null) {
            parseCommandLine(console.inputQueue().poll());
        }
    }
    
    /**
     * Called after the OOMkit has initialized and a window is fully visible and usable.
     * This method is the first one to be called on this class
     * @param window Oomkit application window (no need to modify)
     * @param parameters Command line parameters given, can be used for defining port and server address to connect
     */
    @Override
    public void initialize(Window window, Application.Parameters parameters) {
        // To --port=1234 
        // IDEA: Run -> Edit configurations -> Program arguments
        // Eclipse (Ran as Java Application): Run -> Run configuration... -> Java Application -> Main (varies) -> Arguments -> Program arguments

        // Start server on the port given as a command line parameter or 1234
        try{startServer(parameters.getNamed().getOrDefault("port", "1234"), myName);
        }catch(Exception e){
        	System.out.println("server not alusted");
        	e.printStackTrace();
        }

        // Connect to address given as a command line parameter "server" (default: localhost) on port given (default: 1234)
//        connectToServer(parameters.getNamed().getOrDefault("server", "localhost"), parameters.getNamed().getOrDefault("port", "1234"));

        views = new Views(mainCanvas, lowendMachine, synkistely, configuration().tickDuration, new Random().nextLong());
        this.console = window.console();

        // Set Game into intro mode showing the level and title text
        setMode(GameMode.Intro);

        resetGame();

        // Populate menu
        views.setMenu("Kissasota 2029", new String[]{
        		"Arvo uusi nimi",
        		"Palvelinyhteys",
                "Aloita peli",
                "Lopeta"
        });

        updateMenuInfo();
    }

    /**
     * Called when the window is closed
     * Useful for terminating threads
     */
    @Override
    public void terminate() {
        System.out.println("Closing the game!");
    }

    /**
     * Resets the single player game
     */
    public void resetGame() {
        otherPlayers.clear();
        gameState = null;
    }

    /**
     * Add AI player with provided name
     * @param name The name of the ai player to be created
     */
    public void joinGame(String name) {
        if (otherPlayers.size() + 1 < maxPlayers) {
            otherPlayers.add(new Player(name, new LinkedBlockingQueue<>(), false));
        }
    }

    /**
     * Called peridically by OOMkit, makes game to proceed
     * Very important function in terms of understanding the game structure
     * See the super method documentation for better understanding
     */
    @Override
    public void tick() {
    	if(confreceived && gameMode == GameMode.Menu) robot.keyType(KeyCode.ENTER);
    	if(!host && !newPlayersUpdated) {
    		robot.keyType(KeyCode.DOWN);
    		newPlayersUpdated = true;
    	}
        handleConsoleInput();
        toggleGameMode();
        views.redraw();
    }

    /**
     * Sets the game mode. Mainly affects on the current view on the scereen (Intro, menu, game...)
     * @param mode
     */
    public void setMode(GameMode mode) {
        // Start new game if not running
        if (mode == GameMode.Game && gameState == null)
            initGame();

        gameMode = mode;
        views.setMode(mode);
        updateMenuInfo();
    }
    
    public void setMode(GameMode mode, GameConfiguration conf) {
        // Start new game if not running
        if (mode == GameMode.Game && gameState == null)
            initGame(conf);

        initGame(conf);
        gameMode = mode;
        views.setMode(mode);
        updateMenuInfo();
    }

    /**
     * Start the mesh server on the specified port
     * @param port The port the mesh should listen to for new nodes
     */
    protected void startServer(String port, String name) throws Exception{
    	System.out.print("Starting server at port " + port + "... ");
        
        // serveri ajetaan säikeessä
		new Thread(new Runnable() {
			@Override
			public void run() {
				for(int openPort = Integer.parseInt(port) ;openPort<3000;openPort++) {
			        try{
			        	mesh = new Mesh(openPort, verbose, name, moveBuffer, otherPlayers);
			        	mesh.startListening();
			        	break;
			        }catch(Exception e){
			        	System.out.println("??");
			            System.out.println("Hemmetti: "+e.getMessage());
			            System.out.println("Trying another port: " + port);
			            
			        }
                }
		        
			}
        }).start();

        //kuuntelee muutoksia moveBuffer vektorissa ja lisää liikkeet pelaajille
        new Thread(new Runnable(){
        
            @Override
            public void run() {
                while(true){
                    try {
                        if(moveBuffer.size() > 0){
                        	// TODO: handle all incoming objects
                        	if (verbose) System.out.println("received something");
                        	if (serverConnectionAddress == null) serverConnectionAddress = mesh.getConnectionInfo();
                            Object thisObject = moveBuffer.remove(0);
                            if(thisObject instanceof MoveThrowBanana){
                                MoveThrowBanana thisMove = (MoveThrowBanana)thisObject;
                                addPlayerMove(thisMove.playerName, thisMove);
                                if (verbose) System.out.println("lisätty muuvei");
                            }
                            if(thisObject instanceof Player){
                            	if (verbose) System.out.println("still received a player");
                                Player thisPlayer = (Player)thisObject;
                                joinGame(thisPlayer.name);
                            }
                            if(thisObject instanceof GameConfiguration){
                            	host = false;                            	
                            	gameConfiguration = (GameConfiguration) thisObject;
                            	if (verbose) System.out.println("received game configuration");
                            	confreceived  = true;
                            	//handleConfiguration();
                                // TODO: if thisobject gameconfiguration
                            }
                            
                        }    
                    } catch (Exception e) {
                        break;
                    }
                    
                }
            }
        }).start();

        // ...or at least somebody should be
    }
    
    private Robot robot = new Robot();
    

    /**
     * Connect the Mesh into an existing mesh
     * @param address The IP address of the mesh node to connect to
     * @param port The listening port of the mesh node to connect to
     */
    protected void connectToServer(String address, String port) {
//        System.out.printf("Connecting to server at %s", address + port);
        //System.out.println("Connecting to server at " + address +" port " + port);
        int prot = Integer.parseInt(port);

        mesh.connectToServer(address, prot);
		        //connected = true;
		        //serverConnectionAddress = mesh.getConnectionInfo();
		        //updateMenuInfo();
        
        // ...or at least somebody should be
    }

    /**
     * Starts a new single player game with max number of AI players
     */
    private void initGame() {
        double h = getCanvas().getHeight();

        // Create maxPlayers-1 AI players
        //for (int i=1; i<maxPlayers; i++) {
        //    joinGame("Kisumisu " + i);
        //}

        List<String> names = new LinkedList<>();
        names.add(myName);
        for (Player player : otherPlayers) names.add(player.name);
        
        GameConfiguration configuration;

        if (gameConfiguration == null) {
        	configuration = new GameConfiguration(gameSeed, h, names);
        } else {
        	configuration = gameConfiguration;
        }
        
        for (Player player : otherPlayers) {
        	if ((player.name).equals(myName)){
        		player.local = true;
        	} else {
        		player.local = false;
        	}
        }

        gameState = new GameState(configuration, myName, new LinkedBlockingQueue<>(), otherPlayers, host);
        views.setGameState(gameState);
    }
    
    /**
     * Starts a new single player game with max number of AI players
     */
    private void initGame(GameConfiguration configuration) {
    	// dunno what this would be for
        @SuppressWarnings("unused")
		double h = getCanvas().getHeight();

        // Create maxPlayers-1 AI players
        //for (int i=1; i<maxPlayers; i++) {
        //    joinGame("Kisumisu " + i);
        //}
        otherPlayers.clear();
        for (String playerString : configuration.playerNames) otherPlayers.add(new Player(playerString,  new LinkedBlockingQueue<>(), false));
        for (Player player : otherPlayers) {
        	if ((player.name).equals(myName)){
        		player.local = true;
        	} else {
        		player.local = false;
        	}
        }
        	
        List<String> names = new LinkedList<>();
        names.add(myName);
        for (Player player : otherPlayers) names.add(player.name);

        //GameConfiguration configuration = newConfiguration;
        

        gameState = new GameState(configuration, myName, new LinkedBlockingQueue<>(), otherPlayers, host);
        views.setGameState(gameState);
    }

    /**
     * Add move to players move queue by using player name
     * @param player Player name
     * @param move The move to be added
     */
    private void addPlayerMove(String player, Move move) {
        for (Player p : otherPlayers)
        	//if(host) {	
        		if (p.name.equals(player))
    			p.moves.add(move);  
        	//}
    }

    /**
     * Handles message sending. Usually fired by "say" command
     * @param msg Chat message object containing the message and other information
     */
    protected void handleChatMessage(ChatMessage msg) {
        //System.out.printf(myName +": %s%n", msg.contents);
        mesh.sendObject(msg);
    }

    /**
     * Handles starting a multiplayer game. This event is usually fired by selecting
     * Palvelinyhteys in game menu
     */
    protected void handleMultiplayer(String address) {
        if (verbose) System.out.println("Not implemented on this logic");
        int defaultServerPort = Integer.parseInt(hostPort);
	    if (mesh.getPort() == defaultServerPort) {
	        	defaultServerPort = 1235;
	    }
        connectToServer(address, Integer.toString(defaultServerPort));
    }

    /**
     * Handles banana throwing. This event is usually fired by angle and velocity commands
     * @param mtb
     */
    protected void handleThrowBanana(MoveThrowBanana mtb) {
    	MoveThrowBanana thisMTB = mtb;
    	thisMTB.playerName = myName;
        gameState.addLocalPlayerMove(mtb);
        mesh.sendObject(mtb);
    }

    /**
     * Handles name change. Fired by "name" command
     * @param newName Your new name
     */
    protected void handleNameChange(String newName) {
        myName = newName;
        mesh.yourName = myName;
        updateMenuInfo();
        System.out.println("Name changed to "+ myName);
    }

    /**
     * Parses the game command prompt and fires appropriate handlers
     * @param cmd Unparsed command to be parsed
     */
    private void parseCommandLine(String cmd) {
        if (cmd.contains(" ")) {
            String rest = cmd.substring(cmd.split(" ")[0].length() + 1);
            switch (cmd.split(" ")[0]) {
                case "q":
                case "quit":
                case "exit":
                    Platform.exit();
                    break;
                case "name":
                    handleNameChange(rest);
                    break;
                case "s":
                case "chat":
                case "say":
                    handleChatMessage(new ChatMessage(myName, "all", rest));
                    break;
                case "a":
                case "k":
                case "angle":
                case "kulma":
                    if (gameMode != GameMode.Game) return;
                    try {
                        double angle = Double.parseDouble(rest);
                        MoveThrowBanana mtb = new MoveThrowBanana(angle, Double.NaN, myName);
                        handleThrowBanana(mtb);
                        System.out.println("Asetettu kulma: " + angle);
                    } catch (NumberFormatException e) {
                        System.out.println("Virheellinen komento, oikea on: angle <liukuluku -45..225>");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "v":
                case "n":
                case "velocity":
                case "nopeus":
                    if (gameMode != GameMode.Game) return;
                    try {
                        double velocity = Double.parseDouble(rest);
                        MoveThrowBanana mtb = new MoveThrowBanana(Double.NaN, velocity, myName);
                        handleThrowBanana(mtb);
                        System.out.println("Asetettu nopeus: " + velocity);
                    } catch (NumberFormatException e) {
                        System.out.println("Virheellinen komento, oikea on: velocity <liukuluku 0..150>");
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "startnewserver":
					try {
						startServer(rest, myName);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
                case "connect":
                	String[] connection = rest.split(":");
                	connectToServer(connection[0],connection[1]);
                	break;
                case "sethost":
                case "hostaddress":
                	hostAddress = rest;
                	break;
                case "sethostport":
                case "hostport":
                	hostPort = rest;
                    break;
                case "join":
                	mesh.sendObject(myName);
                	System.out.println("trying to send player");

            }
        }
    }

    /**
     * Primitive AI - creates moves for AI players
     * leave this here for future local game things
     */
    @SuppressWarnings("unused")
	private void moveAIplayers() {
        // currently a rather primitive random AI
        if (new Random().nextInt(50) < 4 && !otherPlayers.isEmpty()) {
            Move move = new MoveThrowBanana(
                    new Random().nextDouble() * 180,
                    35 + new Random().nextDouble() * 35, "Kisumisu");

            addPlayerMove("Kingkong " + (new Random().nextInt(otherPlayers.size()) + 1), move);
        }
    }

    /**
     * Updates the info on the bottom of the menu
     */
    protected void updateMenuInfo() {
        views.setMenuInfo(new String[]{"Nimesi: "+myName,
        		"Pelaajia: " + (otherPlayers.size() + 1), 
        		"Yhdistetty koneeseen <-> "+ serverConnectionAddress, 
        		"Peli aktiivinen: " + (gameState != null),
        		"Tekijät: Tuisku, Roosa ja Heikki"
        		}
        		);
    }
    

    /**
     * Calls different functions depending on the current game mode. Called periodically by the GorillaLogic tick() method
     */
    private void toggleGameMode() {
        switch (gameMode) {
            case Intro:
                // when the intro is done, jump to menu
                if (views.introDone())
                    setMode(GameMode.Menu);
                break;
            case Menu:
                c++;
                if (c > 50) {
                    c = 0;
                }
                if (selectedMenuItem == 1 && c == 0) {
                    updateMenuInfo();
                }
                break;
            case Game:
                //moveAIplayers();
                // Advance the game state, the actual game
                gameState.tick();
                break;
        }
    }
}
