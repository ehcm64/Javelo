package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Represents the parameters of the background map
 *
 * @param zoomLevel zoom level of the map
 * @param xTopLeft  x coordinate of the top left corner
 * @param yTopLeft  y coordinate of the top left corner
 * @author Timo Moebel (345665)
 */
public record MapViewParameters(int zoomLevel, double xTopLeft, double yTopLeft) {
    /**
     * Returns the coordinates of the top-left corner in form of Swiss coordinates
     * @return a Point2D from coordinates of the top-left corner
     */
    public Point2D topLeft() {
        return new Point2D(xTopLeft, yTopLeft);
    }

    /**
     * Returns a new background map with the same zoom level
     * but new x and y coordinates
     * @param x x coordinate of the new top left corner
     * @param y y coordinate of the new top left corner
     * @return a new background map
     */
    public MapViewParameters withMinXY(double x, double y) {
        return new MapViewParameters(zoomLevel, x, y);
    }

    /**
     * Returns a point in the Web Mercator system from two coordinates according
     * to the zoom level and the top left corner coordinates
     * @param x x coordinate expressed in relation to the top-left corner
     *          of the map portion displayed on the screen
     * @param y y coordinate expressed in relation to the top-left corner
     *          of the map portion displayed on the screen
     * @return a point in the Web Mercator system from two coordinates according
     *           to the zoom level and the top left corner coordinates
     */
    public PointWebMercator pointAt(double x, double y) {
        return PointWebMercator.of(zoomLevel, xTopLeft + x, yTopLeft + y);
    }

    /**
     * Returns the x coordinate in pixels of a point
     * represented in the Web Mercator system
     * @param point the point in the Web Mercator system
     * @return the x coordinate expressed in relation to the top-left corner
     *          of the map portion displayed on the screen
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoomLevel) - xTopLeft;
    }

    /**
     * Returns the y coordinate in pixels of a point
     * represented in the Web Mercator system
     * @param point the point in the Web Mercator system
     * @return the y coordinate expressed in relation to the top-left corner
     *          of the map portion displayed on the screen
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - yTopLeft;
    }
}
