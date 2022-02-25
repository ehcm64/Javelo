package ch.epfl.javelo;

/**
 * Provides a method for checking preconditions of methods of other classes.
 *
 * @author Edouard Mignan (345875)
 */
public final class Preconditions {

    private Preconditions() {
    }

    /**
     * Checks if boolean argument is true and throws exception otherwise.
     *
     * @param shouldBeTrue argument that should be true
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
