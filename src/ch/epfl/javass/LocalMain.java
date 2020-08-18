package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.*;
import ch.epfl.javass.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Class that serves to launch a game if you want to be the host. You have to
 * give the proper arguments to the main for it to work properly. A little
 * remainder: Utilisation: java ch.epfl.javass.LocalMain <j1>…<j4> [<graine>]
 * où: <jn> spécifie le joueur n, ainsi: h:<nom> un joueur humain nommé <nom>
 * s:<nom>:<nbr_iterations> un joueur simulé avec nbr_ierations donné et à par
 * défaut 10000 iterations(ATTENTION: le nombre d'iterations doit être plus
 * grand ou égale à 10) r:<nom>: <IP_du_joueur_distant> un joueur distant avec
 * la ip donnée par défaut il va se jouer sur l'ordinateur courrant
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class LocalMain extends Application {

    private static final int MCTS_ITERATIONS_DEFAULT = 10_000;
    private static final String IP_HOST_DEFAULT = "localhost";
    private static final String[] PLAYER_NAMES_DEFAULT = {"Aline", "Bastien",
            "Colette", "David"};
    private static final String HUMAN_PLAYER = "h";
    private static final String SIMULATED_PLAYER = "s";
    private static final String DISTANT_PLAYER = "r";
    private static final int FAILED = 1;
    private static final double PLAYER_WAITING_TIME = 2;
    private static final long END_OF_TRICK_WAITING_TIME = 1000;
    private static final int POSITION_OF_NAME_IN_ARG = 1;
    private static final int POSITION_OF_ITERATIONS_IN_ARG = 2;
    private static final int POSITION_OF_IP_IN_ARG = 2;
    private static final int POSITION_OF_PL_TYPE_IN_ARG = 0;
    private static final int POSITION_OF_SEED_IN_ARG = 4;
    private static final int MIN_ARG_ALLOWED = 4;
    private static final int MAX_ARG_ALLOWED = 5;
    private static final int MAX_ARG_ALLOWED_HUMAN_PL = 2;
    private static final int MAX_ARG_ALLOWED_SIMULATED_PL = 3;
    private static final int MAX_ARG_ALLOWED_DISTANT_PL = 3;
    private static final int MIN_ITERATIONS_MCTS = 10;
    private static final String MESSAGE_MCTS = "Rappelez-vous que la façon de créer"
            + " un joueur simulé est:\ns:Nom(optionel):Iterations"
            + "(entier plus grand ou égale à 10)(optionel)";
    private static final String MESSAGE_DISTANT = "Rappelez-vous que la façon de créer"
            + " un joueur distant est:\nr:Nom(optionel):IP(optionel)\n";
    private static final String MESSAGE_GENERAL = "Utilisation: java ch.epfl.javass.LocalMain"
            + " <j1>…<j4> [<graine>]\n" + "où :\n"
            + "<jn> spécifie le joueur n, ainsi:\n"
            + "  h:<nom>  un joueur humain nommé <nom>\n"
            + "  s:<nom>:<nbr_iterations> un joueur simulé avec nbr_ierations"
            + " donné et à par défaut 10000 iterations(ATTENTION: le nombre d'iterations"
            + " doit être plus gran ou égale à 10)\n"
            + "  r:<nom>:<IP_du_joueur_distant> un joueur distant avec ip donnée par défaut"
            + " il va se jouer sur l'ordinateur courrant";

    private Random rng;

    /**
     * main that will launch the game with the given arguments. For it to work
     * properly you have to follow the style given above.
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
        List<String> arguments = this.getParameters().getRaw();
        // checking if there are the appropriate number of arguments
        if (arguments.size() < MIN_ARG_ALLOWED
                || arguments.size() > MAX_ARG_ALLOWED) {
            System.err.println(MESSAGE_GENERAL);

            System.exit(FAILED);
        }
        // checking if the user introduced a seed or not
        if (arguments.size() == MAX_ARG_ALLOWED) {
            try {
                rng = new Random(
                        Long.parseLong(arguments.get(POSITION_OF_SEED_IN_ARG)));
            } catch (NumberFormatException e) {
                System.err.println(
                        "Votre cinquième argument donnant la graine pour le générateur"
                                + " de nombres aléatoires"
                                + "du jeu n'est pas correct, il doit être un long\n"
                                + "Et vous avez mis: "
                                + arguments.get(POSITION_OF_SEED_IN_ARG));
                System.exit(FAILED);
            }
        } else {
            rng = new Random();
        }
        // this is a two dimensional array of strings which has the information
        // for each player and will be ordered with the ordinal of each player.
        // This means that player 1 will have the information on the position
        // 0(because it's its ordinal) and same for the rest
        String[][] playersArgs = new String[PlayerId.COUNT][];

        for (int i = 0; i < PlayerId.COUNT; ++i) {
            playersArgs[i] = arguments.get(i).split(":");
        }

        Map<PlayerId, String> playerNames = createPlayerNamesMap(playersArgs);
        Map<PlayerId, Player> players = createPlayersMap(playersArgs);
        // creating new thread where the Jass game will take place
        Thread gameThread = new Thread(() -> {
            JassGame game = new JassGame(rng.nextLong(), players, playerNames);
            while (!game.isGameOver()) {
                game.advanceToEndOfNextTrick();
                try {
                    Thread.sleep(END_OF_TRICK_WAITING_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }

    /**
     * method that creates a map of all the players taking into account the
     * given arguments describing which players does the user want and how does
     * he want them.
     *
     * @param playersArgs corresponding to the information describing each player and
     *                    ordered by the ordinal of the players.
     * @return map of the players and its id if all the given arguments where
     * correctly written if not the program will stop and an error
     * message will be printed on the console
     */
    private Map<PlayerId, Player> createPlayersMap(String[][] playersArgs) {
        Map<PlayerId, Player> players = new EnumMap<>(PlayerId.class);
        for (PlayerId p : PlayerId.ALL) {
            players.put(p, createPlayerFrom(p, playersArgs));
        }
        return players;
    }

    /**
     * method that fills the map of playerNames with the ids of each of the
     * players and his the given name, if no name is given a default name is
     * taken. Will finally return this map.
     *
     * @param playersArgs are the arguments corresponding to each player information
     *                    ordered with respect to their the ordinal
     * @return playerNames which is a map with the names of each player and its
     * corresponding id.
     */
    private Map<PlayerId, String> createPlayerNamesMap(String[][] playersArgs) {
        Map<PlayerId, String> playerNames = new EnumMap<>(PlayerId.class);

        for (PlayerId p : PlayerId.ALL) {
            // getting the name of the player
            String[] playerArgs = playersArgs[p.ordinal()];
            String name = PLAYER_NAMES_DEFAULT[p.ordinal()];
            if (playerArgs.length > POSITION_OF_NAME_IN_ARG
                    && !playerArgs[POSITION_OF_NAME_IN_ARG].isEmpty()) {
                name = playerArgs[POSITION_OF_NAME_IN_ARG];
            }
            playerNames.put(p, name);
        }
        return playerNames;
    }

    /**
     * method that will try to create a player from the given arguments if the
     * arguments are correctly written it will create a new player and will put
     * it with its id in the map of the class if the arguments are not correctly
     * written it will print a message of error and will stop the program.
     *
     * @param id        of the player you want to create
     * @param arguments to create the players ordered by their ordinal
     * @return Player if it corresponded to one of the available and if the
     * arguments where correct if not it exits the program and prints a
     * message error
     */
    private Player createPlayerFrom(PlayerId id, String[][] playersArgs) {
        String[] args = playersArgs[id.ordinal()];

        String playerType = args[POSITION_OF_PL_TYPE_IN_ARG];

        switch (playerType) {
            case HUMAN_PLAYER: {
                return humanSettings(args);

            }
            case SIMULATED_PLAYER: {
                return simulatedSettings(args, id);

            }
            case DISTANT_PLAYER: {
                return distantSettings(args, id);

            }
            default: {
                System.err.print(
                        "Le joueur que vous avez décrit ne correspond a aucun de ceux"
                                + " qu'on vous propose\n"
                                + "Le premier charactère de chaque argument définie le type"
                                + " du joueur\n"
                                + "Il doit être compris dans un de ces trois:\n"
                                + "h: pour un joueur humain\n"
                                + "s: pour un joueur simulé\n"
                                + "r: pour un joueur distant\n"
                                + "\nOn vous rappelle comment utiliser ce programme:\n"
                                + MESSAGE_GENERAL);
                System.exit(FAILED);
            }
            return null;
        }
    }

    /**
     * This method will be called when we want to create a human player, it will
     * check if the arguments are correct and if not it will stop and print a
     * message of error. I everything is correct it will create a Graphical
     * player adapter and will return it.
     *
     * @param args where the information of the player is
     * @return a graphical player adapter if all the arguments where correct
     */
    private Player humanSettings(String[] args) {
        if (args.length > MAX_ARG_ALLOWED_HUMAN_PL) {
            System.err.println(
                    "Le nombre maximal d'arguments pour creer un joueur humain est"
                            + " 2 et vous en avez mis: " + args.length
                            + " arguments\n"
                            + "Rappelez-vous que la façon de créer un joueur humain est:\n"
                            + "h:Nom(optionel)");
            System.exit(FAILED);
        }
        return new GraphicalPlayerAdapter();
    }

    /**
     * This method will be called when the user wants to create a simulated
     * player. Then it will check if all the arguments given are correct with
     * respect to our rules stated at the beginning. And it will finally return
     * an MCTS player.
     *
     * @param args where the information of the player is
     * @param id   of this player
     * @return an MCTS player if all the arguments given where in accordance
     * with our rules
     */
    private Player simulatedSettings(String[] args, PlayerId id) {
        if (args.length > MAX_ARG_ALLOWED_SIMULATED_PL) {
            System.err.println(
                    "Le nombre maximal d'arguments pour creer un joueur simulé est 3"
                            + " et vous en avez mis: " + args.length
                            + " arguments\n" + MESSAGE_MCTS);

            System.exit(FAILED);
        }

        int iterations = MCTS_ITERATIONS_DEFAULT;
        if (args.length == MAX_ARG_ALLOWED_SIMULATED_PL) {
            try {
                iterations = Integer
                        .parseInt(args[POSITION_OF_ITERATIONS_IN_ARG]);
            } catch (NumberFormatException e) {
                System.err.println(
                        "Votre troisième argument pour le joueur simulé MCTS n'est pas correct,"
                                + " il doit être un entier\n" + MESSAGE_MCTS
                                + "\nEt vous avez écrit :"
                                + args[POSITION_OF_ITERATIONS_IN_ARG]
                                + " ou vous auriez du écrire le nombre d'itérations pour ce joueur");
                System.exit(FAILED);
            }
            if (iterations < MIN_ITERATIONS_MCTS) {
                System.err.println(
                        "Le nombre minimal d'itérations pour un joueur simulé MCTS est 10 "
                                + "et vous en avez mis: " + iterations
                                + " iterations");
                System.exit(FAILED);
            }
        }
        return new PacedPlayer(new MctsPlayer(id, rng.nextLong(), iterations),
                PLAYER_WAITING_TIME);
    }

    /**
     * This method will only be called when you want to create a distant player.
     * It will check that all the arguments are correct according to our rules
     * and if it is the case it will create a RemotePlayerClient and will return
     * it.
     *
     * @param args with all the information to create the player
     * @param id   of the player
     * @return a RemotePlayerClient if the given arguments where correct
     */
    private Player distantSettings(String[] args, PlayerId id) {
        if (args.length > MAX_ARG_ALLOWED_DISTANT_PL) {
            System.err.println(
                    "Le nombre maximal d'arguments pour creer un joueur distant "
                            + "est 3 et vous avez mis: " + args.length
                            + " arguments\n" + MESSAGE_DISTANT);
            System.exit(FAILED);
        }

        try {
            // we are checking if there is an ip given or if empty and if it's
            // the case we put the localhost
            String ip = (args.length == MAX_ARG_ALLOWED_DISTANT_PL
                    && !args[POSITION_OF_IP_IN_ARG].isEmpty())
                    ? args[POSITION_OF_IP_IN_ARG]
                    : IP_HOST_DEFAULT;
            return new RemotePlayerClient(ip);

        } catch (Exception e) {
            System.err.println(
                    "Vous devez en premier lieu démarrer le joueur distant (avec RemoteMain)"
                            + " sur l'autre ordinateur (ou sur le votre si vous le désirez)\n"
                            + "avant de démarrer le LocalMain sur votre ordinateur.\n"
                            + MESSAGE_DISTANT);
            System.exit(FAILED);

        }
        return null;

    }
}
