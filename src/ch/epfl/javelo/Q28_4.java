package ch.epfl.javelo;

/**
 * Allows converting between normal integer and Q28.4 number format.
 *
 * @author Edouard Mignan (345875)
 */
public final class Q28_4 {
    private static final int SHIFT_DISTANCE = 4;

    private Q28_4() {
    }

    /**
     * Converts an Integer in Q28.4 notation
     * @param i 32-bit vector
     * @return the vector in Q28.4 notation
     */
    public static int ofInt(int i) {
        return i << SHIFT_DISTANCE;
    }

    /**
     * Converts a Q28.4 vector into its value as a double.
     * @param q28_4 32-bit vector
     * @return the vector as double
     */
    public static double asDouble(int q28_4) {
        return Math.scalb((double) q28_4, -SHIFT_DISTANCE);
    }

    /**
     * Converts a Q28.4 vector into its value as a float.
     * @param q28_4 32-bit vector
     * @return the vector as float
     */
    public static float asFloat(int q28_4) {
        return Math.scalb((float) q28_4, -SHIFT_DISTANCE);
    }
}