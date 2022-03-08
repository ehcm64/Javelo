package ch.epfl.javelo;

/**
 * Allows converting between Integer and Q28.4 coordinates
 * @author Edouard Mignan (345875)
 */
public final class Q28_4 {

    private Q28_4() {
    }

    /**
     * Converts an Integer in Q28.4 notation
     * @param i 32-bit vector
     * @return the vector in Q28.4 notation
     */
    public static int ofInt(int i) {
        return i << 4;
    }

    /**
     * Converts a Q28.4 vector in an Integer
     * @param q28_4 32-bit vector
     * @return the vector as double
     */
    public static double asDouble(int q28_4) {
        return Math.scalb((double) q28_4, -4);
    }

    /**
     * Converts a Q28.4 vector in a double
     * @param q28_4 32-bit vector
     * @return the vector as float
     */
    public static float asFloat(int q28_4) {
        return Math.scalb((float) q28_4, -4);
    }
}