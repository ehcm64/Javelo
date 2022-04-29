package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * Represents a point in the Web Mercator system
 * @author Edouard Mignan (345875)
 */

public record PointWebMercator(double x, double y) {
    private static final int TILE_LENGTH = 8; // Tile length is 2^8 pixels.

    public PointWebMercator {
        Preconditions.checkArgument(0 <= x && x <= 1 && 0 <= y && y <= 1);
    }

    /**
     * Return a PointWebMercator without zoom
     * @param zoomLevel level of zoom
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @return the same point (PointWebMercator) cancelling zoom
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        double xUnzoomed = Math.scalb(x, -TILE_LENGTH - zoomLevel);
        double yUnzoomed = Math.scalb(y, -TILE_LENGTH - zoomLevel);
        return new PointWebMercator(xUnzoomed, yUnzoomed);
    }

    /**
     * Converts a point in Swiss coordinates to PointWebMercator coordinates
     * @param pointCh point in Swiss coordinates
     * @return same point but in Point WebMercator coordinates
     */
    public static PointWebMercator ofPointCh(PointCh pointCh) {
        double longitude = pointCh.lon();
        double latitude = pointCh.lat();
        double x = WebMercator.x(longitude);
        double y = WebMercator.y(latitude);
        return new PointWebMercator(x, y);
    }

    /**
     * Converts x coordinates of a PointWebMercator point in longitude
     * @return longitude corresponding to the x coordinate
     */
    public double lon() {
        return WebMercator.lon(x);
    }

    /**
     * Converts y coordinates of a PointWebMercator point in latitude (degrees)
     * @return latitude corresponding to the y coordinate
     */
    public double lat() {
        return WebMercator.lat(y);
    }

    /**
     * Zooms the x value at a certain level
     * @param zoomLevel the zoom level
     * @return zooms x value at a certain level
     */
    public double xAtZoomLevel(int zoomLevel) {
        return Math.scalb(x, TILE_LENGTH + zoomLevel);
    }

    /**
     * Zooms the y value at a certain level
     * @param zoomLevel the zoom level
     * @return zooms y value at a certain level
     */

    public double yAtZoomLevel(int zoomLevel) {
        return Math.scalb(y, TILE_LENGTH + zoomLevel);
    }

    /**
     * Converts a point in PointWebMercator coordinates in Swiss coordinates
     * @return point in Swiss coordinates
     */
    public PointCh toPointCh() {
        double n = Ch1903.n(lon(), lat());
        double e = Ch1903.e(lon(), lat());
        return SwissBounds.containsEN(e, n) ? new PointCh(e, n) : null;
    }
}
