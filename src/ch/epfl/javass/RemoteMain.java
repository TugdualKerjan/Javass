package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Class that serves to launch a distant player. There are some important things
 * to consider: you have to run this main before the guest of the game runs the
 * local main if you don't there will be an error. Also if you run it in another
 * computer you have to be connected to the same wifi as the host and you should
 * also give him your ip address that can be easily find by typing: whats my ip
 * adress? on an internet browser
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class RemoteMain extends Application {
    /**
     * method main that will launch the distant player here no specific
     * arguments are needed
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /*
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread playerThread = new Thread(() -> {
            new RemotePlayerServer(new GraphicalPlayerAdapter(), "Test");
        });
        playerThread.setDaemon(true);
        playerThread.start();
        System.out.println("La partie commencera à la connexion du client...");
    }
}
