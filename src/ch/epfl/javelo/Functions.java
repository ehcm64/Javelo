package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

public final class Functions {

    private Functions() {
    }

    public static DoubleUnaryOperator constant(double y) {
        return new Constant(y);
    }

    public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
        Preconditions.checkArgument(samples.length >= 2 && xMax > 0);
        return new Sampled(samples, xMax);
    }

    private static final class Constant implements DoubleUnaryOperator {
        double constant;

        private Constant(double c) {
            this.constant = c;
        }

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

        @Override
        public double applyAsDouble(double x) {
            if (x <= 0) return this.samples[0];
            else {
                double step = xMax / (this.samples.length - 1);
                for (int i = 1; i < this.samples.length; i++) {
                    if (x < i * step)
                        return Math2.interpolate(this.samples[i - 1], this.samples[i], (x - (i - 1) * step) / step);
                }
            }
            return this.samples[this.samples.length - 1];
        }
    }
}
