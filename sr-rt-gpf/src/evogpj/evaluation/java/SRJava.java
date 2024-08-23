/**
 * Copyright (c) 2011-2013 ALFA Group
 * 
 * Licensed under the MIT License.
 * 
 * See the "LICENSE" file for a copy of the license.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.  
 *
 * @author Ignacio Arnaldo
 * 
 */
package evogpj.evaluation.java;

import java.util.ArrayList;
import java.util.List;

import evogpj.genotype.Tree;
import evogpj.gp.Individual;
import evogpj.gp.Population;
import evogpj.math.Function;
import evogpj.algorithm.Parameters;
import evogpj.evaluation.FitnessFunction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Evaluation of symbolic regression models in Java
 * @author Ignacio Arnaldo
 */
public class SRJava extends FitnessFunction {

	private final DataJava data;
    private final int pow;
    private final boolean USE_INT;
    private final int numThreads;
    public static String FITNESS_KEY = Parameters.Operators.SR_JAVA_FITNESS;
    public Boolean isMaximizingFunction = true;

    public SRJava(DataJava data) {
        this(data, false, 1);
    }

    public SRJava(DataJava aData, boolean is_int, int anumThreads) {
        this.data = aData;
        USE_INT = is_int;
        numThreads = anumThreads;
        this.pow = 2; // or any default value
    }
    
    public SRJava(DataJava data, int pow, boolean isInt, int numThreads) {
        this.data = data;
        this.pow = pow;
        this.USE_INT = isInt;
        this.numThreads = numThreads;
        
    }

    @Override
    public Boolean isMaximizingFunction() {
        return this.isMaximizingFunction;
    }

    public void eval(Individual ind) {
        Tree genotype = (Tree) ind.getGenotype();
        Function func = genotype.generate();

        double[] predictions = new double[data.getNumberOfFitnessCases()];
        double[] actualValues = data.getTargetValues();

        for (int i = 0; i < data.getNumberOfFitnessCases(); i++) {
            List<Double> inputRow = new ArrayList<>();
            for (int j = 0; j < data.getNumberOfFeatures(); j++) {
                inputRow.add(data.getInputValues()[i][j]);
            }
            predictions[i] = func.eval(inputRow);
        }

        // Calculate Spearman's rank correlation
        double spearmanCorrelation = calculateSpearmansRankCorrelation(predictions, actualValues);
        ind.setFitness(SRJava.FITNESS_KEY, spearmanCorrelation);

        func = null;
    }

    private double calculateSpearmansRankCorrelation(double[] predicted, double[] actual) {
        int n = predicted.length;

        Integer[] predictedRanks = IntStream.range(0, n).boxed().toArray(Integer[]::new);
        Arrays.sort(predictedRanks, Comparator.comparingDouble(i -> predicted[i]));
        double[] rankedPredicted = new double[n];
        for (int i = 0; i < n; i++) {
            rankedPredicted[predictedRanks[i]] = i + 1;
        }

        Integer[] actualRanks = IntStream.range(0, n).boxed().toArray(Integer[]::new);
        Arrays.sort(actualRanks, Comparator.comparingDouble(i -> actual[i]));
        double[] rankedActual = new double[n];
        for (int i = 0; i < n; i++) {
            rankedActual[actualRanks[i]] = i + 1;
        }

        double sumDiffsSquared = 0;
        for (int i = 0; i < n; i++) {
            double diff = rankedPredicted[i] - rankedActual[i];
            sumDiffsSquared += diff * diff;
        }

        return 1 - (6 * sumDiffsSquared) / (n * (n * n - 1));
    }

    @Override
    public void evalPop(Population pop) {
        ArrayList<SRJavaThread> alThreads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            SRJavaThread threadAux = new SRJavaThread(i, pop, numThreads);
            alThreads.add(threadAux);
        }

        for (SRJavaThread threadAux : alThreads) {
            threadAux.start();
        }

        for (SRJavaThread threadAux : alThreads) {
            try {
                threadAux.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(SRJava.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public class SRJavaThread extends Thread {
        private int indexThread, totalThreads;
        private Population pop;

        public SRJavaThread(int anIndex, Population aPop, int aTotalThreads) {
            indexThread = anIndex;
            pop = aPop;
            totalThreads = aTotalThreads;
        }

        @Override
        public void run() {
            int indexIndi = 0;
            for (Individual individual : pop) {
                if (indexIndi % totalThreads == indexThread) {
                    eval(individual);
                }
                indexIndi++;
            }
        }
    }
}
