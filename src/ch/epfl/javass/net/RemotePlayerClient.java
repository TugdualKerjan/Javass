package ch.epfl.javass.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Map;
import java.util.StringJoiner;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import ch.epfl.javass.bonus.ChatSticker;
import ch.epfl.javass.bonus.Soundlines;
import ch.epfl.javass.bonus.StickerBean;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;
import javafx.collections.MapChangeListener;

public final class RemotePlayerClient implements Player, AutoCloseable {
    
    private InputStream inputVoice;
    private OutputStream outputVoice;
    private Socket sVoice;

    private BufferedReader r;
    private BufferedWriter w;
    private Socket s;

    public RemotePlayerClient(String nomHote) {
        StickerBean.setOnlineBoolean(true);

        //Sticker
        Thread stickers = new Thread(() -> {
            try (Socket sChat = new Socket(nomHote, 5109)) {
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
                while(!sChat.isClosed()) {
                    String result;
                    if ((result = rChat.readLine()) != null) {
                        System.out.println(result);
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
                e.printStackTrace();
                throw new UncheckedIOException(e);
            }
        });
        stickers.setDaemon(true);
        stickers.start();

        //Voice
        try {
            sVoice = new Socket(nomHote, 5020);
            inputVoice = new BufferedInputStream(sVoice.getInputStream());
            outputVoice = new BufferedOutputStream(sVoice.getOutputStream());

            startListening();
            startPlayback();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        //Game
        try {
            System.out.println(nomHote);
            this.s = new Socket(nomHote, 5108);
            this.r = new BufferedReader(new InputStreamReader(s.getInputStream(), US_ASCII));
            this.w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), US_ASCII));
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        String players = StringSerializer.combine(',',
                StringSerializer.serializeString(playerNames.get(PlayerId.PLAYER_1)),
                StringSerializer.serializeString(playerNames.get(PlayerId.PLAYER_2)),
                StringSerializer.serializeString(playerNames.get(PlayerId.PLAYER_3)),
                StringSerializer.serializeString(playerNames.get(PlayerId.PLAYER_4)));
        send(JassCommand.PLRS, StringSerializer.serializeInt(ownId.ordinal()), players);
    }

    @Override
    public void setTrump(Card.Color trump) {
        send(JassCommand.TRMP, StringSerializer.serializeInt(trump.ordinal()));
    }

    @Override
    public void updateHand(CardSet newHand) {
        send(JassCommand.HAND, StringSerializer.serializeLong(newHand.packed()));
    }

    @Override
    public void updateTrick(Trick newTrick) {
        send(JassCommand.TRCK, StringSerializer.serializeInt(newTrick.packed()));
    }

    @Override
    public void updateScore(Score score) {
        send(JassCommand.SCOR, StringSerializer.serializeLong(score.packed()));
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        send(JassCommand.WINR, StringSerializer.serializeInt(winningTeam.ordinal()));
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        String score = StringSerializer.serializeLong(state.packedScore());
        String cards = StringSerializer.serializeLong(state.packedUnplayedCards());
        String trick = StringSerializer.serializeInt(state.packedTrick());

        String stringState = StringSerializer.combine(',', score, cards, trick);
        send(JassCommand.CARD, stringState, StringSerializer.serializeLong(hand.packed()));
        try {
            return Card.ofPacked(StringSerializer.deserializeInt(r.readLine()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public boolean doYouWantRevenge() {
        send(JassCommand.RVNG);
        try {
            return StringSerializer.deserializeInt(r.readLine())==1;
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getMyName() {
        send(JassCommand.NAME);
        try {
            return StringSerializer.deserializeString(r.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        s.close();
        r.close();
        w.close();
    }

    private void send(JassCommand command, String... string) {
        try {
            w.write(command.toString() + " ");
            StringJoiner j = new StringJoiner(" ", "", "\n");
            for (String s : string) {
                j.add(s);
            }
            w.write(j.toString());
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListening() throws LineUnavailableException {
        TargetDataLine targetDataLine = Soundlines.getInput();

        System.out.println("Getting info from client");

        StickerBean.booleanProperty().addListener((o, oV, nV) -> {
            if(nV != oV || nV.booleanValue() == true) {
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
                        } catch (Exception e) { e.printStackTrace(); }
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
                } catch (IOException e) { e.printStackTrace(); }
            };
        };
        playback.setDaemon(true);
        playback.start();
    }
}
