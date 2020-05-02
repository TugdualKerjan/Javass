

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

public final class runServer extends Application {
    public static void main(String[] args) { launch(args); }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        new Thread(() -> {
            new RemotePlayerServer(new GraphicalPlayerAdapter(), "Bob");
        }).start();
    }
}

