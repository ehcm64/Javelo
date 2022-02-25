package ch.epfl.javelo.projection;

/**
 * Allows conversions between WGS 84 and Swiss coordinates.
 *
 * @author Timo Moebel (345665)
 */
public final class Ch1903 {

    private Ch1903() {
    }

    /**
     * Calculates North coordinate of a point from its longitude and latitude.
     *
     * @param lon the longitude of the point
     * @param lat the latitude of the point
     * @return the north coordinate of the point
     */
    public static double n(double lon, double lat) {
        lon = Math.toDegrees(lon);
        lat = Math.toDegrees(lat);
        double lon1 = 1e-4 * (3600 * lon - 26782.5);
        double lat1 = 1e-4 * (3600 * lat - 169028.66);
        double nCoord = 1_200_147.07 + 308_807.95 * lat1 + 3_745.25 * lon1 * lon1 +
                76.63 * lat1 * lat1 - 194.56 * lon1 * lon1 * lat1 + 119.79 * lat1 * lat1 * lat1;
        return nCoord;
    }

    /**
     * Calculates East coordinate of a point from its longitude and latitude.
     *
     * @param lon the longitude of the point
     * @param lat the latitude of the point
     * @return the east coordinate of the point
     */
    public static double e(double lon, double lat) {
        lon = Math.toDegrees(lon);
        lat = Math.toDegrees(lat);
        double lon1 = 1e-4 * (3600 * lon - 26782.5);
        double lat1 = 1e-4 * (3600 * lat - 169028.66);
        double eCoord = 2600072.37 + 211455.93 * lon1 - 10938.51 * lon1 * lat1
                - 0.36 * lon1 * lat1 * lat1 - 44.54 * lon1 * lon1 * lon1;

        return eCoord;
    }

    /**
     * Calculates Longitude of a point from its east and north coordinates.
     *
     * @param e the east coordinate of the point
     * @param n the north coordinate of the point
     * @return the longitude of the point
     */
    public static double lon(double e, double n) {
        double x = 1e-6 * (e - 2600000);
        double y = 1e-6 * (n - 1200000);
        double lon0 = 2.6779094 + 4.728982 * x + 0.791484 * x * y + 0.1306 * x * y * y - 0.0436 * x * x * x;
        double lon = lon0 * 100.0 / 36.0;
        lon = Math.toRadians(lon);
        return lon;
    }

    /**
     * Calculates Latitude of a point from its east and north coordinates.
     *
     * @param e the east coordinate of the point
     * @param n the north coordinate of the point
     * @return the latitude of the point
     */
    public static double lat(double e, double n) {
        double x = 1e-6 * (e - 2600000);
        double y = 1e-6 * (n - 1200000);
        double lat0 = 16.9023892 + 3.238272 * y - 0.270978 * x * x - 0.002528 * y * y - 0.0447 * x * x * y - 0.0140 * y * y * y;
        double lat = lat0 * 100.0 / 36.0;
        lat = Math.toRadians(lat);
        return lat;
    }
}
