package ch.epfl.javass.bonus;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Soundlines {

    private static AudioFormat audioFormat = new AudioFormat(16000f, 16, 1, true, false);
    private static TargetDataLine targetDataLine = getLineIn();
    private static SourceDataLine sourceDataLine = getLineOut();

    private Soundlines() { }

    public static TargetDataLine getInput() {
        return targetDataLine;
    }

    public static SourceDataLine getOutput() {
        return sourceDataLine;
    }

    public static AudioFormat getFormat() {
        return audioFormat;
    }

    private static TargetDataLine getLineIn() {
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            return targetDataLine;
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SourceDataLine getLineOut() {
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
            return sourceDataLine;
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }
}
