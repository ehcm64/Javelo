package ch.epfl.javelo;

public final class Bits {

    private Bits() {
    }

    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(start + length <= 31 && start >= 0);

    }

    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(start + length < 32 && start >= 0 && length != 32);
    }
}
