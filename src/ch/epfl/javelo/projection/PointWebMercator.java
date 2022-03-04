package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

public record PointWebMercator(double x, double y) {

    public PointWebMercator {
        Preconditions.checkArgument(x <= 1 && x >= 0 && y <= 1 && y >= 0);
    }

    public static PointWebMercator of(int zoomLevel, double x, double y) {
        double xUnzoomed = Math.scalb(x, -8 - zoomLevel);
        double yUnzoomed = Math.scalb(y, -8 - zoomLevel);
        return new PointWebMercator(xUnzoomed, yUnzoomed);
    }

    public static PointWebMercator ofPointCh(PointCh pointCh) {
        double longitude = pointCh.lon();
        double latitude = pointCh.lat();
        double x = WebMercator.x(longitude);
        double y = WebMercator.y(latitude);
        return new PointWebMercator(x, y);

    }

    public double lon() {
        return WebMercator.lon(x);
    }

    public double lat() {
        return WebMercator.lat(y);
    }

    public double xAtZoomLevel(int zoomLevel) {
        return Math.scalb(this.x, 8 + zoomLevel);
    }

    public double yAtZoomLevel(int zoomLevel) {
        return Math.scalb(this.y, 8 + zoomLevel);
    }

    public PointCh toPointCh() {
        double n = Ch1903.n(lon(), lat());
        double e = Ch1903.e(lon(), lat());
        return new PointCh(e, n);
    }

}
