package ch.epfl.javelo;

public final class Math2 {
    
    private Math2() {}

    public static int ceilDiv(int x, int y) {
        if (x < 0 || y <= 0) {
            throw new IllegalArgumentException();
        } else return (x+y-1)/y;
    }

    public static double interpolate(double y0, double y1, double x) {
        return Math.fma(y1 - y0, x, y0);
    }

    public static int clamp (int min, int v, int max) {
        if (min > max) throw new IllegalArgumentException();
        else {
            if (v < min) return min;
            else if (v > max) return max;
            else return v;
        }
    }

    public static double clamp (double min, double v, double max) {
        if (min > max) throw new IllegalArgumentException();
        else {
            if (v < min) return min;
            else if (v > max) return max;
            else return v;
        }
    }

    public static double asinh (double x) {
        return Math.log(x + Math.sqrt(1 + x*x));
    }

    public static double dotProduct(double uX, double uY, double vX, double vY) {
        return Math.fma(uX, vX, uY * vY);
    }

    public static double squaredNorm (double uX, double uY) {
        return dotProduct(uX, uY, uX, uY);
    }

    public static double norm (double uX, double uY) {
        return Math.sqrt(squaredNorm(uX, uY));
    }

    public static double projectionLength (double aX, double aY, double bX, double bY, double pX, double pY) {
        double uX = pX - aX;
        double uY = pY - aY;

        double vX = bX - aX;
        double vY = bY - aY;

        return dotProduct(uX, uY, vX, vY)/norm(vX, vY);
    }
}
