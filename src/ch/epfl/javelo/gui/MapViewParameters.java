package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Represents the parameters of the view of the map.
 *
 * @author Timo Moebel (345665)
 *
 * @param zoomLevel the zoom level of the map view
 * @param xTopLeft  the x coordinate of the pixel in the top left corner
 *                  of the scene in the web mercator system
 * @param yTopLeft  the y coordinate of the pixel in the top left corner
 *                  of the scene in the web mercator system
 */
public record MapViewParameters(int zoomLevel, double xTopLeft, double yTopLeft) {

    /**
     * Returns a 2D-Point with the coordinates of the top left corner.
     *
     * @return the point
     */
    public Point2D topLeft() {
        return new Point2D(xTopLeft, yTopLeft);
    }

    /**
     * Returns new map view parameters at the same zoom level
     * but with the given top left corner coordinates.
     *
     * @param x the x coordinate of the top left corner
     * @param y the y coordinate of the top left corner
     * @return the new map view parameters
     */
    public MapViewParameters withMinXY(double x, double y) {
        return new MapViewParameters(zoomLevel, x, y);
    }

    /**
     * Returns the point in the web mercator system associated to the
     * given coordinates in the map view system.
     *
     * @param x the x coordinates in the map view system
     * @param y the y coordinates in the map view system
     * @return the point in the web mercator system
     */
    public PointWebMercator pointAt(double x, double y) {
        return PointWebMercator.of(zoomLevel, xTopLeft + x, yTopLeft + y);
    }

    /**
     * Returns the x coordinate in the map view system of a point
     * in the web mercator system.
     *
     * @param point the point
     * @return the x coordinate
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoomLevel) - xTopLeft;
    }

    /**
     * Returns the y coordinate in the map view system of a point
     * in the web mercator system.
     *
     * @param point the point
     * @return the y coordinate
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - yTopLeft;
    }
}
