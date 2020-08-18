package ch.epfl.javass.bonus;

import ch.epfl.javass.jass.PlayerId;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class StickerBean {

    private static SimpleBooleanProperty onlineBoolean = new SimpleBooleanProperty(false);
    private static SimpleBooleanProperty voiceBoolean = new SimpleBooleanProperty(false);
    private static ObservableMap<PlayerId, ChatSticker> sticker = addPlayers();

    public static ObservableMap<PlayerId, ChatSticker> stickerProperty() {
        return sticker;
    }

    public static void setSticker(PlayerId player, ChatSticker newSticker) {
        sticker.put(player, newSticker);
    }

    public static void setVoiceBoolean(boolean newBool) {
        voiceBoolean.set(newBool);
    }

    public static ReadOnlyBooleanProperty booleanProperty() {
        return voiceBoolean;
    }

    public static void setOnlineBoolean(boolean newBool) {
        onlineBoolean.set(newBool);
    }

    public static ReadOnlyBooleanProperty onlineProperty() {
        return onlineBoolean;
    }

    private static ObservableMap<PlayerId, ChatSticker> addPlayers() {
        ObservableMap<PlayerId, ChatSticker> bob = FXCollections.<PlayerId, ChatSticker>observableHashMap();
        bob.put(PlayerId.PLAYER_1, null);
        bob.put(PlayerId.PLAYER_2, null);
        bob.put(PlayerId.PLAYER_3, null);
        bob.put(PlayerId.PLAYER_4, null);
        return bob;
    }
}