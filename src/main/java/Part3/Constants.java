package Part3;

/**
 * This class holds the configuration setup parameters for all the three parts
 * 1a, 1b and 1c for CPEN502 assignment on BackpropagationLearning.
 * @date 19 October 2018
 * @author @Neha, @Manik
 *
 */
public class Constants {

    //Neural Net input initialization
    static final int NUM_INPUTS = 4; //Number of Neural Net inputs
    static final int NUM_HIDDEN_NEURONS = 100; //Number of Hidden Neural Net Neurons

    static final int NUM_OUTPUTS = 8;// Number of NeuralNet outputs
    static final double TOTAL_ERROR = 0.05; //Total Convergence Error

    // Binary and Bipolar version of the XOR training set {bias, input1, input2}
    static final double[][] BINARY_XOR_INPUT = new double[][]
            {{ 1.0, 0.0, 0.0}, { 1.0, 0.0, 1.0}, { 1.0, 1.0, 0.0}, { 1.0, 1.0, 1.0}};
    static final double[][] BIPOLAR_XOR_INPUT = new double[][]
            {{ 1.0,-1.0,-1.0}, { 1.0,-1.0, 1.0}, { 1.0, 1.0,-1.0}, { 1.0, 1.0, 1.0}};

    //Binary and Bipolar versions of the XOR training set {output}
    static final double[] BINARY_XOR_OUTPUT = new double[]
            { 0.0, 1.0, 1.0, 0.0};
    static final double[] BIPOLAR_XOR_OUTPUT = new double[]
            { -1.0, 1.0, 1.0, -1.0};

    //Maximum number of epochs and convergence trials
    static final int NUM_CONVERGENCE_TRIALS = 500;
    static final int NUM_MAX_EPOCHS = 6000;

    //Scalable Neural Set Setup
    static final int MAX_HIDDEN_NEURONS =  16;
    static final int MAX_INPUTS =          16;

    // Part - 1a configuration setup
    static final int Q1A_MIN_VALUE = 0 ;
    static final int Q1A_MAX_VALUE = 1;

    static final double Q1A_MIN_WEIGHT = -0.5;
    static final double Q1A_MAX_WEIGHT = 0.5;

    static final double Q1A_MOMENTUM = 0.0;
    static final double Q1A_LEARNING_RATE = 0.2;

    // Part - 1b configuration setup
    static final int Q1B_MIN_VALUE = -1;
    static final int Q1B_MAX_VALUE = 1;

    static final double Q1B_MIN_WEIGHT = -0.5;
    static final double Q1B_MAX_WEIGHT = 0.5;

    static final double Q1B_MOMENTUM = 0.0;
    static final double Q1B_LEARNING_RATE = 0.005;

    // Part - 1c configuration setup
    static final int Q1C_MIN_VALUE = -1;
    static final int Q1C_MAX_VALUE = 1;

    static final double Q1C_MIN_WEIGHT = -2.0;
    static final double Q1C_MAX_WEIGHT = 1.5;

    static final double Q1C_MOMENTUM = 0.9;
    static final double Q1C_LEARNING_RATE = 0.2;
}
