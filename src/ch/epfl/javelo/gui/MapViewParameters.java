package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

public record MapViewParameters(int zoomLevel, double xTopLeft, double yTopLeft) {

    public Point2D topLeft() {
        return new Point2D(xTopLeft, yTopLeft);
    }

    public MapViewParameters withMinXY(double x, double y) {
        return new MapViewParameters(zoomLevel, x, y);
    }

    public PointWebMercator pointAt(double x, double y) {
        return PointWebMercator.of(zoomLevel, xTopLeft + x, yTopLeft + y);
    }

    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoomLevel) - xTopLeft;
    }

    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - yTopLeft;
    }
}
