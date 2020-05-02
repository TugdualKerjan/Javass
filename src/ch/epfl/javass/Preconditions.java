package ch.epfl.javass;

/**
 * Class to verify certain things like indexes etc before moving on
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class Preconditions {

    // Non instantiable
    private Preconditions() {
    }

    /**
     * Checks if the boolean is false, and throws and exception if so
     * 
     * @param b
     *            if false throws IllegalArgumentException
     */
    public static void checkArgument(boolean b) {
        if (b == false)
            throw new IllegalArgumentException();
    }

    /**
     * Checks if index is correctly in the range of 0 to size
     * 
     * @param index
     *            at which we look
     * @param size
     *            size of the total selectable area
     * @return true if index is positive and less than size
     */
    public static int checkIndex(int index, int size) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();
        else
            return index;
    }

}
