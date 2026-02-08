package com.yourdomain.guardianac.utils;

import java.util.List;

/**
 * Math utilities used across multiple checks for statistical analysis.
 */
public final class MathUtil {

    private MathUtil() {}

    /**
     * Calculate the Greatest Common Divisor of two doubles using the Euclidean algorithm.
     * This is used for mouse sensitivity analysis (GCD check).
     */
    public static double gcd(double a, double b) {
        a = Math.abs(a);
        b = Math.abs(b);
        // Precision threshold
        if (a < 1e-4) return b;
        if (b < 1e-4) return a;
        if (a > b) {
            double temp = a;
            a = b;
            b = temp;
        }
        // Iterate for precision
        for (int i = 0; i < 100; i++) {
            double remainder = b % a;
            if (remainder < 1e-6) return a;
            b = a;
            a = remainder;
        }
        return a;
    }

    /**
     * Calculate the standard deviation of a list of doubles.
     */
    public static double standardDeviation(List<? extends Number> values) {
        if (values.size() < 2) return Double.MAX_VALUE;
        double mean = values.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(Number::doubleValue)
                .map(v -> (v - mean) * (v - mean))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * Calculate kurtosis — measures the "tailedness" of a distribution.
     * Legitimate click patterns have normal kurtosis; autoclickers have flat distributions.
     */
    public static double kurtosis(List<? extends Number> values) {
        if (values.size() < 4) return 0.0;
        double mean = values.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
        double n = values.size();
        double m2 = 0, m4 = 0;
        for (Number v : values) {
            double diff = v.doubleValue() - mean;
            m2 += diff * diff;
            m4 += diff * diff * diff * diff;
        }
        m2 /= n;
        m4 /= n;
        if (m2 == 0) return 0;
        return (m4 / (m2 * m2)) - 3.0; // excess kurtosis
    }

    /**
     * Calculate skewness of a distribution.
     */
    public static double skewness(List<? extends Number> values) {
        if (values.size() < 3) return 0.0;
        double mean = values.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
        double n = values.size();
        double m2 = 0, m3 = 0;
        for (Number v : values) {
            double diff = v.doubleValue() - mean;
            m2 += diff * diff;
            m3 += diff * diff * diff;
        }
        m2 /= n;
        m3 /= n;
        double sd = Math.sqrt(m2);
        if (sd == 0) return 0;
        return m3 / (sd * sd * sd);
    }

    /**
     * Calculate the outlier count in a dataset using IQR method.
     */
    public static int countOutliers(List<? extends Number> values) {
        if (values.size() < 4) return 0;
        List<Double> sorted = values.stream()
                .mapToDouble(Number::doubleValue)
                .sorted()
                .boxed()
                .toList();
        double q1 = sorted.get(sorted.size() / 4);
        double q3 = sorted.get(3 * sorted.size() / 4);
        double iqr = q3 - q1;
        double lower = q1 - 1.5 * iqr;
        double upper = q3 + 1.5 * iqr;
        int count = 0;
        for (double v : sorted) {
            if (v < lower || v > upper) count++;
        }
        return count;
    }

    /**
     * Wrap an angle to [-180, 180] range.
     */
    public static float wrapAngle(float angle) {
        angle = angle % 360.0f;
        if (angle > 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    /**
     * Get the angle between two 3D vectors in degrees.
     */
    public static double angleBetween(org.bukkit.util.Vector a, org.bukkit.util.Vector b) {
        double dot = a.normalize().dot(b.normalize());
        dot = Math.max(-1.0, Math.min(1.0, dot));
        return Math.toDegrees(Math.acos(dot));
    }

    /**
     * Clamp a value between min and max.
     */
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Calculate the standard deviation of a double array.
     */
    public static double standardDeviation(double[] values) {
        if (values.length < 2) return Double.MAX_VALUE;
        double mean = 0;
        for (double v : values) mean += v;
        mean /= values.length;
        double variance = 0;
        for (double v : values) variance += (v - mean) * (v - mean);
        variance /= values.length;
        return Math.sqrt(variance);
    }

    /**
     * Calculate the mean (average) of a list of numbers.
     */
    public static double mean(List<? extends Number> values) {
        if (values.isEmpty()) return 0.0;
        double sum = 0;
        for (Number v : values) sum += v.doubleValue();
        return sum / values.size();
    }

    /**
     * Calculate the variance of a list of numbers.
     */
    public static double variance(List<? extends Number> values) {
        if (values.size() < 2) return 0.0;
        double avg = mean(values);
        double sum = 0;
        for (Number v : values) {
            double diff = v.doubleValue() - avg;
            sum += diff * diff;
        }
        return sum / values.size();
    }

    /**
     * Calculate the GCD of a list of longs using the Euclidean algorithm.
     * Used for click pattern analysis.
     */
    public static double gcd(List<Long> values) {
        if (values == null || values.isEmpty()) return 0;
        double result = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            result = gcd(result, values.get(i));
        }
        return result;
    }
}
