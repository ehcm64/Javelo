package ch.epfl.javelo.projection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointWebMercatorTest {

    @Test
    void of() {
        assertEquals(0.518275214444, PointWebMercator.of(19, 69561722, 47468099).x(), 1e-6);
        assertEquals(0.353664894749, PointWebMercator.of(19, 69561722, 47468099).y(), 1e-6);
    }

    @Test
    void ofPointCh() {
        PointCh point = new PointCh(Ch1903.e(Math.toRadians(6.5790772), Math.toRadians(46.5218976)), Ch1903.n(Math.toRadians(6.5790772), Math.toRadians(46.5218976)));
        assertEquals(0.518275214444, PointWebMercator.ofPointCh(point).x(), 1e-3);
        assertEquals(0.353664894749, PointWebMercator.ofPointCh(point).y(), 1e-3);
    }

    @Test
    void lon() {
        assertEquals(Math.toRadians(6.5790772), new PointWebMercator(0.518275214444, 0.353664894749).lon(), 1e-6);
    }

    @Test
    void lat() {
        assertEquals(Math.toRadians(46.5218976), new PointWebMercator(0.518275214444, 0.353664894749).lat(), 1e-6);
    }

    @Test
    void xAtZoomLevel() {
        assertEquals(69561722, new PointWebMercator(0.518275214444, 0.353664894749).xAtZoomLevel(19), 1);
    }

    @Test
    void yAtZoomLevel() {
        assertEquals(47468099, new PointWebMercator(0.518275214444, 0.353664894749).yAtZoomLevel(19), 1);
    }

    @Test
    void toPointCh() {
        assertEquals(Ch1903.e(Math.toRadians(6.5790772), Math.toRadians(46.5218976)),
                new PointWebMercator(0.518275214444, 0.353664894749).toPointCh().e(), 1e-3);
        assertEquals(Ch1903.n(Math.toRadians(6.5790772), Math.toRadians(46.5218976)), new PointWebMercator(0.518275214444, 0.353664894749).toPointCh().n(), 1e-3);
    }

    @Test
    void x() {
        assertEquals(0.518275214444, new PointWebMercator(0.518275214444, 0.353664894749).x(), 1e-6);
    }

    @Test
    void y() {
        assertEquals(0.353664894749, new PointWebMercator(0.518275214444, 0.353664894749).y(), 1e-6);
    }
}