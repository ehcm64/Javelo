package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * Represents a point in the Web Mercator system
 * @author Edouard Mignan (345875)
 */

public record PointWebMercator(double x, double y) {

    public PointWebMercator {
        Preconditions.checkArgument(x <= 1 && x >= 0 && y <= 1 && y >= 0);
    }

    /**
     * Return a PointWebMercator without zoom
     * @param zoomLevel level of zoom
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @return the same point (PointWebMercator) cancelling zoom
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        double xUnzoomed = Math.scalb(x, -8 - zoomLevel);
        double yUnzoomed = Math.scalb(y, -8 - zoomLevel);
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
        return WebMercator.lon(this.x);
    }

    /**
     * Converts y coordinates of a PointWebMercator point in latitude (degrees)
     * @return latitude corresponding to the y coordinate
     */
    public double lat() {
        return WebMercator.lat(this.y);
    }

    /**
     * Zooms the x value at a certain level
     * @param zoomLevel
     * @return zooms x value at a certain level
     */
    public double xAtZoomLevel(int zoomLevel) {
        return Math.scalb(this.x, 8 + zoomLevel);
    }

    /**
     * Zooms the y value at a certain level
     * @param zoomLevel
     * @return zooms y value at a certain level
     */

    public double yAtZoomLevel(int zoomLevel) {
        return Math.scalb(this.y, 8 + zoomLevel);
    }

    /**
     * Converts a point in PointWebMercator coordinates in Swiss coordinates
     * @return point in Swiss coordinates
     */
    public PointCh toPointCh() {
        double n = Ch1903.n(lon(), lat());
        double e = Ch1903.e(lon(), lat());
        return new PointCh(e, n);
    }
}
