package ch.epfl.javelo;

/**
 * Allows to extract a sequence of bits from a 32-bit vector
 * @author Edouard Mignan (345875)
 */

public final class Bits {

    private Bits() {
    }

    /**
     * Extracts a sequence of bits from a 32-bit vector
     *
     * @param value 32-bit vector
     * @param start starting index
     * @param length range
     * @throws IllegalArgumentException if start and length are
     * in the range from 0 to 31 (inclusive)
     * @return value interpreted as a two complement signed value
     */
    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(start + length - 1 <= 31 && start >= 0 && length >= 1);
        value = value << (32 - (start + length));
        value = value >> (32 - length);
        return value;
    }
    /**
     * Extracts a sequence of bits from a 32-bit vector
     *
     * @param value 32-bit vector
     * @param start starting index
     * @param length range
     * @throws IllegalArgumentException if start and length are
     * in the range from 0 to 31 (inclusive) and is not signed
     * @return value interpreted as a not signed value
     */
    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(start + length - 1 <= 31 && start >= 0 && length >= 1 && length != 32);
        value = value << (32 - (start + length));
        value = value >>> (32 - length);
        return value;
    }
}
