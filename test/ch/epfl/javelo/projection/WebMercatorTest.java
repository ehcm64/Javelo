package ch.epfl.javelo.projection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebMercatorTest {

    @Test
    void x() {
        assertEquals(0.518275214444, WebMercator.x(Math.toRadians(6.5790772)), 1e-6);
    }

    @Test
    void y() {
        assertEquals(0.353664894749, WebMercator.y(Math.toRadians(46.5218976)), 1e-6);
    }

    @Test
    void lon() {
        assertEquals(Math.toRadians(6.5790772), WebMercator.lon(0.518275214444), 1e-6);
    }

    @Test
    void lat() {
        assertEquals(Math.toRadians(46.5218976), WebMercator.lat(0.353664894749), 1e-6);
    }
}