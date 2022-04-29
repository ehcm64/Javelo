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
     * @param value  32-bit vector
     * @param start  starting index
     * @param length range
     * @return value interpreted as a two complement signed value
     * @throws IllegalArgumentException if length of extraction is greater than 32-bits or null
     */
    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(start + length - 1 <= Integer.SIZE - 1
                && start >= 0
                && length >= 1);

        value = value << (Integer.SIZE - (start + length));
        value = value >> (Integer.SIZE - length);
        return value;
    }

    /**
     * Extracts a sequence of bits from a 32-bit vector
     *
     * @param value 32-bit vector
     * @param start starting index
     * @param length range
     * @throws IllegalArgumentException if length of extraction is greater or equal to 32-bits or null
     * @return value interpreted as a not signed value
     */
    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(start + length - 1 <= Integer.SIZE - 1
                && start >= 0
                && length >= 1
                && length != Integer.SIZE);

        value = value << (Integer.SIZE - (start + length));
        value = value >>> (Integer.SIZE - length);
        return value;
    }
}
