package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapViewParametersTest {

    @Test
    void testXYReversal() {
        double x = 4.2;
        double y = 4.6;
        MapViewParameters mvp = new MapViewParameters(4, x, y);
        PointWebMercator pwm = mvp.pointAt(3, 4);
        assertEquals(3, mvp.viewX(pwm));
        assertEquals(4, mvp.viewY(pwm));
    }
}