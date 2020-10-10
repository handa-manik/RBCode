import java.io.IOException;
import java.util.Random;
import java.io.File;

import Part3.NeuralNet;

public class NeuralNetTrain {
    public static final double ALPHA = 0.1;
    public static final double GAMMA = 0.9;
    public static final double EPSILON = 0.9;
    private static final boolean isQLearning = true;

    private static final int NUM_INPUTS = 5;            // Number of NN inputs
    private static final int NUM_HIDDEN_NEURONS = 100;  // Number of NN hidden neurons
    private static final int NUM_OUTPUTS = 8;           // Number of NN outputs

    private static final double MOMENTUM = 0.0;         // Momentum parameter for backpropagation
    private static final double LEARNING_RATE = 0.005;  // Learning rate parameter for backpropagation


    private double diff;
    private NeuralNet nn;

    public NeuralNetTrain() {
        nn = new NeuralNet(NUM_INPUTS,NUM_OUTPUTS,NUM_HIDDEN_NEURONS,LEARNING_RATE,MOMENTUM,-1,1,-0.5,0.5);
    }

    /*public double nnTrain(double[] inputs, double imRwd) {
        if (isQLearning) {
            *//* Q-Learning (off-policy) learning *//*
            double prevQValue = nn.outputFor(inputs, false);
            double newQValue = prevQValue + ALPHA * (imRwd + GAMMA * getMaxQValue(inputs) - prevQValue);
            diff = newQValue - prevQValue;
            //System.out.println("=== nnTrain() ===");
            //System.out.println("prevState: " + prevState + ", prevAction: " + prevAction);
            //System.out.println("Old Q-Value: " + prevQValue + ", New Q-Value: " + newQValue + ", Different: " + (newQValue - prevQValue) + " and " + diff);
            nn.train(inputs);
        }
        return diff;
    }*/

  /*  public int selectAction(double[] inputs) {
        Random rn = new Random();
        if(rn.nextDouble() <= EPSILON) {
            *//* random move *//*
            int ranNum = rn.nextInt(Action.NumActions);
            return ranNum;
        } else {
            *//* greedy move *//*
            return getBestAction(inputs);
        }
    }*/

    /*public double getMaxQValue(double[] inputs) {

        double maxQ = Double.NEGATIVE_INFINITY;
        double[] temp;
        temp = inputs;
       // go though 5 actions with that particular input sequence
        //for (int i = 0; i < inputs[6]; i++) {
        for (int i = 0; i < Action.NumActions; i++) {
            // need to overwrite action element [6] with all 5 actions
            // then get only outputs from neural network
            // no backpropagation needed here
            temp[Action.NumActions] = i;
            double tempQ = nn.outputFor(temp, false);
            if (tempQ > maxQ)
                maxQ = tempQ;
        }
        return maxQ;
    }*/

    /*public int getBestAction(double[] inputs) {

        double maxQ = Double.NEGATIVE_INFINITY;
        int bestAction = 0;
        double[] temp;
        temp = inputs;
        go though 5 actions with that particular input sequence
        //for (int i = 0; i < inputs[6]; i++) {
        for (int i = 0; i < Action.NumActions; i++) {
            // need to overwrite action element [6] with all 5 actions
            // then get only outputs from neural network
            // no backpropagation needed here
            temp[Action.NumActions] = i;
            double tempQ = nn.outputFor(temp, false);
            if (tempQ > maxQ) {
                maxQ = tempQ;
                bestAction = i;
            }
        }
        return bestAction;
    }*/

    public void nnSave(File f) {
        nn.save(f);
    }

    public void nnLoad(String s) {
        try {
            nn.load(s);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}