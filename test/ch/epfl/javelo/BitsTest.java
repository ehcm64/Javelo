package ch.epfl.javelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitsTest {

    @Test
    void extractSignedWorks() {
        assertEquals(-8, Bits.extractSigned(8, 0, 4));
    }

    @Test
    void extractSignedThrowsOnNullLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractSigned(8, 2, 0);
        });
    }

    @Test
    void extractUnsignedWorks() {
        assertEquals(0b10101, Bits.extractUnsigned(0b11010100, 2, 5));
    }

    @Test
    void extractUnsignedThrowsOnNullLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            Bits.extractUnsigned(8, 2, 0);
        });
    }
}