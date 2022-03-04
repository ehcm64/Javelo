package ch.epfl.javelo.projection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebMercatorTest {

    @Test
    void x() {
        assertEquals(0.518275214444, WebMercator.x(6.5790772), 1e-6);
    }

    @Test
    void y() {
        assertEquals(0.353664894749, WebMercator.y(46.5218976), 1e-6);
    }

    @Test
    void lon() {
        assertEquals(6.5790772, WebMercator.lon(0.518275214444), 1e-6);
    }

    @Test
    void lat() {
        assertEquals(46.5218976, WebMercator.lat(0.353664894749), 1e-6);
    }
}