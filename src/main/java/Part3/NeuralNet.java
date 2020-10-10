package Part3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * NeuralNet class implements a neural net with 2 inputs and
 * four hidden neurons. (this is scalable) and 1 output configuration.
 * A multi-layer perceptron is created and trained using
 * the error-backpropagation algorithm.
 * @date 19 October 2018
 * @author @Neha, @Manik
 */
public class NeuralNet implements NeuralNetInterface {
    // Integer bounds for sigmoid activation function used by the output neuron only
    private double lower_ArgA;
    private double upper_ArgB;

    private static final int MAX_HIDDEN_NEURONS = 1000;
    private static final int MAX_INPUTS =         16;
    private static final int MAX_OUTPUTS =        16;

    //Range of initial weights for the neural net
    private double nn_Min_Weight;
    private double nn_Max_Weight;

    //Both the variables change as given for part 1a, 1b and 1c
    public double nn_LearningRate;
    public double nn_MomentumTerm;

    // Neural network configuration setup with one hidden layer and two inputs
    public int nn_Num_Inputs;
    public int nn_Num_Hidden_Neurons;
    private int mNumOutputs;

    // Stores bias weights for Output neurons
    public static double nn_HiddenNeuron_to_Output_BiasWeight;
    public static double nn_Old_HiddenNeuron_Output_BiasWeight;

    // Stores the input value from the training set
    public double[] nn_Input_Values = new double[Constants.MAX_INPUTS];

    // Array to store the output neuron's input weights
    private static double[][] mOutputNeuronWeights = new double[MAX_OUTPUTS][MAX_HIDDEN_NEURONS];
    // Array to store the previous output neuron's weights
    private static double[][] mPreviousOutputNeuronWeights = new double[MAX_OUTPUTS][MAX_HIDDEN_NEURONS];
    // Variables for output neuron bias weight
    private static double[] mOutputNeuronBiasWeights = new double[MAX_OUTPUTS];
    private static double[] mPreviousOutputNeuronBiasWeights = new double[MAX_OUTPUTS];

    // Variable for unactivated output neuron value
    private double[] mOutputNeuronUnactivatedValues = new double[MAX_OUTPUTS];
    // Variable for value of output neuron
    private double[] mOutputNeuronValues = new double[MAX_OUTPUTS];
    // Variable for out neuron error
    private double[] mOutputNeuronErrors = new double[MAX_OUTPUTS];

    // Stores the weights from input to each neuron in the hidden layer
    public static double[][] nn_Input_Weights = new double[Constants.MAX_HIDDEN_NEURONS][Constants.MAX_INPUTS];
    public static double[][] nn_Old_Input_Weights = new double[Constants.MAX_HIDDEN_NEURONS][Constants.MAX_INPUTS];

    // Stores the input signal, output signal and neuron errors in the hidden layer
    public double[] nn_Input_to_HiddenNeuron_Signal = new double[Constants.MAX_HIDDEN_NEURONS];
    public double[] nn_HiddenNeuron_to_Output_Signal = new double[Constants.MAX_HIDDEN_NEURONS];
    public double[] nn_HiddenNeuron_Errors = new double[Constants.MAX_HIDDEN_NEURONS];

    // Stores the weights old/updated for hidden layer neuron to output
    public static double[] nn_HiddenNeuron_to_Output_Weights = new double[Constants.MAX_HIDDEN_NEURONS];
    public static double[] nn_Old_HiddenNeuron_Output_Weights = new double[Constants.MAX_HIDDEN_NEURONS];

    // Stores the input, output, and error for the output neuron
    public double nn_OutputNeuron_InputSignal;
    public double nn_OutputNeuron_OutputSignal;
    public double nn_OutputNeuron_Error;

    private static final int TRAINING_SET_STATE_INDEX = 0;
    private static final int TRAINING_SET_ACTION_INDEX = 1;

    private static final int WEIGHTS_INPUT_INDEX = 0;
    private static final int WEIGHTS_OUTPUT_INDEX = 1;

    /**
     * Constructor for NeuralNet
     *
     * @param argNumInputs    The number of inputs in your input vector
     * @param argNumHidden    The number of hidden neurons in your hidden layer. Only a single hidden layer is supported
     * @param argLearningRate The learning rate coefficient
     * @param argMomentumTerm The momentum coefficient
     * @param argA            Integer lower bound of sigmoid used by the output neuron only.
     * @param argB            Integer upper bound of sigmoid used by the output neuron only.
     */
    public NeuralNet(int argNumInputs,
                     int argNumOutputs,
                     int argNumHidden,
                     double argLearningRate,
                     double argMomentumTerm,
                     double argA,
                     double argB,
                     double argWeightInitMin,
                     double argWeightInitMax) {
        //System.out.println("Inside NeuralNet Constructor!!!");
        lower_ArgA = argA;
        upper_ArgB = argB;
        nn_LearningRate = argLearningRate;
        nn_MomentumTerm = argMomentumTerm;
        nn_Min_Weight = argWeightInitMin;
        nn_Max_Weight = argWeightInitMax;
        nn_Num_Inputs = argNumInputs + 1;
        mNumOutputs = argNumOutputs;
        nn_Num_Hidden_Neurons = argNumHidden;

        zeroWeights();
    }

    /**
     * Initialize the weights to 0.
     */
    public void zeroWeights()
    {
        //System.out.println("Inside zeroWeights()!!!");
        int i, j;
        for(i = 0; i < nn_Num_Hidden_Neurons; i++) {

            for(j = 0; j < nn_Num_Inputs; j++) {
                nn_Input_Weights[i][j] = 0.0;
                nn_Old_Input_Weights[i][j] = 0.0;
            }
        }

        // initialize output neuron weights
        for(i = 0; i < mNumOutputs; i++)
        {
            for(j = 0; j < nn_Num_Hidden_Neurons; j++)
            {
                mPreviousOutputNeuronWeights[i][j] = 0.0;
                mOutputNeuronWeights[i][j] = 0.0;
            }
        }
    }

    /**
     * This method implements the sigmoid function
     * @param x The input
     * @return f(x) = 1 / (1 + exp(-x))
     */
    public double sigmoid(double x)
    {
        double result;
        result =  1 / (1 + Math.exp(-x));
        return result;
    }

    /**
     * This method implements a general sigmoid with asymptotes bounded by (a,b)
     * @param x The input
     * @return f(x) = (b - a) / (1 + exp(-x)) + a
     */
    public double customSigmoid(double x)
    {
        double result;
        result = (upper_ArgB - lower_ArgA) * sigmoid(x) + lower_ArgA;
        return result;
    }

    /**
     * This method implements the first derivative of the general sigmoid above
     * @param x The input
     * @return f'(x) = (1 / (b - a))(customSigmoid(x) - a)(b - customSigmoid(x))
     */
    public double customSigmoidDerivative(double x)
    {
        double coefficient = (1.0/(upper_ArgB - lower_ArgA));
        double result = coefficient * (customSigmoid(x) - lower_ArgA) * (upper_ArgB - customSigmoid(x));
        return result;
    }

    /**
     * Initialize the weights to random values.
     * For say 2 inputs, the input vector is [0] & [1]. We add [2] for the bias.
     * Like wise for hidden units. For say 2 hidden units which are stored in an array.
     * [0] & [1] are the hidden & [2] the bias.
     * We also initialise the last weight change arrays. This is to implement the alpha term.
     */
    public void initializeWeights()
    {
       // System.out.println("Inside initializeWeights()!!!");
        int i, j;
         for(i = 0; i < nn_Num_Hidden_Neurons; i++)
         {
            for(j = 0; j < nn_Num_Inputs; j++) {
                nn_Input_Weights[i][j] = generateRandom(nn_Min_Weight, nn_Max_Weight);
            }
        }

        // initialize outer neuron weights
        for(i = 0; i < mNumOutputs; i++)
        {
            for(j = 0; j < nn_Num_Hidden_Neurons; j++)
            {
                // initialize the output neuron weights
                mOutputNeuronWeights[i][j] = generateRandom(nn_Min_Weight, nn_Max_Weight);
                mOutputNeuronBiasWeights[i] = generateRandom(nn_Min_Weight, nn_Max_Weight);
            }
        }
        nn_Old_Input_Weights = nn_Input_Weights.clone();
        nn_Old_HiddenNeuron_Output_Weights = nn_HiddenNeuron_to_Output_Weights.clone();
        nn_Old_HiddenNeuron_Output_BiasWeight = nn_HiddenNeuron_to_Output_BiasWeight;
    }

    /**
     * This method will tell the NN the output
     * value that should be mapped to the given input vector. I.e.
     * the desired correct output value for an input.
     * @param trainingSet The training set
     * @return The error in the output for that input vector
     */
    public double[] train(ArrayList<ArrayList<Double>> trainingSet)
    {
       //System.out.println("Inside train()!!!");
        int i;
        double[] errors = new double[mNumOutputs];
        double[] inputArray = new double[trainingSet.get(TRAINING_SET_STATE_INDEX).size()];
        double[] outputArray = new double[trainingSet.get(TRAINING_SET_ACTION_INDEX).size()];

        // Convert ArrayLists into static arrays
        for(i = 0; i < inputArray.length; i++)
        {
            inputArray[i] = trainingSet.get(TRAINING_SET_STATE_INDEX).get(i);
        }
        for(i = 0; i < outputArray.length; i++)
        {
            outputArray[i] = trainingSet.get(TRAINING_SET_ACTION_INDEX).get(i);
        }

        // Feed forward phase begins here
        outputFor(inputArray);

        //Backpropagation phase begins here
        calErrorBackPropagation(outputArray);

        computeNewWeights(); // weights update for error convergence
        for(i = 0; i < mNumOutputs; i++)
        {
            errors[i] = outputArray[i] - mOutputNeuronValues[i];
        }
        return errors;
    }

    public double train(double[] x, double argValue)
    {
        double error = 0.0;
        return error;
    }

    /**
     * @param x The input vector. An array of doubles.
     * @return The value returned by the NN for this input vector
     */
    public double outputFor(double[] x)
    {
       // System.out.println("Inside outputFor()!!!");
        int input, hiddenNeuron, outputNeuron, index;
        nn_Input_Values = x;

        for (index = 0; index < x.length; index++)
        {
            nn_Input_Values[index+1] = x[index];
        }

        // Applying the activation function to each neuron bias in the hidden neuron
        for(hiddenNeuron = 0; hiddenNeuron < nn_Num_Hidden_Neurons; hiddenNeuron++) {
            nn_Input_to_HiddenNeuron_Signal[hiddenNeuron] = 0.0;
            for(input = 0; input < nn_Num_Inputs; input++) {
                nn_Input_to_HiddenNeuron_Signal[hiddenNeuron] += nn_Input_Weights[hiddenNeuron][input] * nn_Input_Values[input];
            }
            // Applying the activation function on the weighted sum to get the output signal
            nn_HiddenNeuron_to_Output_Signal[hiddenNeuron] = customSigmoid(nn_Input_to_HiddenNeuron_Signal[hiddenNeuron]);
        }
        // Applying the activation function to the output neuron to get output value
        for(outputNeuron = 0; outputNeuron < mNumOutputs; outputNeuron++) {
            nn_OutputNeuron_InputSignal = 0.0;
            for (hiddenNeuron = 0; hiddenNeuron < nn_Num_Hidden_Neurons; hiddenNeuron++) {
                nn_OutputNeuron_InputSignal += nn_HiddenNeuron_to_Output_Signal[hiddenNeuron] * nn_HiddenNeuron_to_Output_Weights[hiddenNeuron];
            }
            nn_OutputNeuron_InputSignal += (1.0 * nn_HiddenNeuron_to_Output_BiasWeight);

            // Applying the activation function on the weighted sum to get the output value
            nn_OutputNeuron_OutputSignal = customSigmoid(nn_OutputNeuron_InputSignal);
        }

        return nn_OutputNeuron_OutputSignal;
    }

    /**
     * This method is called from the OutputFor method.
     * It calculates the error backpropagation error from the output
     * to calculate the weight correction term
     * This function maps to step 6 in the Backpropagation Algorithm
     */
    public void calErrorBackPropagation(double[] expectedValues)
    {
        int hiddenNeuron,outputNeuron, outputNeuronIndex;
        double summedWeightedErrors;;
        for(outputNeuron = 0; outputNeuron < mNumOutputs; outputNeuron++)
        {
            // Calculate the output error from the feed forward
            mOutputNeuronErrors[outputNeuron] = (expectedValues[outputNeuron] - mOutputNeuronValues[outputNeuron]) * customSigmoidDerivative(mOutputNeuronUnactivatedValues[outputNeuron]);

            // Backpropagate the output error
            for(hiddenNeuron = 0; hiddenNeuron < nn_Num_Hidden_Neurons; hiddenNeuron++)
            {
                summedWeightedErrors = 0.0;

                // Sum all of the output neuron errors * hidden neuron weights
                for(outputNeuronIndex = 0; outputNeuronIndex < mNumOutputs; outputNeuronIndex++)
                {
                    summedWeightedErrors += mOutputNeuronErrors[outputNeuronIndex] * mOutputNeuronWeights[outputNeuronIndex][hiddenNeuron];
                }
                // Multiply weighted sum with derivative of activation
                nn_HiddenNeuron_Errors[hiddenNeuron] = summedWeightedErrors * customSigmoidDerivative(nn_Input_to_HiddenNeuron_Signal[hiddenNeuron]);
            }
        }
    }

    /**
     * All the weights are updated based on the most
     * recently computed backpropagated error.
     */
    public void computeNewWeights()
    {
       // System.out.println("Inside computeNewWeights()!!!");
        int input,hiddenNeuron,outputNeuron;
        double[] newOutputNeuronBiasWeights = new double[MAX_OUTPUTS];
        double[][] newOutputNeuronWeights = new double[MAX_OUTPUTS][MAX_HIDDEN_NEURONS];
        double[][] updatedWeights_InputNeuron = new double[Constants.MAX_HIDDEN_NEURONS][Constants.MAX_INPUTS];

        // Compute new weights for each neuron from the hidden layer to the output
        for(outputNeuron = 0; outputNeuron < mNumOutputs; outputNeuron++) {
            // Bias input weight
            newOutputNeuronBiasWeights[outputNeuron] =
                    mOutputNeuronBiasWeights[outputNeuron] +
                            calculateWeightDelta(1.0,
                                    mOutputNeuronErrors[outputNeuron],
                                    mOutputNeuronBiasWeights[outputNeuron],
                                    mPreviousOutputNeuronBiasWeights[outputNeuron]);
            for (hiddenNeuron = 0; hiddenNeuron < nn_Num_Hidden_Neurons; hiddenNeuron++) {
                newOutputNeuronWeights[outputNeuron][hiddenNeuron] =
                        mOutputNeuronWeights[outputNeuron][hiddenNeuron] +
                                calculateWeightDelta(
                                        nn_HiddenNeuron_to_Output_Signal[hiddenNeuron],
                                        mOutputNeuronErrors[outputNeuron],
                                        mOutputNeuronWeights[outputNeuron][hiddenNeuron],
                                        mPreviousOutputNeuronWeights[outputNeuron][hiddenNeuron]);
            }
        }
        // Compute new weights for input to each neuron in the hidden layer
        for(hiddenNeuron = 0; hiddenNeuron < nn_Num_Hidden_Neurons; hiddenNeuron++) {
            for(input = 0; input < nn_Num_Inputs; input++) {
                updatedWeights_InputNeuron[hiddenNeuron][input] = nn_Input_Weights[hiddenNeuron][input] +
                        calculateWeightDelta( nn_Input_Values[input],
                                              nn_HiddenNeuron_Errors[hiddenNeuron],
                                              nn_Input_Weights[hiddenNeuron][input],
                                              nn_Old_Input_Weights[hiddenNeuron][input]);
            }
        }

        mPreviousOutputNeuronBiasWeights = mOutputNeuronBiasWeights.clone();
        mPreviousOutputNeuronWeights = mOutputNeuronWeights.clone();
        nn_Old_Input_Weights = nn_Input_Weights.clone();

        mOutputNeuronBiasWeights = newOutputNeuronBiasWeights.clone();
        mOutputNeuronWeights = newOutputNeuronWeights.clone();
        nn_Input_Weights = updatedWeights_InputNeuron.clone();
    }

    public double calculateWeightDelta(double weightInput, double error, double currentWeight, double previousWeight)
    {
        double momentumTerm, learningTerm;

        momentumTerm = nn_MomentumTerm * (currentWeight - previousWeight);
        learningTerm = nn_LearningRate * error * weightInput;
        return (momentumTerm + learningTerm);
    }

    /**
     * Generates a random value within upper and lower bound provided by the user
     * @param min lower bound of the random number to be generated
     * @param max upper bound of the random number to be generated
     * @return a random number within lower and upper bound
     */
    private double generateRandom(double min, double max)
    {
        double random, result;
        random = new Random().nextDouble();
        result = (random * (max - min));
        return result;
    }

    /**
     * Turns the neural network's weight into a multidimensional ArrayList
     * @return A multidimensional ArrayList object containing the network's weights
     */
    public ArrayList<ArrayList<ArrayList<Double>>> getWeights()
    {
        int i, j;
        ArrayList<ArrayList<ArrayList<Double>>> networkWeights = new ArrayList<>();
        ArrayList<ArrayList<Double>> inputWeights = new ArrayList<>();
        ArrayList<ArrayList<Double>> outputWeights = new ArrayList<>();
        ArrayList<Double> weightSubSet;

        // get input neuron weights
        for(i = 0; i < nn_Num_Hidden_Neurons; i++)
        {
            weightSubSet = new ArrayList<>();

            for(j = 0; j < nn_Num_Inputs; j++)
            {
                weightSubSet.add(nn_Input_Weights[i][j]);
            }

            inputWeights.add(weightSubSet);
        }

        networkWeights.add(inputWeights);

        // get output neuron weights
        for(i = 0; i < mNumOutputs; i++)
        {
            weightSubSet = new ArrayList<>();

            weightSubSet.add(mOutputNeuronBiasWeights[i]);
            for(j = 0; j < nn_Num_Hidden_Neurons; j++)
            {
                weightSubSet.add(mOutputNeuronWeights[i][j]);
            }
            outputWeights.add(weightSubSet);
        }

        networkWeights.add(outputWeights);

        return networkWeights;
    }

    /**
     * Applies an inputted multidimensional ArrayList to the Neural Network's weights
     * @param weights A multidimensional ArrayList (usually produced by getWeights)
     */
    public void setWeights(ArrayList<ArrayList<ArrayList<Double>>> weights)
    {
        int i, j;

        // set input neuron weights
        for(i = 0; i < nn_Num_Hidden_Neurons; i++)
        {
            for(j = 0; j < nn_Num_Inputs; j++)
            {
                nn_Input_Weights[i][j] = weights.get(WEIGHTS_INPUT_INDEX).get(i).get(j);
            }
        }

        // set output neuron weights
        for(i = 0; i < mNumOutputs; i++)
        {
            mOutputNeuronBiasWeights[i] = weights.get(WEIGHTS_OUTPUT_INDEX).get(i).get(0);

            for(j = 0; j < nn_Num_Hidden_Neurons; j++)
            {
                // initialize the output neuron weights
                mOutputNeuronWeights[i][j] = weights.get(WEIGHTS_OUTPUT_INDEX).get(i).get(j+1);
            }
        }

        // Copy the initial weights into the delta tracking variables
        nn_Old_Input_Weights = nn_Input_Weights.clone();
        mPreviousOutputNeuronWeights = mOutputNeuronWeights.clone();
        mPreviousOutputNeuronBiasWeights = mOutputNeuronBiasWeights.clone();

        nn_Old_HiddenNeuron_Output_BiasWeight = nn_HiddenNeuron_to_Output_BiasWeight;
        nn_Old_HiddenNeuron_Output_Weights = nn_HiddenNeuron_to_Output_Weights.clone();

    }

    public void save(File argFile) {
        //Do Nothing
    }

    public void load(String argFileName) throws IOException {
        //Do nothing
    }
}
