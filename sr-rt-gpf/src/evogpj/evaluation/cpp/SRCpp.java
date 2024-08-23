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
package evogpj.evaluation.cpp;

import evogpj.evaluation.Expression;
import evogpj.evaluation.ParseExpression;
import evogpj.genotype.Tree;
import evogpj.gp.Individual;
import evogpj.gp.Population;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import evogpj.algorithm.Parameters;
import evogpj.evaluation.FitnessFunction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Implements fitness evaluation for symbolic regression in C++
 * 
 * @author Owen Derby
 */
public class SRCpp extends FitnessFunction {

    private final DataCpp data;  // Ensure DataCpp is imported and used
    private final boolean USE_INT;

    // defaults to SR_DATA_FITNESS
    public static String FITNESS_KEY = Parameters.Operators.SR_CPP_FITNESS;
    public Boolean isMaximizingFunction = true;
    public Boolean discreteFitness = false;

    ArrayList<String> FUNC_SET, UNARY_FUNC_SET;
    String datasetPath;
    int numberOfFitnessCases, numberOfFeatures, numberOfResults,
        currentMaxSize, pow, numberOfThreads;
    
    public SRCpp(List<String> aFUNC_SET, List<String> aUNARY_FUNC_SET, String aDataset,
                 int aNumberOfFitnessCases, int aNumberOfFeatures, int aNumberOfResults,
                 int aNumberOfThreads, int aPow, boolean useInts) {
        USE_INT = useInts;
        pow = aPow;
        FUNC_SET = (ArrayList<String>) aFUNC_SET;
        UNARY_FUNC_SET = (ArrayList<String>) aUNARY_FUNC_SET;
        datasetPath = aDataset;
        numberOfFitnessCases = aNumberOfFitnessCases;
        numberOfFeatures = aNumberOfFeatures;
        numberOfResults = aNumberOfResults;
        currentMaxSize = 0;
        numberOfThreads = aNumberOfThreads;
        data = new DataCpp(datasetPath, 1);  // Initialize data with the correct constructor
    }

    @Override
    public Boolean isMaximizingFunction() {
        return this.isMaximizingFunction;
    }

    @Override
    public void evalPop(Population pop) {
        try {
            evaluatePopulationCPU(pop, USE_INT, pow, numberOfThreads);
        } catch (IOException ex) {
            Logger.getLogger(SRCpp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void evaluatePopulationCPU(Population pop, boolean useInts, int p, int numberOfThreads)
            throws IOException {

        int numberOfIndividuals = pop.size();
        Expression[] expressions = new Expression[numberOfIndividuals];
        double[] predictions = new double[numberOfFitnessCases];
        double[] actualValues = data.getTargetValues();  // Accessing the actual target values

        for (int i = 0; i < numberOfIndividuals; i++) {
            expressions[i] = new Expression();
            expressions[i].setOps((ArrayList<String>) FUNC_SET);
            expressions[i].setUnOps((ArrayList<String>) UNARY_FUNC_SET);
            String prefixExpression = ((Tree) pop.get(i).getGenotype()).toPrefixString();
            expressions[i].setPrefixExpression(prefixExpression);
        }

        ParseExpression pe = new ParseExpression();
        for (int i = 0; i < numberOfIndividuals; i++) {
            pe.setExpression(expressions[i]);
            String prefixExpression = expressions[i].getPrefixExpression();
            String infix = pe.getInfixFromPrefix(prefixExpression);
            expressions[i].setInfixExpression(infix);
            pe.getPosfixFromInfix();
            pe.getcodedRPN();
        }

        // Implement the necessary code to evaluate the population using C++
        // and fill in the predictions array accordingly.

        // For demonstration purposes, I assume the fitnessArrayList stores predictions.
        for (int i = 0; i < numberOfFitnessCases; i++) {
            predictions[i] = 0; // Replace with actual prediction logic
            actualValues[i] = data.getTargetValues()[i];
        }

        // Implement Spearman's rank correlation or any other operations here
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

    private void compileInterpreter(int numberOfIndividuals, int newMaxIndividualSize) {
        // Implement the method to compile C++ code if required
    }
}
