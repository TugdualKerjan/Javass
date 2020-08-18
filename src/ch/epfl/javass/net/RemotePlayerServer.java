package ch.epfl.javass.net;

import ch.epfl.javass.bonus.ChatSticker;
import ch.epfl.javass.bonus.Soundlines;
import ch.epfl.javass.bonus.StickerBean;
import ch.epfl.javass.jass.*;
import ch.epfl.javass.jass.Card.Color;
import javafx.collections.MapChangeListener;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class RemotePlayerServer {

    private Player player;
    private String myName;

    private ServerSocket s0Voice;
    private Socket sVoice;
    private InputStream inputVoice;
    private OutputStream outputVoice;

    private BufferedReader r;
    private BufferedWriter w;
    private Socket s;
    private ServerSocket s0;


    public RemotePlayerServer(Player p, String myName) {
        StickerBean.setOnlineBoolean(true);
        this.myName = myName;
        player = p;
        run();
    }

    public void run() {
        //Sticker
        Thread stickers = new Thread(() -> {
            try (ServerSocket s0Chat = new ServerSocket(5109)) {
                Socket sChat = s0Chat.accept();
                BufferedReader rChat = new BufferedReader(new InputStreamReader(sChat.getInputStream(), US_ASCII));
                BufferedWriter wChat = new BufferedWriter(new OutputStreamWriter(sChat.getOutputStream(), US_ASCII));
                StickerBean.stickerProperty().addListener((MapChangeListener<PlayerId, ChatSticker>) l -> {
                    try {
                        String playerSticker = StringSerializer.serializeString(l.getMap().get(l.getKey()).name());
                        String playerOrdinal = StringSerializer.serializeInt(l.getKey().ordinal());
                        wChat.write(StringSerializer.combine(',', playerOrdinal, playerSticker) + "\n");
                        wChat.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                while (!sChat.isClosed()) {
                    String result;
                    if ((result = rChat.readLine()) != null) {
//                        System.out.println(result);
                        try {
                            String[] bob = StringSerializer.split(result, ',');
                            String playerSticker = StringSerializer.deserializeString(bob[1]);
                            PlayerId player = PlayerId.ALL.get(StringSerializer.deserializeInt(bob[0]));
                            ChatSticker sticker = ChatSticker.valueOf(playerSticker);

                            StickerBean.setSticker(player, sticker);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        stickers.setDaemon(true);
        stickers.start();

        try {
            s0Voice = new ServerSocket(5020);
            sVoice = s0Voice.accept();
            inputVoice = new BufferedInputStream(sVoice.getInputStream());
            outputVoice = new BufferedOutputStream(sVoice.getOutputStream());

            startPlayback();
            startListening();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        //Game
        try {
            s0 = new ServerSocket(5108);
            s = s0.accept();
            r = new BufferedReader(new InputStreamReader(s.getInputStream(), US_ASCII));
            w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), US_ASCII));
            while (!s0.isClosed()) {
                String result;
                if ((result = r.readLine()) != null) {
                    whichMethod(result);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void whichMethod(String result) {
        //change name of variable to something more representative
        String[] bob = StringSerializer.split(result, ' ');
        switch (JassCommand.valueOf(bob[0])) {
            case PLRS:
                String[] playerNames = StringSerializer.split(bob[2], ',');
                Map<PlayerId, String> map = new HashMap<>();
                map.put(PlayerId.PLAYER_1, StringSerializer.deserializeString(playerNames[0]));
                map.put(PlayerId.PLAYER_2, StringSerializer.deserializeString(playerNames[1]));
                map.put(PlayerId.PLAYER_3, StringSerializer.deserializeString(playerNames[2]));
                map.put(PlayerId.PLAYER_4, StringSerializer.deserializeString(playerNames[3]));
                player.setPlayers(PlayerId.ALL.get(StringSerializer.deserializeInt(bob[1])), map);
                break;
            case TRMP:
                player.setTrump(Color.ALL.get(StringSerializer.deserializeInt(bob[1])));
                break;
            case HAND:
                player.updateHand(CardSet.ofPacked(StringSerializer.deserializeLong(bob[1])));
                break;
            case TRCK:
                player.updateTrick(Trick.ofPacked(StringSerializer.deserializeInt(bob[1])));
                break;
            case CARD:
                String[] components = StringSerializer.split(bob[1], ',');
                Card toPlay = player.cardToPlay(
                        TurnState.ofPackedComponents(StringSerializer.deserializeLong(components[0]),
                                StringSerializer.deserializeLong(components[1]),
                                StringSerializer.deserializeInt(components[2])),
                        CardSet.ofPacked(StringSerializer.deserializeLong(bob[2])));

                try {
                    w.write(StringSerializer.serializeInt(toPlay.packed()) + "\n");
                    w.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case SCOR:
                player.updateScore(Score.ofPacked(StringSerializer.deserializeLong(bob[1])));
                break;
            case NAME:
                try {
                    System.out.println(myName);
                    w.write(StringSerializer.serializeString(myName) + "\n");
                    w.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case WINR:
                player.setWinningTeam(TeamId.ALL.get(StringSerializer.deserializeInt(bob[1])));
                try {
                    r.close();
                    w.close();
                    s.close();
                    s0.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
                break;
            case RVNG:
                try {
                    w.write(StringSerializer.serializeInt((player.doYouWantRevenge()) ? 1 : 0));
                    w.flush();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            default:
                System.out.println("we missed a case");
        }
    }

    private void startListening() throws LineUnavailableException {
        TargetDataLine targetDataLine = Soundlines.getInput();

        System.out.println("Getting info from client");

        StickerBean.booleanProperty().addListener((o, oV, nV) -> {
            if (nV != oV || nV.booleanValue() == true) {
                Thread listen = new Thread() {
                    @Override
                    public void run() {
                        try {
                            byte tempBuffer[] = new byte[10000];
                            while (StickerBean.booleanProperty().get()) {
                                targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                                outputVoice.write(tempBuffer);
                                outputVoice.flush();
                            }
                            join();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                listen.setDaemon(true);
                listen.start();
            }
        });
    }

    private void startPlayback() throws LineUnavailableException {
        SourceDataLine sourceDataLine = Soundlines.getOutput();

        Thread playback = new Thread() {
            @Override
            public void run() {
                try {
                    byte tempBuffer[] = new byte[10000];
                    while (inputVoice.read(tempBuffer) != -1) {
                        sourceDataLine.write(tempBuffer, 0, tempBuffer.length);
                    }
                    sourceDataLine.drain();
                    sourceDataLine.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ;
        };
        playback.setDaemon(true);
        playback.start();
    }
}
