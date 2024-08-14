package main;

import evogpj.algorithm.Parameters;
import evogpj.algorithm.SymbRegMOO;
import evogpj.gp.Individual;
import evogpj.gp.Population;

import java.io.IOException;
import java.util.Properties;

public class TestSpearmanCorrelation {
    public static void main(String[] args) throws IOException {
        // Load properties or set them manually
        Properties props = new Properties();
        props.setProperty(Parameters.Names.PROBLEM, "sample0.csv"); // Set the correct path to your dataset
        props.setProperty(Parameters.Names.NUM_GENS, "50"); // Set the number of generations
        props.setProperty(Parameters.Names.POP_SIZE, "100"); // Set the population size
        props.setProperty(Parameters.Names.FITNESS, Parameters.Operators.SR_JAVA_FITNESS + ", " + Parameters.Operators.SUBTREE_COMPLEXITY_FITNESS + ", SpearmanCorrelation");

        // Initialize the algorithm
        SymbRegMOO symbReg = new SymbRegMOO(props, 3600); // Run for 1 hour max

        // Run the algorithm
        Individual best = symbReg.run_population();

        // Output the results
        System.out.println("Best Individual: " + best);
        Population bestPop = symbReg.getBestPop();
        for (Individual ind : bestPop) {
            System.out.println(ind);
            //System.out.println("Spearman Correlation: " + ind.getFitness("SpearmanCorrelation"));
        }
    }
}
