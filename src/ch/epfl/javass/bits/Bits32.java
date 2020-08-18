package ch.epfl.javass.bits;

import ch.epfl.javass.Preconditions;

/**
 * Class that provides static methods for the int representing the packed Card
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class Bits32 {

    // Non instanciable
    private Bits32() {
    }

    /**
     * Create a 32 byte vector with a mask at certain places, return as int
     *
     * @param start index to start at
     * @param size  length of mask
     * @return int representing the 32 byte mask
     */
    public static int mask(int start, int size) {
        Preconditions.checkArgument(start >= 0 && size >= 0 && start + size <= Integer.SIZE);
        // Exception to our method
        if (size == Integer.SIZE) return -1;
        return (((1 << size) - 1) << start);
    }

    /**
     * Extracts from a vector of 32 bits the number between start(included) and
     * start + size(not included)
     *
     * @param bits  int from which to extract value
     * @param start starting index of the value
     * @param size  length of the value
     * @return int representing the value at the specified index and size
     */
    public static int extract(int bits, int start, int size) {
        Preconditions.checkArgument(start >= 0 && size >= 0 && start + size <= Integer.SIZE);
        return ((bits & mask(start, size)) >>> start);
    }

    /**
     * Packs the two values into a int representing a 32 bit vector.
     *
     * @param v1 value
     * @param s1 size allocated
     * @return int in which values are packed
     */
    public static int pack(int v1, int s1, int v2, int s2) {
        Preconditions.checkArgument(checkPaires(v1, s1) && checkPaires(v2, s2) && s1 + s2 <= Integer.SIZE);
        return (v2 << s1) + v1;
    }

    /**
     * Packs the three values into a int representing a 32 bit vector.
     *
     * @param v1 value
     * @param s1 size allocated
     * @return int in which values are packed
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
        Preconditions
                .checkArgument(checkPaires(v1, s1) && checkPaires(v2, s2) && checkPaires(v3, s3) && s1 + s2 + s3 <= Integer.SIZE);
        return (v3 << (s1 + s2)) + (v2 << s1) + v1;
    }

    /**
     * Packs the seven values into a int representing a 32 bit vector.
     *
     * @param v1 value
     * @param s1 size allocated
     * @return int in which values are packed
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3, int v4, int s4, int v5, int s5, int v6,
                           int s6, int v7, int s7) {
        Preconditions.checkArgument(checkPaires(v1, s1) && checkPaires(v2, s2) && checkPaires(v3, s3)
                && checkPaires(v6, s6) && checkPaires(v4, s4) && checkPaires(v5, s5) && checkPaires(v6, s6)
                && checkPaires(v7, s7) && s1 + s2 + s3 + s4 + s5 + s6 + s7 <= Integer.SIZE);
        v2 = v2 << s1;
        v3 = v3 << (s1 + s2);
        v4 = v4 << (s1 + s2 + s3);
        v5 = v5 << (s1 + s2 + s3 + s4);
        v6 = v6 << (s1 + s2 + s3 + s4 + s5);
        v7 = v7 << (s1 + s2 + s3 + s4 + s5 + s6);
        return v7 + v6 + v5 + v4 + v3 + v2 + v1;

    }

    /**
     * Checks if size is between 0 and 31, if a value is greater than its size
     *
     * @param value value wanted to be packed
     * @param size  size wanted to be allocated
     * @return true if the size allocated is big enough for the given value
     */
    private static boolean checkPaires(int value, int size) {
        Preconditions.checkIndex(size, Integer.SIZE);
        return (1 << size - 1) >= Integer.highestOneBit(value);
    }
}
