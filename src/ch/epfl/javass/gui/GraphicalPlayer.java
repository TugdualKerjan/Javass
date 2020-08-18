package ch.epfl.javass.gui;

import ch.epfl.javass.bonus.ChatSticker;
import ch.epfl.javass.bonus.Soundlines;
import ch.epfl.javass.bonus.StickerBean;
import ch.epfl.javass.jass.*;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that will be used by the player (Graphical Player Adapter). This
 * creates the graphical interface for the Jass game with which a human will be
 * able to interact from its computer.
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class GraphicalPlayer {

    // Static variables
    private final static String SCORE_STYLE = "-fx-font: 16 Optima;"
            + " -fx-background-color: lightgray;"
            + " -fx-padding: 5px; -fx-alignment: center;";
    private final static String TRICK_STYLE = "-fx-background-color: whitesmoke;"
            + " -fx-padding: 5px; -fx-border-width: 3px 0px; -fx-border-style:"
            + " solid; -fx-border-color: gray; -fx-alignment: center;";
    private final static String HAND_STYLE = "-fx-background-color: lightgray;"
            + " -fx-spacing: 5px; -fx-padding: 5px;";
    private final static String VICTORY_STYLE = "-fx-font: 16 Optima;"
            + " -fx-background-color: white";
    private final static String HALO_STYLE = "-fx-arc-width: 20;"
            + " -fx-arc-height: 20; -fx-fill: transparent;"
            + " -fx-stroke: lightpink; -fx-stroke-width: 5; -fx-opacity: 0.5";
    private final static String TEXT_STYLE = "-fx-font: 14 Optima";
    private final static String CHAT_STYLE = "-fx-background-color: whitesmoke; -fx-padding: 5px; -fx-border-width: 3px 3px; -fx-border-style: solid; -fx-border-color: gray; -fx-alignment: bottom-center;";
    private final static double TRUMP_WIDTH_AND_HEIGHT = 101d;
    private final static double CARD_WIDTH = 120d;
    private final static double CARD_HEIGHT = 180d;
    private final static double HANDCARD_WIDTH = 80d;
    private final static double HANDCARD_HEIGHT = 120d;
    private final static double GAUSSIAN_BLUR_INTENSITY = 4d;
    private final static int POS_PL_NAMES_GRID = 0;
    private final static int POS_TURN_POINTS_GRID = 1;
    private final static int POS_TRICK_POINTS_GRID = 2;
    private final static int POS_TOTAL_STR_GRID = 3;
    private final static int POS_TOTAL_POINTS_GRID = 4;
    //    private final static float OPACITY_TOTALLY_VISIBLE = 1f;
    //    private final static float OPACITY_SLIGHTLY_VISIBLE = 0.2f;
    private final static int PLAYER_DOWN = 0;
    private final static int PLAYER_RIGHT = 1;
    private final static int PLAYER_UP = 2;
    private final static int PLAYER_LEFT = 3;
    private final static int CARD_HAND_RESOLUTION = 160;
    private final static int CARD_TRICK_RESOLUTION = 240;
    private final static double CHAT_WIDTH = 80d;
    private final static double CHAT_HEIGHT = 35d;
    private static final ObservableMap<Card, Image> imageCardsHand = createImagesMap(
            CARD_HAND_RESOLUTION);
    private static final ObservableMap<Card, Image> imagesCardsTrick = createImagesMap(
            CARD_TRICK_RESOLUTION);
    private static final ObservableMap<Color, Image> imagesTrump = createTrumpMap();
    private static ObservableMap<ChatSticker, Image> imagesSticker = createStickerMap();
    private final Scene scene;
    private final String myName;
    private boolean iWantRevenge = false;
    private double chosenCardX;
    private double chosenCardY;
    private ReadOnlyDoubleProperty width;
    private ReadOnlyDoubleProperty height;
    private String targetURL2 = "https://westeurope.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=en-US";

    /**
     * Public constructor for a Graphical player. It will assign each value to
     * its correspondent attribute of this object.
     *
     * @param ownId     playerId of this player
     * @param playerMap map with the names of each player associated to its id
     * @param score
     * @param trick
     * @param hand
     * @param commQueue array that will be used for the communication of the card to
     *                  play with the player adapter
     */
    public GraphicalPlayer(PlayerId ownId, Map<PlayerId, String> playerMap,
                           ScoreBean score, TrickBean trick, HandBean hand,
                           ArrayBlockingQueue<Card> commQueue) {
        myName = playerMap.get(ownId);
        BorderPane main = new BorderPane();
        main.setRight(createChatStickerPane(ownId));
        main.setTop(createScorePane(score, playerMap));
        Pane trickPane = createTrickPane(ownId, trick, playerMap);
        width = trickPane.widthProperty();
        height = trickPane.heightProperty();
        main.setBottom(createHandPane(hand, commQueue));
        main.setCenter(trickPane);


        StackPane stack = new StackPane(main,
                createVictoryPanes(TeamId.TEAM_1, score, playerMap),
                createVictoryPanes(TeamId.TEAM_2, score, playerMap));
        scene = new Scene(stack);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.C) {
                System.out.println("Voice command on");
                chooseCard(hand, commQueue);
            } else if (e.getCode() == KeyCode.SPACE) {
                StickerBean.setVoiceBoolean(true);
                StickerBean.setSticker(ownId, ChatSticker.SOUND);
            }

        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                StickerBean.setVoiceBoolean(false);
                StickerBean.setSticker(ownId, ChatSticker.NONE);
            }
        });
    }

    /**
     * method that will return the image of the symbol of the given color
     *
     * @param c color of the symbol we want to have the image
     * @return an Image with the symbol of the color c
     */
    private static Image createImageTrump(Color c) {
        return new Image("/trump_" + c.ordinal() + ".png");
    }

    /**
     * method that will return the image of the given card.
     *
     * @param card that we want the image of.
     * @return image of the given card
     */
    private static Image createImageCard(Card card, int size) {
        return new Image("/card_" + card.color().ordinal() + "_"
                + card.rank().ordinal() + "_" + size + ".png");
    }

    private static Image createImageSticker(ChatSticker sticker) {
        if (sticker.equals(ChatSticker.SOUND))
            return new Image("bonus/" + sticker.name().toLowerCase() + ".gif");
        return new Image("bonus/" + sticker.name().toLowerCase() + ".png");
    }

    /**
     * creates a map with each card of the game and its corresponding image.
     *
     * @return ObservabelMap with each card and its corresponding image.
     */
    private static ObservableMap<Card, Image> createImagesMap(int size) {
        ObservableMap<Card, Image> images = FXCollections
                .<Card, Image>observableHashMap();
        for (int i = 0; i < CardSet.ALL_CARDS.size(); ++i) {
            images.put(CardSet.ALL_CARDS.get(i),
                    createImageCard(CardSet.ALL_CARDS.get(i), size));
        }
        return images;
    }

    /**
     * creates a map with each color and its corresponding image.
     *
     * @return ObservableMap with each color and its image.
     */
    private static ObservableMap<Color, Image> createTrumpMap() {
        ObservableMap<Color, Image> imagesTrump = FXCollections
                .<Color, Image>observableHashMap();
        for (int i = 0; i < Color.COUNT; ++i) {
            imagesTrump.put(Color.ALL.get(i),
                    createImageTrump(Color.ALL.get(i)));
        }
        return imagesTrump;
    }

    private static ObservableMap<ChatSticker, Image> createStickerMap() {
        ObservableMap<ChatSticker, Image> imagesSticker = FXCollections
                .<ChatSticker, Image>observableHashMap();
        for (ChatSticker sticker : ChatSticker.ALL) {
            imagesSticker.put(sticker, createImageSticker(sticker));
        }
        return imagesSticker;
    }

    private void chooseCard(HandBean handBean, ArrayBlockingQueue<Card> cardQueue) {
        new Thread(() -> {
            try {
                URL obj = new URL(targetURL2);
                HttpsURLConnection con = (HttpsURLConnection) obj
                        .openConnection();
                con.setDoOutput(true);

                con.setRequestMethod("POST");
                con.setRequestProperty("Host",
                        "westeurope.stt.speech.microsoft.com");
                con.addRequestProperty("Content-Type",
                        "audio/wav; codecs=audio/pcm; samplerate=16000");
                con.addRequestProperty("Ocp-Apim-Subscription-Key",
                        "ef0f47d44fb04e0bb4a0cd9305a869fb");
                con.addRequestProperty("Transfer-Encoding", "chunked");
                con.addRequestProperty("Expect", "5000-continue");

                TargetDataLine targetDataLine = Soundlines.getInput();

                System.out.println("Listening...");

                try {
                    AudioInputStream writer = new AudioInputStream(
                            (InputStream) new AudioInputStream(
                                    targetDataLine),
                            Soundlines.getFormat(),
                            (long) (Soundlines.getFormat().getFrameRate() * 2));
                    DataOutputStream request = new DataOutputStream(
                            con.getOutputStream());
                    AudioSystem.write(writer, Type.WAVE, request);


                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                targetDataLine.stop();
                targetDataLine.start();
                System.out.println(targetDataLine.isOpen());

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        try {
                            String[] array = inputLine.split(",");
                            for (String s : array) {
                                if (s.contains("DisplayText")) {
                                    String text = s.split(":")[1].toLowerCase();
                                    Rank rank = null;
                                    Color color = null;
                                    System.out.println(text);
                                    Pattern p = Pattern.compile(
                                            "6|7|8|9|10|ace|king|queen|jack|ten|six|seven|eight|nine");
                                    Matcher m = p.matcher(text);
                                    if (m.find()) {
                                        String resultString = m.group();
                                        if (resultString.length() <= 2)
                                            rank = Rank.ALL.get(Integer
                                                    .parseInt(resultString) - 6);
                                        else
                                            switch (resultString.toLowerCase()) {
                                                case ("six"): {
                                                    rank = Rank.SIX;
                                                    break;
                                                }
                                                case ("seven"): {
                                                    rank = Rank.SEVEN;
                                                    break;
                                                }
                                                case ("eight"): {
                                                    rank = Rank.EIGHT;
                                                    break;
                                                }
                                                case ("nine"): {
                                                    rank = Rank.NINE;
                                                    break;
                                                }
                                                case ("ten"): {
                                                    rank = Rank.TEN;
                                                    break;
                                                }
                                                case ("jack"): {
                                                    rank = Rank.JACK;
                                                    break;
                                                }
                                                case ("queen"): {
                                                    rank = Rank.QUEEN;
                                                    break;
                                                }
                                                case ("king"): {
                                                    rank = Rank.KING;
                                                    break;
                                                }
                                                case ("ace"): {
                                                    rank = Rank.ACE;
                                                    break;
                                                }
                                                default:
                                                    rank = null;
                                            }
                                        System.out.println("Card rank: "
                                                + rank.toString());
                                    }
                                    p = Pattern
                                            .compile("spade|heart|diamond|club");
                                    m = p.matcher(text);
                                    if (m.find()) {
                                        String resultString = m.group();
                                        switch (resultString.toLowerCase()) {
                                            case ("spade"): {
                                                color = Color.SPADE;
                                                break;
                                            }
                                            case ("club"): {
                                                color = Color.CLUB;
                                                break;
                                            }
                                            case ("diamond"): {
                                                color = Color.DIAMOND;
                                                break;
                                            }
                                            case ("heart"): {
                                                color = Color.HEART;
                                                break;
                                            }
                                            default:
                                                color = null;
                                        }
                                        System.out.println("Card color: "
                                                + color.toString());
                                    }
                                    if (color != null && rank != null)
                                        if (handBean.playableCards()
                                                .contains(Card.of(color, rank)))
                                            cardQueue.put(handBean.handProperty().get(
                                                    handBean.handProperty().indexOf(Card
                                                            .of(color, rank))));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    in.close();
                    con.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * method that will create and return the Stage where the game will be
     * displayed. It creates the score pane, trick pane, hand pane and victory
     * panes and puts them into the stage.
     *
     * @return the stage with all the panes
     */
    public Stage createStage() {
        Stage stage = new Stage();
        stage.setTitle("Javass - " + myName);
        stage.setScene(scene);
        return stage;
    }

    /**
     * method that creates a Grid pane for the score of both teams 1 and 2
     *
     * @return Grid Pane with the scorePane
     */
    private GridPane createScorePane(ScoreBean scoreBean,
                                     Map<PlayerId, String> playerNames) {
        GridPane scoresPane = new GridPane();
        scoresPane.setStyle(SCORE_STYLE);
        for (TeamId team : TeamId.ALL) {
            int row = team.ordinal();

            StringProperty diffPoints = new SimpleStringProperty();

            scoreBean.turnPointsProperty(team)
                    .addListener((o, x,
                                  y) -> diffPoints.set("(+"
                            + Math.max((y.intValue() - x.intValue()), 0)
                            + ")"));

            Text names = new Text(getPlayerNames(team, playerNames));
            GridPane.setHalignment(names, HPos.RIGHT);
            scoresPane.add(names, POS_PL_NAMES_GRID, row);

            Text turnPoints = new Text();
            GridPane.setHalignment(turnPoints, HPos.RIGHT);
            turnPoints.textProperty()
                    .bind(Bindings.convert(scoreBean.turnPointsProperty(team)));
            scoresPane.add(turnPoints, POS_TURN_POINTS_GRID, row);

            Text trickPointsWonned = new Text();
            GridPane.setHalignment(trickPointsWonned, HPos.LEFT);
            trickPointsWonned.textProperty().bind(Bindings.convert(diffPoints));
            scoresPane.add(trickPointsWonned, POS_TRICK_POINTS_GRID, row);

            Text total = new Text(" / Total : ");
            GridPane.setHalignment(total, HPos.LEFT);
            scoresPane.add(total, POS_TOTAL_STR_GRID, row);

            Text totalPointsText = new Text();
            GridPane.setHalignment(totalPointsText, HPos.RIGHT);
            totalPointsText.textProperty()
                    .bind(Bindings.convert(scoreBean.gamePointsProperty(team)));
            scoresPane.add(totalPointsText, POS_TOTAL_POINTS_GRID, row);
        }
        return scoresPane;
    }

    /**
     * method that return in form of a string the name of the two player of the
     * corresponding team
     *
     * @param team that you want to get the names of the players
     * @return a String with both names of the players of team.
     */
    private String getPlayerNames(TeamId team,
                                  Map<PlayerId, String> playerNames) {
        PlayerId player1 = (team.equals(TeamId.TEAM_1)) ? PlayerId.PLAYER_1
                : PlayerId.PLAYER_2;
        PlayerId player2 = (team.equals(TeamId.TEAM_1)) ? PlayerId.PLAYER_3
                : PlayerId.PLAYER_4;

        return playerNames.get(player1) + " et " + playerNames.get(player2)
                + " : ";
    }

    /**
     * method that creates a GridPane with the trick that is being played it
     * will be formed by the four cards and the trump in the middle
     *
     * @return the GridPane of the trick
     */
    private GridPane createTrickPane(PlayerId ownId, TrickBean trickBean,
                                     Map<PlayerId, String> playerNames) {
        GridPane trickPane = new GridPane();
        trickPane.setStyle(TRICK_STYLE);

        PlayerId[] playerInOrder = new PlayerId[PlayerId.COUNT];
        for (int i = 0; i < PlayerId.COUNT; i++) {
            playerInOrder[i] = PlayerId.ALL
                    .get((ownId.ordinal() + i) % PlayerId.COUNT);
        }

        // Bottom player, bottom cell
        trickPane.add(createBox(playerInOrder[PLAYER_DOWN], ownId, playerNames,
                trickBean), 1, 2);
        // Right player, right column
        trickPane.add(createBox(playerInOrder[PLAYER_RIGHT], ownId, playerNames,
                trickBean), 2, 0, 1, 3);
        // Top player, top cell
        trickPane.add(createBox(playerInOrder[PLAYER_UP], ownId, playerNames,
                trickBean), 1, 0);
        // Left player, left column
        trickPane.add(createBox(playerInOrder[PLAYER_LEFT], ownId, playerNames,
                trickBean), 0, 0, 1, 3);

        trickPane.add(createTrumpBox(trickBean), 1, 1);

        return trickPane;
    }

    /**
     * method that will create the box of the trump with a binding to the trick
     * in order to be updated if the trump changes.
     *
     * @return the box of the trump
     */
    private VBox createTrumpBox(TrickBean trickBean) {
        ImageView trump = new ImageView();
        trump.imageProperty()
                .bind(Bindings.valueAt(imagesTrump, trickBean.trumpProperty()));
        trump.imageProperty().addListener((o, oV, nV) -> {
            trump.setOpacity(0);
            trump.setScaleX(CARD_WIDTH);
            trump.setScaleY(CARD_HEIGHT);
            trump.setRotate(80);
            Timeline timeline = new Timeline();
            timeline.getKeyFrames()
                    .addAll(new KeyFrame(Duration.millis(500), "Move",
                                    new KeyValue(trump.opacityProperty(), 1)),

                            new KeyFrame(Duration.millis(500), "Bigger",
                                    new KeyValue(trump.scaleXProperty(), 1),
                                    new KeyValue(trump.scaleYProperty(), 1),
                                    new KeyValue(trump.rotateProperty(), 0)));
            timeline.play();
        });
        trump.setFitWidth(TRUMP_WIDTH_AND_HEIGHT);
        trump.setFitHeight(TRUMP_WIDTH_AND_HEIGHT);
        VBox trumpBox = new VBox(trump);
        trumpBox.setAlignment(Pos.CENTER);
        return trumpBox;
    }

    /**
     * method that will create the ImageView of the card that the player with
     * PlayerId being ownId has played it will be empty if he hasn't played yet
     * the card. Anyways it is binded so it changes when this card changes
     *
     * @param ownId id of the player you want to know which card he played
     * @return Image view of the card played by the player with ownId
     */
    private ImageView createCard(PlayerId ownId, PlayerId playerId, TrickBean trickBean) {
        ImageView card = new ImageView();
        int millisecondTime = 300;
        card.imageProperty().bind(Bindings.valueAt(imagesCardsTrick,
                Bindings.valueAt(trickBean.trickProperty(), playerId)));
        if (ownId == playerId) {
            card.imageProperty().addListener((o, oV, nV) -> {
                double cardX = (card.localToScene(card.getBoundsInLocal())
                        .getMinX()
                        + card.localToScene(card.getBoundsInLocal()).getMaxX())
                        / 2;
                double cardY = (card.localToScene(card.getBoundsInLocal())
                        .getMinY()
                        + card.localToScene(card.getBoundsInLocal()).getMaxY())
                        / 2;
                card.setTranslateX(chosenCardX - cardX);
                card.setTranslateY(chosenCardY - cardY);
                card.setScaleX(HANDCARD_WIDTH / CARD_WIDTH);
                card.setScaleY(HANDCARD_HEIGHT / CARD_HEIGHT);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames()
                        .add(new KeyFrame(Duration.millis(100),
                                new KeyValue(card.translateXProperty(), 0),
                                new KeyValue(card.translateYProperty(), 0),
                                new KeyValue(card.scaleXProperty(), 1),
                                new KeyValue(card.scaleYProperty(), 1)));
                timeline.play();
            });
        }
        if (ownId == PlayerId.ALL.get((playerId.ordinal() + 1) % PlayerId.COUNT)) {
            card.imageProperty().addListener((o, oV, nV) -> {
                card.setTranslateX(-width.get());
                card.setScaleX(HANDCARD_WIDTH / CARD_WIDTH);
                card.setScaleY(HANDCARD_HEIGHT / CARD_HEIGHT);
                card.setRotate(80);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(millisecondTime), "Move",
                                new KeyValue(card.translateXProperty(), 0)),

                        new KeyFrame(Duration.millis(millisecondTime), "Bigger",
                                new KeyValue(card.scaleXProperty(), 1),
                                new KeyValue(card.scaleYProperty(), 1),
                                new KeyValue(card.rotateProperty(), 0)));
                timeline.play();
            });
        }
        if (ownId == PlayerId.ALL.get((playerId.ordinal() + 3) % PlayerId.COUNT)) {
            card.imageProperty().addListener((o, oV, nV) -> {
                card.setTranslateX(width.get());
                card.setScaleX(HANDCARD_WIDTH / CARD_WIDTH);
                card.setScaleY(HANDCARD_HEIGHT / CARD_HEIGHT);
                card.setRotate(-80);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(millisecondTime), "Move",
                                new KeyValue(card.translateXProperty(), 0)),

                        new KeyFrame(Duration.millis(millisecondTime), "Bigger",
                                new KeyValue(card.scaleXProperty(), 1),
                                new KeyValue(card.scaleYProperty(), 1),
                                new KeyValue(card.rotateProperty(), 0)));
                timeline.play();
            });
        }
        if (ownId == PlayerId.ALL.get((playerId.ordinal() + 2) % PlayerId.COUNT)) {
            card.imageProperty().addListener((o, oV, nV) -> {
                card.setTranslateY(-height.get());
                card.setScaleX(HANDCARD_WIDTH / CARD_WIDTH);
                card.setScaleY(HANDCARD_HEIGHT / CARD_HEIGHT);
                card.setRotate(80);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(millisecondTime), "Move",
                                new KeyValue(card.translateYProperty(), 0)),

                        new KeyFrame(Duration.millis(millisecondTime), "Bigger",
                                new KeyValue(card.scaleXProperty(), 1),
                                new KeyValue(card.scaleYProperty(), 1),
                                new KeyValue(card.rotateProperty(), 0)));
                timeline.play();
            });
        }
        card.setFitHeight(CARD_HEIGHT);
        card.setFitWidth(CARD_WIDTH);
        return card;
    }

    /**
     * method that creates a VBox with the name of the player and its
     * corresponding card
     *
     * @param ownId of the player
     * @param card  of the player
     * @return VBox with the name of the player and image of the card
     */
    private VBox createBox(PlayerId playerId, PlayerId ownId,
                           Map<PlayerId, String> playerNames, TrickBean trickBean) {
        ImageView card = createCard(ownId, playerId, trickBean);
        VBox box;
        if (!playerId.equals(ownId)) {
            box = new VBox(new Text(playerNames.get(playerId)), new StackPane(
                    createHalo(playerId, trickBean), card, createChatBubble(ownId, playerId)));
        } else {
            box = new VBox(
                    new StackPane(createHalo(playerId, trickBean), card,
                            createChatBubble(ownId, playerId)),
                    new Text(playerNames.get(playerId)));
        }
        box.setAlignment(Pos.CENTER);
        box.setStyle(TEXT_STYLE);
        return box;

    }

    /**
     * method that will create a halo around each card and this will only be
     * visible when the card is the best one in the trick.
     *
     * @param ownId of the player who played the card
     * @return a Rectangle that will be placed around the card with the
     * previously explained property
     */
    private Rectangle createHalo(PlayerId playerId, TrickBean trickBean) {
        Rectangle rect = new Rectangle();
        rect.setHeight(CARD_HEIGHT);
        rect.setWidth(CARD_WIDTH);
        rect.setStyle(HALO_STYLE);
        rect.setEffect(new GaussianBlur(GAUSSIAN_BLUR_INTENSITY));
        rect.visibleProperty()
                .bind(trickBean.winningPlayer().isEqualTo(playerId));

        return rect;
    }

    /**
     * method that creates the final pane where there will be the names of the
     * players and its score. This one will only be visible when the game has
     * finished and the current team has won.
     *
     * @param team that we want to create its pane
     * @return BorderPane with team players names and points of both current and
     * other team
     */
    private BorderPane createVictoryPanes(TeamId team, ScoreBean scoreBean,
                                          Map<PlayerId, String> playerNames) {
        BorderPane victoryPane = new BorderPane();
        victoryPane.setStyle(VICTORY_STYLE);

        Text text = new Text();
        text.setTextAlignment(TextAlignment.CENTER);

        text.textProperty()
                .bind(Bindings.format("%s ont gagné avec %d points contre %d.",
                        getPlayerNames(team, playerNames),
                        scoreBean.totalPointsProperty(team),
                        scoreBean.totalPointsProperty(team.other())));

        victoryPane.setCenter(text);

        victoryPane.visibleProperty()
                .bind(scoreBean.winningTeamProperty().isEqualTo(team));
//
//        Button wantRevenge = new Button("I Want a Revenge!!");
//        wantRevenge.setStyle(TEXT_STYLE);
//        wantRevenge.setOnAction(e->{
//            iWantRevenge = true;
//        });
//        victoryPane.setBottom(wantRevenge);
        return victoryPane;
    }

    public boolean wantRevenge() {
        return iWantRevenge;
    }

    /**
     * method that will create the pane where the hand of this class current
     * player will be displayed.
     *
     * @return Pane with each card and you can click on the playable cards and
     * play it
     */
    private Pane createHandPane(HandBean handBean,
                                ArrayBlockingQueue<Card> commQueue) {
        Pane handPane;
        HBox box = new HBox();
        box.setStyle(HAND_STYLE);
        ImageView[] cardHand = new ImageView[Jass.HAND_SIZE];
        for (int i = 0; i < Jass.HAND_SIZE; ++i) {
            cardHand[i] = createCardHand(i, handBean, commQueue);
            box.getChildren().add(cardHand[i]);
        }
        handPane = new Pane(box);
        return handPane;
    }

    /**
     * method that will create the ImageView of the card of the hand at the
     * given index.
     *
     * @param index of the card in the hand
     * @return ImageView of the card at the given index
     */

    private ImageView createCardHand(int index, HandBean handBean,
                                     ArrayBlockingQueue<Card> commQueue) {
        ImageView card = new ImageView();
        card.imageProperty().bind(Bindings.valueAt(imageCardsHand,
                Bindings.valueAt(handBean.handProperty(), index)));

        card.setOnMouseClicked(e -> {
            try {
                commQueue.put(handBean.handProperty().get(index));
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });

        card.imageProperty().addListener((o, oV, nV) -> {
            if (oV != nV && nV == null) {
                chosenCardX = (card.localToScene(card.getBoundsInLocal())
                        .getMaxX()
                        + card.localToScene(card.getBoundsInLocal()).getMinX())
                        / 2;
                chosenCardY = (card.localToScene(card.getBoundsInLocal())
                        .getMaxY()
                        + card.localToScene(card.getBoundsInLocal()).getMinY())
                        / 2;
            }
        });

        BooleanBinding isPlayable = Bindings.createBooleanBinding(
                () -> handBean.playableCards()
                        .contains(handBean.handProperty().get(index)),
                handBean.playableCards(), handBean.handProperty());

        card.opacityProperty()
                .bind(Bindings.when(isPlayable).then(1f).otherwise(0.2f));
        card.disableProperty().bind(isPlayable.not());

        card.setFitHeight(HANDCARD_HEIGHT);
        card.setFitWidth(HANDCARD_WIDTH);

        return card;
    }

    private ImageView createChatBubble(PlayerId ownId, PlayerId player) {
        ImageView bubble = new ImageView();
        bubble.imageProperty().bind(Bindings.valueAt(imagesSticker,
                Bindings.valueAt(StickerBean.stickerProperty(), player)));
        Timeline timeline = createTimelineAnimation(bubble, player);
        bubble.imageProperty().addListener((o, oV, nV) -> {
            bubble.opacityProperty().set(1);
            if (StickerBean.stickerProperty().get(player)
                    .equals(ChatSticker.NONE)) {
                bubble.opacityProperty().set(0);
            } else if (!StickerBean.stickerProperty().get(player)
                    .equals(ChatSticker.SOUND)) {
                timeline.jumpTo(Duration.ZERO);
                timeline.play();
            }
        });
        bubble.setFitHeight(HANDCARD_WIDTH / 3);
        bubble.setFitWidth(HANDCARD_WIDTH);
        bubble.setTranslateX(
                (player.equals(PlayerId.ALL.get((ownId.ordinal() + 3) % 4))
                        ? -CARD_WIDTH / 2
                        : CARD_WIDTH / 2));
        bubble.setTranslateY((player.equals(ownId) ? CARD_HEIGHT / 2 - 20
                : -CARD_HEIGHT / 2 + 20));
        return bubble;
    }

    private GridPane createChatStickerPane(PlayerId ownId) {
        GridPane gridPane = new GridPane();
        gridPane.setStyle(CHAT_STYLE);
        gridPane.add(createStickerSelect(ownId, ChatSticker.SEXY), 0, 0);
        gridPane.add(createStickerSelect(ownId, ChatSticker.LMAO), 1, 0);
        gridPane.add(createStickerSelect(ownId, ChatSticker.REKT), 0, 1);
        gridPane.add(createStickerSelect(ownId, ChatSticker.YEET), 1, 1);
        gridPane.setHgap(5d);
        gridPane.setVgap(5d);
        return gridPane;
    }

    private ImageView createStickerSelect(PlayerId ownId, ChatSticker sticker) {
        ImageView image = new ImageView();
        image.setImage(imagesSticker.get(sticker));
        image.setOnMouseClicked(e -> {
            StickerBean.setSticker(ownId, sticker);
            new Timeline(
                    new KeyFrame(Duration.millis(50), "Smaller",
                            new KeyValue(image.scaleXProperty(), 0.7),
                            new KeyValue(image.scaleYProperty(), 0.7)),

                    new KeyFrame(Duration.millis(150), "Bigger",
                            new KeyValue(image.scaleXProperty(), 1),
                            new KeyValue(image.scaleYProperty(), 1)),

                    new KeyFrame(Duration.millis(500),
                            new KeyValue(image.disableProperty(), true)),

                    new KeyFrame(Duration.millis(600),
                            new KeyValue(image.disableProperty(), false)))
                    .play();
            ;
        });
        image.setFitWidth(CHAT_WIDTH);
        image.setFitHeight(CHAT_HEIGHT);
        return image;
    }

    private Timeline createTimelineAnimation(ImageView view, PlayerId player) {
        Timeline timeline = new Timeline();
        timeline.getKeyFrames()
                .addAll(new KeyFrame(Duration.millis(100), "Bigger",
                                new KeyValue(view.scaleXProperty(), 1.3),
                                new KeyValue(view.scaleYProperty(), 1.3)),

                        new KeyFrame(Duration.millis(300), "Smaller",
                                new KeyValue(view.scaleXProperty(), 1),
                                new KeyValue(view.scaleYProperty(), 1)),

                        new KeyFrame(Duration.millis(4000),
                                new KeyValue(view.opacityProperty(), 1)),

                        new KeyFrame(Duration.millis(5000),
                                new KeyValue(view.opacityProperty(), 0)));
        return timeline;
    }
}
