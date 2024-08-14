package evogpj.evaluation.java;

import evogpj.genotype.Tree;
import evogpj.gp.Individual;
import evogpj.gp.Population;
import evogpj.utils.SpearmansRank;
import evogpj.evaluation.FitnessFunction;
import evogpj.math.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class SpearmanCorrelationFitness extends FitnessFunction {

    private final DataJava data;

    public SpearmanCorrelationFitness(DataJava data) {
        this.data = data;
    }

    // Remove @Override if these methods don't exist in FitnessFunction
    public void eval(Individual ind) {
        Tree genotype = (Tree) ind.getGenotype();
        double[] predictedValues = getPredictedValues(genotype);
        double[] targetValues = getTarget();
        
     // Debugging output
        //System.out.println("Predicted Values: " + Arrays.toString(predictedValues));
        //System.out.println("Target Values: " + Arrays.toString(targetValues));

        double spearmanCorrelation = SpearmansRank.calculateSpearmanRankCorrelation(predictedValues, targetValues);
        
     // Debugging output for the calculated correlation
        //System.out.println("Spearman's Rank Correlation: " + spearmanCorrelation);

        
        ind.setFitness("SpearmanCorrelation", 1.0 - spearmanCorrelation); // Assuming fitness is a minimization objective
    }

    public void evalPop(Population pop) {
        for (Individual ind : pop) {
            eval(ind);
        }
    }

    private double[] getPredictedValues(Tree genotype) {
        double[] predictedValues = new double[data.getNumberOfFitnessCases()];
        List<Double> d;
        double[][] inputValuesAux = data.getInputValues();

        Function func = genotype.generate();
        for (int i = 0; i < data.getNumberOfFitnessCases(); i++) {
            d = new ArrayList<>();
            for (int j = 0; j < data.getNumberOfFeatures(); j++) {
                d.add(inputValuesAux[i][j]);
            }
            predictedValues[i] = func.eval(d);
        }
        return predictedValues;
    }

    private double[] getTarget() {
        return data.getTargetValues();
    }

    @Override
    public Boolean isMaximizingFunction() {
        return false; // Spearman correlation is typically minimized when used as a fitness function
    }
}
