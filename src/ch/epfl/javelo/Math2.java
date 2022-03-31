package ch.epfl.javelo;

/**
 * Provides useful methods for mathematical calculations.
 *
 * @author Edouard Mignan (345875)
 */
public final class Math2 {

    private Math2() {
    }

    /**
     * Calculates the division of x by y and returns the closest superior integer to the result
     *
     * @param x the numerator
     * @param y the denominator
     * @return the result rounded to the closest higher integer
     * @throws IllegalArgumentException if x is negative or y is negative or equal to 0
     */
    public static int ceilDiv(int x, int y) {
        Preconditions.checkArgument(x >= 0 && y > 0);
        return (x + y - 1) / y;
    }

    /**
     * Calculates the interpolation of a point on a line drawn from two other points.
     *
     * @param y0 the y coordinate of the x = 0 point
     * @param y1 the y coordinate of the x = 1 point
     * @param x  the x coordinate of the point to interpolate on the line
     * @return the y coordinate of the point interpolated on the line
     */
    public static double interpolate(double y0, double y1, double x) {
        return Math.fma(y1 - y0, x, y0);
    }

    /**
     * Clamps a given value between a minimum and a maximum.
     *
     * @param min the minimum value
     * @param v   the given value
     * @param max the maximum value
     * @return original value clamped between the minimum and maximal values
     * @throws IllegalArgumentException if minimum value is greater than maximum value
     */
    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(max >= min);
        if (v < min) return min;
        else return Math.min(v, max);
    }

    /**
     * Clamps a given value between a minimum and a maximum.
     *
     * @param min the minimum value
     * @param v   the given value
     * @param max the maximum value
     * @return original value clamped between the minimum and maximal values
     * @throws IllegalArgumentException if minimum value is greater than maximum value
     */
    public static double clamp(double min, double v, double max) {
        Preconditions.checkArgument(max >= min);
        if (v < min) return min;
        else return Math.min(v, max);
    }

    /**
     * Calculates the inverse of hyperbolic sine function.
     *
     * @param x the value to input
     * @return the hyperbolic sine inverse of x
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + (x * x)));
    }

    /**
     * Calculates the dot product of two vectors.
     *
     * @param uX the x coordinate of the first vector
     * @param uY the y coordinate of the first vector
     * @param vX the x coordinate of the second vector
     * @param vY the y coordinate of the second vector
     * @return the dot product
     */
    public static double dotProduct(double uX, double uY, double vX, double vY) {
        return Math.fma(uX, vX, uY * vY);
    }

    /**
     * Calculates the square of the norm of a vector.
     *
     * @param uX the x coordinate of the vector
     * @param uY the y coordinate of the vector
     * @return the squared norm.
     */
    public static double squaredNorm(double uX, double uY) {
        return dotProduct(uX, uY, uX, uY);
    }

    /**
     * Calculates the norm of a vector.
     *
     * @param uX the x coordinate of the vector
     * @param uY the y coordinate of the vector
     * @return the norm
     */
    public static double norm(double uX, double uY) {
        return Math.sqrt(squaredNorm(uX, uY));
    }

    /**
     * Calculates the length of the projection of a vector u (AP) on a vector v (AB).
     *
     * @param aX the x coordinate of point A
     * @param aY the y coordinate of point A
     * @param bX the x coordinate of point B
     * @param bY the y coordinate of point B
     * @param pX the x coordinate of point P
     * @param pY the y coordinate of point P
     * @return the length of the projection
     */
    public static double projectionLength(double aX, double aY,
                                          double bX, double bY,
                                          double pX, double pY) {
        double uX = pX - aX;
        double uY = pY - aY;

        double vX = bX - aX;
        double vY = bY - aY;

        return dotProduct(uX, uY, vX, vY) / norm(vX, vY);
    }
}
