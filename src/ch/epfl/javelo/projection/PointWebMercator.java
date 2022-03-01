package ch.epfl.javelo.projection;

public record PointWebMercator(double x, double y) {

    public PointWebMercator {
        if (x > 1 || x < 0 || y > 1 || y < 0) {
            throw new IllegalArgumentException();
        }
    }

    public static PointWebMercator of(int zoomLevel, double x, double y) {
        PointWebMercator point = new PointWebMercator(x, y);
        double xZoomed = point.xAtZoomLevel(zoomLevel);
        double yZoomed = point.yAtZoomLevel(zoomLevel);
        return new PointWebMercator(xZoomed, yZoomed);

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
        return Math.scalb(y, 8 + zoomLevel);
    }

    public PointCh toPointCh() {
        double longitude = lon();
        double latitude = lat();
        double n = Ch1903.n(longitude, latitude);
        double e = Ch1903.e(longitude, latitude);
        return new PointCh(e, n);
    }

}
