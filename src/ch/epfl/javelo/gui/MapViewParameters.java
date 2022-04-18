package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

public record MapViewParameters(int zoomLevel, int xTopLeft, int yTopLeft) {

    public Point2D topLeft() {
        return new Point2D(xTopLeft, yTopLeft);
    }

    public MapViewParameters withMinXY(int x, int y) {
        return new MapViewParameters(zoomLevel, x, y);
    }

    public PointWebMercator pointAt(int x, int y) {
        return PointWebMercator.of(zoomLevel, xTopLeft + x, yTopLeft + y);
    }

    public int viewX(PointWebMercator point) {
        return (int) (point.xAtZoomLevel(zoomLevel) - xTopLeft);
    }

    public int viewY(PointWebMercator point) {
        return (int) (point.yAtZoomLevel(zoomLevel) - yTopLeft);
    }
}
