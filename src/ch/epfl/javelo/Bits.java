package ch.epfl.javelo;

public final class Bits {

    private Bits() {
    }

    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(start + length - 1 <= 31 && start >= 0 && length >= 1);
        value = value << (32 - (start + length));
        value = value >> (32 - length);
        return value;
    }

    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(start + length - 1 <= 31 && start >= 0 && length >= 1 && length != 32);
        value = value << (32 - (start + length));
        value = value >>> (32 - length);
        return value;
    }
}
