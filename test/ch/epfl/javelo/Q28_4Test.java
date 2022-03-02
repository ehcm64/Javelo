package ch.epfl.javelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Q28_4Test {

    @Test
    void ofIntWorks() {
        assertEquals(0b100000, Q28_4.ofInt(0b10));
    }

    @Test
    void asDoubleWorks() {
        assertEquals(6.25, Q28_4.asDouble(100), 1e-6);
    }

    @Test
    void asFloatWorks() {
        assertEquals(6.25, Q28_4.asFloat(100), 1e-6);
    }
}