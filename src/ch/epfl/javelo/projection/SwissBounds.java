package ch.epfl.javelo.projection;

/**
 * Contains constants and methods linked to geographical limits of Switzerland.
 *
 * @author Edouard Mignan (345875)
 */
public final class SwissBounds {

    private SwissBounds() {}

    /**
     * Smallest east coordinate of Switzerland.
     */
    public static final double MIN_E = 2_485_000;

    /**
     * Greatest east coordinate of Switzerland.
     */
    public static final double MAX_E = 2_834_000;

    /**
     * Smallest north coordinate of Switzerland.
     */
    public static final double MIN_N = 1_075_000;

    /**
     * Greatest north coordinate of Switzerland.
     */
    public static final double MAX_N = 1_296_000;

    /**
     * Width of Switzerland.
     */
    public static final double WIDTH = MAX_E - MIN_E;
    /**
     * Height of Switzerland.
     */
    public static final double HEIGHT = MAX_N - MIN_N;

    /**
     * Checks if given coordinates of a point are inside Switzerland.
     *
     * @param e the east coordinate of the point
     * @param n the north coordinate of the point
     * @return true if the point is in Switzerland, false otherwise
     */
    public static boolean containsEN(double e, double n) {
        return e >= MIN_E && e <= MAX_E && n >= MIN_N && n <= MAX_N;
    }
}
