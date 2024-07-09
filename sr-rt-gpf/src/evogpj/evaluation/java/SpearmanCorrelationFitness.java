package evogpj.evaluation.java;

import evogpj.gp.Individual;
import evogpj.gp.Population;
import evogpj.evaluation.FitnessFunction;
import evogpj.utils.SpearmansRank;

public class SpearmanCorrelationFitness extends FitnessFunction {
    public static String FITNESS_KEY = "SpearmanCorrelation";

    public Boolean isMaximizingFunction = false;

    private final double[] targetValues;

    public SpearmanCorrelationFitness(double[] targetValues) {
        this.targetValues = targetValues;
    }

    @Override
    public Boolean isMaximizingFunction() {
        return this.isMaximizingFunction;
    }

    @Override
    public void eval(Individual ind) {
        // Assuming the individual's genotype has a method to get predicted values
        Tree genotype = (Tree) ind.getGenotype();
        double[] predictedValues = getPredictedValues(genotype);

        double spearmanCorrelation = SpearmansRank.calculateSpearmanRankCorrelation(predictedValues, targetValues);
        ind.setFitness(FITNESS_KEY, 1.0 - spearmanCorrelation); // Assuming fitness is a minimization objective
    }

    @Override
    public void evalPop(Population pop) {
        for (Individual ind : pop) {
            eval(ind);
        }
    }

    private double[] getPredictedValues(Tree genotype) {
        // Implement this method to generate predicted values based on the genotype
        // This should be similar to how you obtain predicted values in SRJava
        return new double[targetValues.length]; // Placeholder implementation
    }
}
