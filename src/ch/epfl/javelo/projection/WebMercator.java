package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

/**
 * Allows converting between WGS84 and Web Mercator coordinates
 *
 * @author Edouard Mignan (345875)
 */
public final class WebMercator {

    private WebMercator() {
    }

    /**
     * Converts a longitude (degrees) in Web Mercator coordinates
     *
     * @param lon longitude (degrees)
     * @return longitude in Web Mercator coordinates
     */
    public static double x(double lon) {
        return (lon + Math.PI) * (1D / (2 * Math.PI));
    }

    /**
     * Converts a latitude (degrees) in Web Mercator coordinates
     *
     * @param lat longitude (degrees)
     * @return longitude in Web Mercator coordinates
     */
    public static double y(double lat) {
        return (1D / (2 * Math.PI)) * (Math.PI - Math2.asinh(Math.tan(lat)));
    }

    /**
     * Converts x (Web Mercator) to longitude (degrees)
     *
     * @param x x coordinate
     * @return longitude in WGS84
     */
    public static double lon(double x) {
        return 2 * Math.PI * x - Math.PI;
    }

    /**
     * Converts y (Web Mercator) to latitude (degrees)
     *
     * @param y y coordinate
     * @return latitude in WGS84
     */
    public static double lat(double y) {
        return Math.atan(Math.sinh(Math.PI - 2 * Math.PI * y));
    }
}
