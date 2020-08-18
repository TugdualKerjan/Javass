package ch.epfl.javass.net;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.StringJoiner;

/**
 * This class StringSerializer will be used by the RemotePlayerClient and
 * RemotePlayerServer to send information under the form of strings through the
 * streams without having any problems of encoding. It provides also some extra
 * methods to work more easily with arrays of String. So its a class that will
 * only be used for its static methods.
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class StringSerializer {
    private final static int HEXADECIMAL_BASE = 16;

    // Non-instanciable
    private StringSerializer() {
    }

    /**
     * Go from int to serialized string
     *
     * @param int to serialize
     * @return serialized int
     */
    public static String serializeInt(int number) {
        return Integer.toUnsignedString(number, HEXADECIMAL_BASE);
    }

    /**
     * Go from long to serialized string
     *
     * @param long to serialize
     * @return serialized long
     */
    public static String serializeLong(long number) {
        return Long.toUnsignedString(number, HEXADECIMAL_BASE);
    }

    /**
     * Go from serialized string to int
     *
     * @param int to deserialize
     * @return deserialized int
     */
    public static int deserializeInt(String number) {
        return Integer.parseUnsignedInt(number, HEXADECIMAL_BASE);
    }

    /**
     * Go from serialized string to long
     *
     * @param long to deserialize
     * @return deserialized long
     */
    public static long deserializeLong(String number) {
        return Long.parseUnsignedLong(number, HEXADECIMAL_BASE);
    }

    /**
     * Go from string to serialized string
     *
     * @param string to serialize
     * @return serialized string
     */
    public static String serializeString(String characters) {
        byte[] bytes = characters.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Go from serialized string to string
     *
     * @param string to deserialize
     * @return deserialized string
     */
    public static String deserializeString(String characters) {
        return new String(Base64.getDecoder().decode(characters.getBytes()));
    }

    /**
     * Combine multiple strings into one with a defined seperator
     *
     * @param seperator char to seperate
     * @param strings   to combine
     * @return string of combined strings
     */
    public static String combine(char seperator, String... strings) {
        StringJoiner joiner = new StringJoiner(String.valueOf(seperator), "",
                "");
        for (String s : strings) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    /**
     * Split the string based on a char seperator
     *
     * @param string    to split
     * @param seperator
     * @return array of each split segment
     */
    public static String[] split(String string, char seperator) {
        return string.split(String.valueOf(seperator));
    }
}