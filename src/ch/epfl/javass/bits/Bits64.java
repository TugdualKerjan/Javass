package ch.epfl.javass.bits;

import ch.epfl.javass.Preconditions;

/**
 * Class that provides static methods to do operations with the long that
 * represents the Scores
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class Bits64 {

    /**
     * Class non instantiable
     */
    private Bits64() {
    }

    /**
     * Method that returns a long that would have size ones starting at start in the
     * binary representation
     *
     * @param start index ( place at where you wish to have the first one)
     * @param size  (number of ones you want)
     * @return a long that would have size ones starting at start in the binary
     * representation
     * @throws Illegal Argument Exception if parameters are wrong
     */
    public static long mask(int start, int size) {
        Preconditions.checkArgument(start >= 0 && size >= 0 && start + size <= 64);
        // Exception to our method
        if (size == 64)
            return 0b1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111L;
        long number = 1L;
        number = (number << size) - 1L;
        number = (number << start);
        return number;
    }

    /**
     * Extracts from a vector of 64 bits the number between start(included) and
     * start + size(not included)
     *
     * @param bits  long from which to extract value
     * @param start starting index of the value
     * @param size  length of the value
     * @return long representing the value at the specified index and size
     */
    public static long extract(long bits, int start, int size) {
        Preconditions.checkArgument(start >= 0 && size >= 0 && start + size <= 64);
        long number = bits;
        number = number & mask(start, size);
        number = (number >> start);
        return number;
    }

    /**
     * Packs the two values into an long representing a 64 bit vector.
     *
     * @param v1 value
     * @param s1 size allocated
     * @return long in which values are packed
     */
    public static long pack(long v1, int s1, long v2, int s2) {
        Preconditions.checkArgument(checkPaires(v1, s1) && checkPaires(v2, s2) && s1 + s2 <= 64);
        v2 = v2 << s1;
        return v2 + v1;
    }

    /**
     * Checks if size is between 0 and 63, if a value is greater than its size
     *
     * @param value value wanted to be packed
     * @param size  size wanted to be allocated
     * @return true if the size allocated is big enough for the given value
     */
    private static boolean checkPaires(long value, int size) {
        Preconditions.checkIndex(size, 64);
        return (1L << size - 1 >= Long.highestOneBit(value));
    }

}
