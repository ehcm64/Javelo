package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

/**
 * Methods for creating objects representing mathematical functions from reals to reals
 *
 * @author Edouard Mignan (345875)
 */

public final class Functions {

    private Functions() {
    }

    /**
     * Returns an constant function
     *
     * @param y constant
     * @return object with y as attribute
     */
    public static DoubleUnaryOperator constant(double y) {
        return new Constant(y);
    }

    /**
     * Returns a function mapping a set of values between 0 and a maximum value
     *
     * @param samples set of values
     * @param xMax    maximum step value
     * @return a function
     */
    public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
        Preconditions.checkArgument(samples.length >= 2 && xMax > 0);
        return new Sampled(samples, xMax);
    }

    private static final class Constant implements DoubleUnaryOperator {
        double constant;

        private Constant(double c) {
            this.constant = c;
        }

        /**
         * Returns the constant value for each value
         *
         * @param x the input value
         * @return the constant
         */
        @Override
        public double applyAsDouble(double x) {
            return this.constant;
        }
    }

    private static final class Sampled implements DoubleUnaryOperator {
        float[] samples;
        double xMax;

        private Sampled(float[] samples, double xMax) {
            this.samples = new float[samples.length];
            System.arraycopy(samples, 0, this.samples, 0, samples.length);
            this.xMax = xMax;
        }

        /**
         * Returns the f(x) by interpolation
         *
         * @param x the input value
         * @return the image of x
         */
        @Override
        public double applyAsDouble(double x) {
            if (x <= 0) {
                return this.samples[0];
            } else {
                double step = xMax / (this.samples.length - 1);
                for (int i = 1; i < this.samples.length; i++) {
                    if (x < i * step) {
                        double newX = (x / step) - i + 1;
                        return Math2.interpolate(this.samples[i - 1],
                                this.samples[i],
                                newX);
                    }
                }
            }
            return this.samples[this.samples.length - 1];
        }
    }
}
