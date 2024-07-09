package evogpj.utils;

import java.util.Arrays;
import java.util.Comparator;

public class SpearmansRank {
    public static double[] calculateRanks(double[] values) {
        int n = values.length;
        double[] ranks = new double[n];
        Integer[] indices = new Integer[n];

        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }

        Arrays.sort(indices, Comparator.comparingDouble(i -> values[i]));

        for (int i = 0; i < n; i++) {
            ranks[indices[i]] = i + 1;
        }

        return ranks;
    }

    public static double calculateSpearmanRankCorrelation(double[] predicted, double[] target) {
        double[] predictedRanks = calculateRanks(predicted);
        double[] targetRanks = calculateRanks(target);

        int n = predicted.length;
        double sumDiffSquared = 0.0;

        for (int i = 0; i < n; i++) {
            double diff = predictedRanks[i] - targetRanks[i];
            sumDiffSquared += diff * diff;
        }

        double spearmanCorrelation = 1 - (6 * sumDiffSquared) / (n * (n * n - 1));
        return spearmanCorrelation;
    }
}
