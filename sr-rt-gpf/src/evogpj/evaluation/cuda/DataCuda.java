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
package evogpj.evaluation.cuda;

import evogpj.evaluation.DataSizeRetreiver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Generates C code that reads the training data and stores it in shared mem
 * @author Ignacio Arnaldo
 */
public class DataCuda {
    String datasetPath;
    int numberOfFitnessCases, numberOfFeatures, numberOfResults;
    double[] targetValues;  // Add this field to store target values
    GenerateReadDataCuda grdcu;
    String binReadDataCU = "tempFiles/readDataC";

    public DataCuda(String aDataset, int aNumberOfResults) {
        int aNumberOfFitnessCases = DataSizeRetreiver.num_fitness_cases(aDataset);
        int aNumberOfFeatures = DataSizeRetreiver.num_terminals(aDataset);
        datasetPath = aDataset;
        numberOfFitnessCases = aNumberOfFitnessCases;
        numberOfFeatures = aNumberOfFeatures;
        numberOfResults = aNumberOfResults;
        grdcu = new GenerateReadDataCuda();

        // Load the target values
        targetValues = loadTargetValues(aDataset);
    }

    public void readAndStoreDataset() {
        grdcu.generateCode(datasetPath, numberOfFitnessCases, numberOfFeatures, numberOfResults);
        String readDataCFile = "tempFiles/readDatasetCU.c";
        grdcu.printCodeToFile(readDataCFile);
        grdcu.compileFile(readDataCFile, binReadDataCU);
        int initDataset = 1; 
        grdcu.setInitDataset(initDataset);
        grdcu.runCode(binReadDataCU);
    }

    public void deallocateDataset() {
        // FREE SHARED MEMORY
        // ALREADY COMPILED CODE, BUT DIFFERENT ARGUMENT
        int initDataset = 0; // argument 0 means that the shared memory will be liberated
        grdcu.setInitDataset(initDataset);
        grdcu.runCode(binReadDataCU);
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
     * Load the target values from the dataset.
     * 
     * @param datasetPath The path to the dataset file.
     * @return The array of target values.
     */
    private double[] loadTargetValues(String datasetPath) {
        double[] targets = new double[numberOfFitnessCases];
        try (BufferedReader br = new BufferedReader(new FileReader(datasetPath))) {
            String line;
            int index = 0;
            while ((line = br.readLine()) != null && index < numberOfFitnessCases) {
                String[] values = line.split(",");
                targets[index] = Double.parseDouble(values[values.length - 1]); // Assuming the last column is the target
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return targets;
    }
}
