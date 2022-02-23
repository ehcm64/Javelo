package ch.epfl.javelo;

public final class Preconditions {

    private Preconditions() {}

    static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
