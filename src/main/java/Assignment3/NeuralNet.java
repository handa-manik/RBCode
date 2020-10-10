package Assignment3;

import java.io.*;
import java.util.*;

public class NeuralNet{

    final Random random = new Random();
    final ArrayList<SNeuron> inputLayer = new ArrayList<SNeuron>();
    final ArrayList<SNeuron> hiddenLayer = new ArrayList<SNeuron>();
    final ArrayList<SNeuron> outputLayer = new ArrayList<SNeuron>();
    // LUT Hashmap to store state/action pairs
    private static HashMap<Integer, Double> nnReinforcementLearningLUTHashMap = new HashMap<>();
    final SNeuron biased_Neuron = new SNeuron();
    final int[] number_of_Layers;

    final double rand = 1;
    final double MAX_WEIGHT = 0.5f;
    final double MIN_WEIGHT = -0.5f;
    final double nn_LearningRate = 0.2f;
    final double nn_MomentumTerm = 0.9f;
    final boolean isBipolar = false;

    static double[] output;


    // Main function
    public static void main(String[] args) throws IOException {

    }

    public NeuralNet(int argNumInputs, int argNumHidden, int argNumOutput, boolean randomWeights, String[][] weight) {
        this.number_of_Layers = new int[] { argNumInputs, argNumHidden, argNumOutput };

        for (int i = 0; i < number_of_Layers.length; i++) {
            if (i == 0) {
                for (int j = 0; j < number_of_Layers[i]; j++) {
                    SNeuron neuron = new SNeuron();
                    inputLayer.add(neuron);
                }
            } else if (i == 1) {
                for (int j = 0; j < number_of_Layers[i]; j++) {
                    SNeuron neuron = new SNeuron();
                    neuron.inputConnectionsAdd(inputLayer);
                    neuron.add_BiasConnection(biased_Neuron);
                    hiddenLayer.add(neuron);
                }
            } else if (i == 2) {
                for (int j = 0; j < number_of_Layers[i]; j++) {
                    SNeuron neuron = new SNeuron();
                    neuron.inputConnectionsAdd(hiddenLayer);
                    neuron.add_BiasConnection(biased_Neuron);
                    outputLayer.add(neuron);
                }
            } else {
                System.out.println("Error occurred while creating the Neural Net");
            }
        }

        if (randomWeights) {
            for (SNeuron neuron : hiddenLayer) {
                ArrayList<NeuronConnection> connections = neuron.getInputConnections();
                for (NeuronConnection conn : connections) {
                    double updatedWeight = getRandomWeight();
                    conn.updatedWeight(updatedWeight);
                }
            }
            for (SNeuron neuron : outputLayer) {
                ArrayList<NeuronConnection> connections = neuron.getInputConnections();
                for (NeuronConnection conn : connections) {
                    double updated_Weight = getRandomWeight();
                    conn.updatedWeight(updated_Weight);
                }
            }
        }

        else {

            try {
                updatedWeights(weight);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        SNeuron.neuronCounter = 0;
        NeuronConnection.connectionCounter = 0;
    }

    public void setInputLayer(double train_X[]) {
        for (int i = 0; i < inputLayer.size(); i++) {
            inputLayer.get(i).setOutput(train_X[i]);
        }
    }

    public double[] getOutput() {
        double[] output = new double[outputLayer.size()];
        for (int i = 0; i < outputLayer.size(); i++)
            output[i] = outputLayer.get(i).getOutputValue();
        return output;
    }

    /**
     * Generates a random value within upper and lower bound provided by the user
     * @return a random number within lower and upper bound
     */
    double getRandomWeight() {

        return rand * (random.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT) - MIN_WEIGHT); //
    }

    public void forwardPropagation() {
        for (SNeuron n : hiddenLayer)
            n.getOutputValue(0);
        for (SNeuron n : outputLayer)
            n.getOutputValue(1);
    }

    public void errorBackpropagation(double outputExp[]) {

        int k = 0;
        for (SNeuron n : outputLayer) {
            ArrayList<NeuronConnection> connections = n.getInputConnections();
            for (NeuronConnection con : connections) {
                double ak = n.getOutputValue();
                double ai = con.LNeuron.getOutputValue();
                double tempIdealOut = outputExp[k];
                double partialDescent;
                partialDescent   = -ai * (tempIdealOut - ak);

                double delWeight = -nn_LearningRate * partialDescent;
                double updated_Weight = con.getWeightValue() + delWeight;
                con.calculateWeightDelta(delWeight);
                con.updatedWeight(updated_Weight + con.getPreviousDeltaWeight() * nn_MomentumTerm);
            }
            k++;
        }

        for (SNeuron n : hiddenLayer) {
            ArrayList<NeuronConnection> connections = n.getInputConnections();
            for (NeuronConnection con : connections) {
                double aj = n.getOutputValue();
                double ai = con.LNeuron.getOutputValue();
                double total_output = 0;
                int j = 0;
                for (SNeuron out_neu : outputLayer) {
                    double wjk = out_neu.getConnection(n.neuronId).getWeightValue(); //new updated weight is used
                    double tempIdealOut = (double) outputExp[j];
                    double ak = out_neu.getOutputValue();
                    j++;

                    total_output = total_output + (-(tempIdealOut - ak) * wjk);

                }
                double partialDescent;
                partialDescent   = aj * (1 - aj) * ai * total_output;

                double delta_Weight = -nn_LearningRate * partialDescent  ;
                double updated_Weight = con.getWeightValue() + delta_Weight;
                con.calculateWeightDelta(delta_Weight);
                con.updatedWeight(updated_Weight + nn_MomentumTerm * con.getPreviousDeltaWeight());
            }
        }
    }

    public static Map<String, Object> start(double [] Xtrain, double [] Ytrain, boolean randWeights, boolean neuralNetTrain, String [][] weight) throws IOException {

        NeuralNet nn = new NeuralNet(5, 3, 1,randWeights,weight);
        nn.setInputLayer(Xtrain);
        nn.forwardPropagation();
        output = nn.getOutput();

        if (neuralNetTrain) {
            nn.errorBackpropagation(Ytrain);
        }

        weight = nn.saveWeights(weight);

        Map<String, Object> multiValues = new HashMap<String, Object>();
        multiValues.put("value", output[0]);
        multiValues.put("array", weight);
        return multiValues;
    }

    public void updatedWeights(String [][] weight) throws IOException{

	  for (int k=0;k<weight.length;k++) {
		  System.out.println(weight[k][0]+" "+weight[k][1]+"    "+weight[k][2]);
	  }

        int i = 0;
        for (SNeuron n : hiddenLayer) {
            ArrayList<NeuronConnection> connections = n.getInputConnections();
            for (NeuronConnection con : connections) {
                System.out.println("Weight value::"+Double.valueOf(weight[i][2]).doubleValue());
                con.updatedWeight(Double.valueOf(weight[i][2]).doubleValue());
                i = i+1;
            }
        }
        // update weights for the output layer
        for (SNeuron n : outputLayer) {
            ArrayList<NeuronConnection> connections = n.getInputConnections();
            for (NeuronConnection con : connections) {
                con.updatedWeight(Double.valueOf(weight[i][2]).doubleValue());
                i = i+1;
            }
        }

    }

    public String[][] saveWeights(String [][] weight) {
        int j = 0;
        for (SNeuron n : hiddenLayer) {
            ArrayList<NeuronConnection> connections = n.getInputConnections();
            for (NeuronConnection con : connections) {
                weight[j][0] = Double.toString(n.neuronId);
                weight[j][1] = Double.toString(con.connectionId);
                weight[j][2] = Double.toString(con.getWeightValue());
                j = j+1;
            }
        }

        for (SNeuron nc : outputLayer) {
            ArrayList<NeuronConnection> connections = nc.getInputConnections();
            for (NeuronConnection con : connections) {
                weight[j][0] = Double.toString(nc.neuronId);
                weight[j][1] = Double.toString(con.connectionId);
                weight[j][2] = Double.toString(con.getWeightValue());
                j=j+1;
            }
        }
        return weight;

    }

    /**
     * Load the lookup table hashmap
     *
             * @param lutFile The filename to use for the lookup table hashmap
     */
    private static void loadLut(File lutFile)
    {
        /*// Quantized values
        int quantRobotX;
        int quantRobotY;
        int quantDistance;
        int quantRobotHeading;

        // Bipolar values
        double bipolarRobotX;
        double bipolarRobotY;
        double bipolarDistance;
        double bipolarRobotBearing;
        try
        {
            FileInputStream fileIn = new FileInputStream(lutFile);
            //ObjectInputStream in = new ObjectInputStream(fileIn);
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(fileIn));
            nnReinforcementLearningLUTHashMap = (HashMap<Integer, Double>) in.readObject();
            // Scale the quantizations to bipolar binary representations
            bipolarRobotX = (quantRobotX * 2.0 / 16.0) - 1.0;
            bipolarRobotY = (quantRobotY * 2.0 / 16.0) - 1.0;
            bipolarDistance = (quantDistance * 2.0 / 16.0) - 1.0;
            bipolarRobotBearing = Math.cos(Math.toRadians(robotBearing));
            in.close();
            fileIn.close();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        catch (ClassNotFoundException exception)
        {
            exception.printStackTrace();
        }*/
    }

}

