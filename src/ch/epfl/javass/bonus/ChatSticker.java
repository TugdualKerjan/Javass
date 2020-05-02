package ch.epfl.javass.bonus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ChatSticker {
    
    SEXY,
    REKT,
    LMAO,
    YEET,
    NONE,
    SOUND;
    
    public static final List<ChatSticker> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    public static final int COUNT = ALL.size();
}
