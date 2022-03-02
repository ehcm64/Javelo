package ch.epfl.javelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FunctionsTest {

    @Test
    void constantWorks() {
        assertEquals(Math.PI, Functions.constant(Math.PI).applyAsDouble(5), 1e-6);
    }

    @Test
    void sampledWorks() {
        float[] samples = {9, 6, 3};
        assertEquals(4.5, Functions.sampled(samples, 4).applyAsDouble(3), 1e-6);
    }

    @Test
    void sampledWorksWithx0() {
        float[] samples = {2, 6};
        assertEquals(2, Functions.sampled(samples, 2).applyAsDouble(0), 1e-6);
    }

    @Test
    void sampledWorksWithxMax() {
        float[] samples = {2, 6};
        assertEquals(6, Functions.sampled(samples, 2).applyAsDouble(2), 1e-6);
    }
}