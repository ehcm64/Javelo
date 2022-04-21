package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapViewParametersTest {

    @Test
    void testXYReversal() {
        double xTopLeft = 4.2;
        double yTopLeft = 4.6;
        double xCoord = 3;
        double yCoord = 4;
        MapViewParameters mvp = new MapViewParameters(4, xTopLeft, yTopLeft);
        PointWebMercator pwm = mvp.pointAt(xCoord, yCoord);
        assertEquals(xCoord, mvp.viewX(pwm));
        assertEquals(yCoord, mvp.viewY(pwm));
    }
}