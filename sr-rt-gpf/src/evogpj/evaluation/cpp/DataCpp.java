/**
 * Copyright (c) 2011-2013 Evolutionary Design and Optimization Group
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
 */
package evogpj.evaluation.cpp;

import evogpj.evaluation.DataSizeRetreiver;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Generates a C program that reads the training data and stores in shared mem
 * @author Ignacio Arnaldo
 */
public class DataCpp {
    String datasetPath;
    int numberOfFitnessCases, numberOfFeatures, numberOfResults;
    double[] targetValues; // Array to hold the target values
    GenerateReadDataCpp grdc;
    String binReadDataC = "tempFiles/readDataCpp";

    public DataCpp(String aDataset, int aNumberOfResults) {
        int aNumberOfFitnessCases = DataSizeRetreiver.num_fitness_cases(aDataset);
        int aNumberOfFeatures = DataSizeRetreiver.num_terminals(aDataset);
        datasetPath = aDataset;
        numberOfFitnessCases = aNumberOfFitnessCases;
        numberOfFeatures = aNumberOfFeatures;
        numberOfResults = aNumberOfResults;
        grdc = new GenerateReadDataCpp();
        loadTargetValues(); // Load target values from the dataset
    }

    public void readAndStoreDataset() {
        grdc.generateCode(0, datasetPath, numberOfFitnessCases,
                numberOfFeatures, numberOfResults);
        String readDataCFile = "tempFiles/readDataCpp.c";
        grdc.printCodeToFile(readDataCFile);
        grdc.compileFile(readDataCFile, binReadDataC);
        int initDataset = 1; 
        grdc.setInitDataset(initDataset);
        grdc.runCode(binReadDataC);
    }

    public void deallocateDataset() {
        // FREE SHARED MEMORY
        // ALREADY COMPILED CODE, BUT DIFFERENT ARGUMENT
        int initDataset = 0; // argument 0 means that the shared memory will be liberated
        grdc.setInitDataset(initDataset);
        grdc.runCode(binReadDataC);
    }

    public int getNumberOfFeatures() {
        return numberOfFeatures;
    }

    public int getNumberOfFitnessCases() {
        return numberOfFitnessCases;
    }

    public double[] getTargetValues() {
        return targetValues;
    }

    /**
     * Load the target values from the dataset file.
     */
    private void loadTargetValues() {
        targetValues = new double[numberOfFitnessCases];
        try (BufferedReader br = new BufferedReader(new FileReader(datasetPath))) {
            String line;
            int index = 0;
            while ((line = br.readLine()) != null && index < numberOfFitnessCases) {
                String[] tokens = line.split(",");
                targetValues[index] = Double.parseDouble(tokens[tokens.length - 1]);
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
