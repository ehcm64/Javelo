package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

public final class WebMercator {

    private WebMercator() {
    }

    public static double x(double lon) {
        return (Math.toRadians(lon) + Math.PI) * (1D / (2 * Math.PI));
    }

    public static double y(double lat) {
        return (1D / (2 * Math.PI)) * (Math.PI - Math2.asinh(Math.tan(Math.toRadians(lat))));
    }

    public static double lon(double x) {
        return Math.toDegrees(2 * Math.PI * x - Math.PI);
    }

    public static double lat(double y) {
        return Math.toDegrees(Math.atan(Math.sinh(Math.PI - 2 * Math.PI * y)));
    }
}
