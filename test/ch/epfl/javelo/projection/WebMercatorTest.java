package ch.epfl.javelo.projection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebMercatorTest {

    @Test
    void x() {
        assertEquals(0.75, WebMercator.x(Math.PI / 2), 1e-6);
    }

    @Test
    void y() {
        assertEquals(0.3597250369, WebMercator.y(Math.PI / 4), 1e-6);
    }

    @Test
    void lon() {
        assertEquals(Math.PI / 2, WebMercator.lon(0.75), 1e-6);
    }

    @Test
    void lat() {
        assertEquals(Math.PI / 4, WebMercator.lat(0.359725037), 1e-6);
    }
}