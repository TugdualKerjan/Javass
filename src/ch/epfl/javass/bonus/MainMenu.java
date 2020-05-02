package ch.epfl.javass.bonus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.PacedPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.RandomPlayer;
import ch.epfl.javass.net.RemotePlayerClient;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainMenu extends Application {

    private SimpleBooleanProperty UsernameMenuVisible = new SimpleBooleanProperty(true);
    private SimpleBooleanProperty GameSelectMenuVisible = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty IPSelectMenuVisible = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty waitingToConnect = new SimpleBooleanProperty(false);
    private Random rng = new Random(System.nanoTime());
    private TextField playerName = new TextField();
    private String[] predefinedNames = { "Alain", "Margaret", "Steve", "Ada" };
    private Map<PlayerId, Player> players;
    private Map<PlayerId, String> playerNames;
   

    private static final String JASS_STYLE = "-fx-font: 300 Optima; -fx-background-color: blue;";
    private static final String TEXT_STYLE = "-fx-text-alignment: center; -fx-font: 30 Optima; -fx-background-color: darkorange; -fx-alignment: center";
    private final static String TRICK_STYLE = "-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 0px; -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: center;";
    private static final double PLAYER_WAITING_TIME = 2;
    private static final long END_OF_TRICK_WAITING_TIME = 1000L;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JassGame");

        StackPane stack = new StackPane(usernameSelectMenu(),
                gameSelectMenu(), ipSelectMenu(), WaitingToConnectMenu());

        primaryStage.setScene(new Scene(stack, 770, 790));
        primaryStage.show();
    }

    private GridPane usernameSelectMenu() {
        GridPane grid = new GridPane();
        grid.setStyle(TRICK_STYLE);

        Text jassGame = new Text("Jass");
        jassGame.setStyle(JASS_STYLE);
        HBox box = new HBox(jassGame);
        box.setAlignment(Pos.CENTER);
        grid.add(box, 0, 0);

        Text username = new Text("  Username:  ");
        HBox input = new HBox(username, playerName);
        input.setStyle(TEXT_STYLE);
        input.setAlignment(Pos.CENTER);

        grid.add(input, 0, 1);
        playerName.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                UsernameMenuVisible.set(false);
                GameSelectMenuVisible.set(true);
            }
        });

        grid.setAlignment(Pos.CENTER);
        grid.visibleProperty().bind(UsernameMenuVisible);
        return grid;
    }
//    private void doWeNeedARevenge() {
//
//            boolean revengeBool = true;
//            for(PlayerId p : PlayerId.ALL) {
//                if(!players.get(p).doYouWantRevenge()) {
//                    revengeBool = false;
//                }
//            }
//            if(revengeBool) {
//                for(PlayerId p:PlayerId.ALL) {
//                    players.get(p).setWinningTeam(null);
//                }
//                startGame();
//            }
//        }
//    
    
    private GridPane gameSelectMenu() {
        GridPane grid = new GridPane();

        Button easyGame = new Button("Play Easy Mode");
        easyGame.setStyle(TEXT_STYLE);
        easyGame.setMinWidth(400);
        grid.add(easyGame, 0, 0);
        easyGame.setOnAction(e -> {
            startEasyGame();
        });

        Button startHardGame = new Button("Play Hard Mode");
        startHardGame.setStyle(TEXT_STYLE);
        startHardGame.setMinWidth(400);
        grid.add(startHardGame, 0, 1);
        startHardGame.setOnAction(e -> {
            startHardGame();
        });

        Button onlineHostGame = new Button("Play Online Mode as Host");
        onlineHostGame.setStyle(TEXT_STYLE);
        onlineHostGame.setMinWidth(400);
        onlineHostGame.setOnAction(e -> {
            GameSelectMenuVisible.set(false);
            IPSelectMenuVisible.set(true);
        });
        grid.add(onlineHostGame, 0, 2);

        Button onlineGuestGame = new Button("Play Online Mode as Guest");
        onlineGuestGame.setStyle(TEXT_STYLE);
        onlineGuestGame.setMinWidth(400);
        onlineGuestGame.setOnAction(e -> {
            GameSelectMenuVisible.set(false);
            waitingToConnect.set(true);
            startOnlineGuestGame();
        });
        grid.add(onlineGuestGame, 0, 3);

        Button back = new Button("Back");
        back.setStyle(TEXT_STYLE);
        GridPane.setHalignment(back, HPos.CENTER);
        back.setOnAction(e -> {
            GameSelectMenuVisible.set(false);
            UsernameMenuVisible.set(true);
        });
        grid.add(back, 0, 4);

        grid.setVgap(10d);
        grid.setAlignment(Pos.CENTER);
        grid.visibleProperty().bind(GameSelectMenuVisible);
        return grid;
    }

    private GridPane ipSelectMenu() {
        GridPane grid = new GridPane();
        grid.setStyle(TRICK_STYLE);

        List<StringProperty> ipAdresses = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            Text IpText = new Text("  IP:  ");

            TextField ip = new TextField();
            ip.setPromptText("Enter player " + (i + 1) + "'s IP");
            ipAdresses.add(ip.textProperty());

            HBox input = new HBox(IpText, ip);
            input.setStyle(TEXT_STYLE);
            input.setAlignment(Pos.CENTER);

            grid.add(input, 0, i);
        }

        Button back = new Button("Back");
        back.setStyle(TEXT_STYLE);
        back.setOnAction(e -> {
            IPSelectMenuVisible.set(false);
            GameSelectMenuVisible.set(true);
        });

        Button ok = new Button(" Start the game ");
        ok.setStyle(TEXT_STYLE);
        ok.setOnAction(e -> {
            startOnlineHostGame(ipAdresses,0);
        });

        GridPane box = new GridPane();
        box.setAlignment(Pos.CENTER);
        box.add(back, 0, 0);
        box.add(ok, 1, 0);
        box.setHgap(10d);
        grid.add(box, 0, 4);

        grid.setAlignment(Pos.CENTER);
        grid.visibleProperty().bind(IPSelectMenuVisible);
        return grid;
    }

    private VBox WaitingToConnectMenu() {
        Text top = new Text("Waiting to connect");
        Text IpText = new Text();
        String systemipaddress = "Couldn't get your IP address";
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            systemipaddress = sc.readLine().trim();
            if (!(systemipaddress.length() > 0)) 
            {
                InetAddress localhost = InetAddress.getLocalHost();
                System.out.println((localhost.getHostAddress()).trim());
                systemipaddress = (localhost.getHostAddress()).trim();
            }
        }    
        catch (Exception e2) {
            systemipaddress = "Cannot Execute Properly";
        }
        IpText = new Text("  IP:  " + systemipaddress);
        VBox box = new VBox();
        box.setStyle(TEXT_STYLE);
        box.getChildren().addAll(top, IpText);
        box.setAlignment(Pos.CENTER);

        box.visibleProperty().bind(waitingToConnect);
        return box;
    }

    private void startOnlineGuestGame() {
        Thread playerThread = new Thread(
                () -> {
                    new RemotePlayerServer(new GraphicalPlayerAdapter(), playerName.getText());
                });
        playerThread.setDaemon(true);
        playerThread.start();
        System.out.println("La partie commencera à la connexion du client...");
    }

    private Map<PlayerId, String> createPlayerNamesMap() {
        Map<PlayerId, String> ns = new EnumMap<>(PlayerId.class);
        PlayerId.ALL.forEach(i -> {
            ns.put(i, (i.equals(PlayerId.PLAYER_1) ? playerName.getText() : predefinedNames[i.ordinal()]));
        });
        return ns;
    }

    private Map<PlayerId, Player> createPlayersHard() {
        Map<PlayerId, Player> ps = new EnumMap<>(PlayerId.class);
        ps.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
        for(PlayerId p:PlayerId.ALL) {
            if (!p.equals(PlayerId.PLAYER_1)){
                ps.put(p, new PacedPlayer(new MctsPlayer(p, rng.nextLong(), 10_000), PLAYER_WAITING_TIME));
            }
        }
        return ps;
    }

    private Map<PlayerId, Player> createPlayersEasy() {
        Map<PlayerId, Player> ps = new EnumMap<>(PlayerId.class);
        ps.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
        ps.put(PlayerId.PLAYER_2, new PacedPlayer(new RandomPlayer(rng.nextLong()),PLAYER_WAITING_TIME));
        ps.put(PlayerId.PLAYER_3, new PacedPlayer(new MctsPlayer(PlayerId.PLAYER_3, rng.nextLong(), 10_000), PLAYER_WAITING_TIME));
        ps.put(PlayerId.PLAYER_4, new PacedPlayer(new RandomPlayer(rng.nextLong()),PLAYER_WAITING_TIME));

        return ps;
    }

    public void startOnlineHostGame(List<StringProperty> onlinePlayerIPs,int compt) {
        System.out.println("starting online host game");
        players = new EnumMap<>(PlayerId.class);
        playerNames = new EnumMap<>(PlayerId.class);
        
        players.put(PlayerId.PLAYER_1, new GraphicalPlayerAdapter());
        playerNames.put(PlayerId.PLAYER_1, playerName.getText());
        
        for(int i = 1; i <= onlinePlayerIPs.size(); i++) {
            if(onlinePlayerIPs.get(i - 1).get() != "" && onlinePlayerIPs.get(i - 1).get() != null && !onlinePlayerIPs.get(i - 1).get().isEmpty()) {
                String ip = onlinePlayerIPs.get(i-1).get();
                System.out.println("Player ips : " + ip);
//                try {
//                    Thread.sleep(10000);
//                } catch (InterruptedException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
                RemotePlayerClient p;
                try {
                p = new RemotePlayerClient(ip);
                }catch(Exception e) {
                    if(compt<10) 
                        startOnlineHostGame(onlinePlayerIPs,++compt);
                     return;
//                    p= new RemotePlayerClient(ip);
                }
                players.put(PlayerId.ALL.get(i), p);
                String name = p.getMyName();
                System.out.println(name);
                playerNames.put(PlayerId.ALL.get(i), p.getMyName());
            } else {
                players.put(PlayerId.ALL.get(i), new PacedPlayer(new MctsPlayer(PlayerId.ALL.get(i), rng.nextLong(), 10_000), PLAYER_WAITING_TIME));
                playerNames.put(PlayerId.ALL.get(i), predefinedNames[i]);
            }
        }
        startGame();
    }

    public void startEasyGame() {
        players = createPlayersEasy();
        playerNames = createPlayerNamesMap();
        startGame();
    }

    public void startHardGame() {
        players = createPlayersHard();
        playerNames = createPlayerNamesMap();
        startGame();
    }

    private void startGame() {
        new Thread(() -> {
            JassGame g = new JassGame(rng.nextLong(), players, playerNames);
            while (!g.isGameOver()) {
                g.advanceToEndOfNextTrick();
                try {
                    Thread.sleep(END_OF_TRICK_WAITING_TIME);
                } catch (Exception e) {
                }
            }
            try {
                Thread.sleep(10000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            doWeNeedARevenge();
        }).start();
        
        
        
    }
}
