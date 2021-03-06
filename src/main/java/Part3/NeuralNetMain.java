package Part3;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class triggers the setup of Neural Net for part 1a, 1b and 1c,
 * runs the trials until the maximum epoch is reached and the error converges.
 * @date 19 October 2018
 * @author @Neha, @Manik
 *
 */
public class NeuralNetMain {
/*
    private static boolean mShuffleTrainingSet = true;

    // LUT file and properties
    private static final String LUT_FILE_NAME = "1MQLEARN.dat";
    private static File mLutFile;
    // LUT Hashmap to store state/action probabilities
    private static HashMap<Integer, Double> mReinforcementLearningLUTHashMap = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Double>> mStateToQ = new HashMap<>();
    private static HashMap<Integer, Double> mStateToQInner = new HashMap<>();
    private static boolean mDebug = true;

    // LUT hash encodings
    private static final int ACTION_FIELD_WIDTH = 3;
    private static final int ACTION_FIELD_OFFSET = 16;
    // Action hash field and offsets
    private static final int ACTION_MOVE_OFFSET = 0;
    private static final int ACTION_MOVE_WIDTH = 2;
    private static final int ACTION_FIRE_OFFSET = 2;
    private static final int ACTION_FIRE_WIDTH = 1;
    private static final int STATE_FIELD_WIDTH = 16;
    private static final int STATE_FIELD_OFFSET = 0;

    // Move actions
    private static final int ACTION_MOVE_UP = 0;
    private static final int ACTION_MOVE_DN = 1;
    private static final int ACTION_MOVE_LT = 2;
    private static final int ACTION_MOVE_RT = 3;
    private static final int ACTION_MOVE_NUM = 4;
    private static final int ACTION_MOVE_DISTANCE = 50;
    // Fire actions
    private static final int ACTION_FIRE_0 = 0;
    private static final int ACTION_FIRE_3 = 1;
    private static final int ACTION_FIRE_NUM = 2;

    // Misc. constants used in the robot
    private static final int ARENA_SIZEX_PX = 800;
    private static final int ARENA_SIZEY_PX = 600;

    private static final int TRAINING_SET_STATE_INDEX = 0;
    private static final int TRAINING_SET_ACTION_INDEX = 1;

    private static final int STATE_POS_X_WIDTH = 4;
    private static final int STATE_POS_X_OFFSET = 0;
    private static final int STATE_POS_X_MAX = ARENA_SIZEX_PX;

    private static final int STATE_POS_Y_WIDTH = 4;
    private static final int STATE_POS_Y_OFFSET = 4;
    private static final int STATE_POS_Y_MAX = ARENA_SIZEY_PX;

    private static final int STATE_DISTANCE_WIDTH = 4;
    private static final int STATE_DISTANCE_OFFSET = 8;
    private static final int STATE_DISTANCE_MAX = 1000;

    private static final int STATE_ROBOT_HEADING_WIDTH = 4;
    private static final int STATE_ROBOT_HEADING_OFFSET = 12;
    private static final int STATE_ROBOT_HEADING_MAX = 360;

    public static void main(String[] args) throws IOException
    {
        double num_avg_epoch_3;
        ArrayList<ArrayList<ArrayList<Double>>> bipolarTrainingSet = new ArrayList<>();
        ArrayList<Double> results = new ArrayList();
        NeuralNet nnObj;
        int state, action, moveAction, fireAction, completeHash, actionIndex, index;
        double qVal, qMin, qMax;

        // Quantized values
        int quantRobotX;
        int quantRobotY;
        int quantDistance;
        int quantRobotHeading;

        // Intermediate values
        double robotHeadingInDegrees;

        // Bipolar values
        double bipolarRobotX;
        double bipolarRobotY;
        double bipolarDistance;
        double bipolarRobotHeading;

        mLutFile = new File(LUT_FILE_NAME);

        // Load LUT file
        loadLut(mLutFile);
        printDebug("LUT file has %d entries\n", mReinforcementLearningLUTHashMap.size());

        // Set min & max values accordingly
        qMin = 999.0;
        qMax = -999.0;

        for (Integer fullHash : mReinforcementLearningLUTHashMap.keySet())
        {
            // Get the state
            state = getIntFieldVal(fullHash, STATE_FIELD_WIDTH, STATE_FIELD_OFFSET);
            // Check if the state has already been parsed
            if (!mStateToQ.containsKey(state))
            {
                actionIndex = 0;
                // Key must be parsed, get associated state/action pairs and their Qs
                for (moveAction = 0; moveAction < ACTION_MOVE_NUM; moveAction++)
                {
                    for (fireAction = 0; fireAction < ACTION_FIRE_NUM; fireAction++)
                    {
                        // Calculate the action hash and create the complete hash by adding it to the current state hash
                        action = generateActionHash(moveAction, fireAction);
                        // Generate complete hash from action and state
                        completeHash = combineStateActionHashes(state, action);
                        // Get the Q value for the given state & action
                        qVal = mReinforcementLearningLUTHashMap.get(completeHash);
                        if (qVal < qMin)
                        {
                            qMin = qVal;
                        }
                        if (qVal > qMax)
                        {
                            qMax = qVal;
                        }
                        // We should now have the action with the highest Q value, construct our training pair
                        mStateToQInner.put(actionIndex, qVal);
                        actionIndex++;
                        //printDebug("Adding action 0x%1x for state 0x%08x with Q value of %3.5f\n", action, state, qVal);
                    }
                }
                mStateToQ.put(state, mStateToQInner);
                mStateToQInner = new HashMap<>();
            }
        }

        printDebug("Training set has %d entries\n", mStateToQ.size());
        printDebug("Max Q was %f\n", qMax);
        printDebug("Min Q was %f\n", qMin);

        // Raw training set is now obtained, need to convert values into NN friendly I/Os
        for (Integer trainingState : mStateToQ.keySet())
        {
            //printDebug("\nState: 0x%08x Action: %x\n", trainingState, mStateToBestActionMap.get(trainingState));
            // Get our quantized values
            quantRobotX = getIntFieldVal(trainingState, STATE_POS_X_WIDTH, STATE_POS_X_OFFSET);
            quantRobotY = getIntFieldVal(trainingState, STATE_POS_Y_WIDTH, STATE_POS_Y_OFFSET);
            quantDistance = getIntFieldVal(trainingState, STATE_DISTANCE_WIDTH, STATE_DISTANCE_OFFSET);
            quantRobotHeading = getIntFieldVal(trainingState, STATE_ROBOT_HEADING_WIDTH, STATE_ROBOT_HEADING_OFFSET);

            // Scale the quantizations to bipolar binary representations
            bipolarRobotX = (quantRobotX * 2.0 / 16.0) - 1.0;
            bipolarRobotY = (quantRobotY * 2.0 / 16.0) - 1.0;
            bipolarDistance = (quantDistance * 2.0 / 16.0) - 1.0;
            robotHeadingInDegrees = (quantRobotHeading * 360 / 16.0);
            bipolarRobotHeading = Math.cos(Math.toRadians(robotHeadingInDegrees));

            ArrayList<ArrayList<Double>> stateAndActionPair = new ArrayList<>();
            ArrayList<Double> bipolarState = new ArrayList<>();
            ArrayList<Double> bipolarAction = new ArrayList<>();

            bipolarState.add(0, bipolarRobotX);
            bipolarState.add(1, bipolarRobotY);
            bipolarState.add(2, bipolarDistance);
            bipolarState.add(3, bipolarRobotHeading);

            for (index = 0; index < 8; index++)
            {
                bipolarAction.add((mStateToQ.get(trainingState).get(index))/(-qMin));
            }

            stateAndActionPair.add(bipolarState);
            stateAndActionPair.add(bipolarAction);

            // Finally add the state and action pair
            bipolarTrainingSet.add(stateAndActionPair);
        }

        // Part 3 - Code Implementation
        System.out.println("Part 4c Code Implementation Begins!!!!!");
        nnObj = new NeuralNet( Constants.NUM_INPUTS, Constants.NUM_OUTPUTS,Constants.NUM_HIDDEN_NEURONS,
                                Constants.Q1B_LEARNING_RATE, Constants.Q1B_MOMENTUM, Constants.Q1B_MIN_VALUE, Constants.Q1B_MAX_VALUE, Constants.Q1B_MIN_WEIGHT, Constants.Q1B_MAX_WEIGHT);
        trialsExecution(nnObj, bipolarTrainingSet, Constants.NUM_CONVERGENCE_TRIALS, Constants.TOTAL_ERROR, Constants.NUM_MAX_EPOCHS, results);
        execResultsToFile(results, "4c.csv");

        System.out.println("Results of Part 1b: " + Constants.NUM_CONVERGENCE_TRIALS + " successful trials for convergence below total error of " + Constants.TOTAL_ERROR + ",");
        //System.out.format(" required an average of %1.3f epochs.", num_avg_epoch_3);
        System.out.println();

    }

    *//**
     * Combine a state and action hash together to form a complete hash
     * @param stateHash State hash
     * @param actionHash Action hash
     * @return The complete state/action hash
     *//*
    private static int combineStateActionHashes(int stateHash, int actionHash)
    {
        return updateIntField(stateHash, ACTION_FIELD_WIDTH, ACTION_FIELD_OFFSET, actionHash);
    }

    public static void execResultsToFile(ArrayList<Double> results, String fileName) throws IOException
    {
        int epoch;
        PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
        printWriter.printf("Epoch, Total Squared Error,\n");
        for(epoch = 0; epoch < results.size(); epoch++) {
            printWriter.printf("%d, %f,\n", epoch, results.get(epoch));
        }
        printWriter.flush();
        printWriter.close();
    }

    *//**
     * This method runs multiple trials until the
     * error convergence occurs or maximum epoch is reached.
     * @param nn_Obj object reference to the neural net
     * @return average number of epochs taken for the error to converge below 0.05
     *//*
    public static void trialsExecution(NeuralNet nn_Obj, ArrayList<ArrayList<ArrayList<Double>>> trainingSet, int numTrials, double convergenceError, int maximumEpochs, ArrayList<Double> results)
    {
        // Clear our results
        results.clear();
        // Initialize weights for a new training session
        nn_Obj.initializeWeights();

        ArrayList<ArrayList<ArrayList<Double>>> weights = nn_Obj.getWeights();
        nn_Obj.setWeights(weights);
        // Attempt convergence
        errorConvergence(nn_Obj, trainingSet, convergenceError, maximumEpochs, results);
    }

    *//**
     * This method computes the total number of epochs
     * required for error convergence until total error is below 0.05.
     * @param nn_Obj object reference to the neural net
     * @return total number of epochs error converge below 0.05
     *//*
    public static void errorConvergence(NeuralNet nn_Obj, ArrayList<ArrayList<ArrayList<Double>>> trainingSet, double totalError, int maximumEpochs, ArrayList<Double> results)
    {
        double cummError, setCummError;
        int index, epoch, output;

        double[] errors;

        for (epoch = 0; epoch < maximumEpochs; epoch++)
        {
            // Shuffle the training set
            if (mShuffleTrainingSet)
            {
                Collections.shuffle(trainingSet);
            }

            cummError = 0.0;
            for (index = 0; index < trainingSet.size(); index++)
            {
                setCummError = 0.0;
                errors = nn_Obj.train(trainingSet.get(index));
                for (output = 0; output < errors.length; output++)
                {
                    setCummError += errors[output] * errors[output];
                }
                setCummError /= 8;
                cummError += setCummError;
            }

            // RMS error
            cummError /= trainingSet.size();
            cummError = Math.sqrt(cummError);
            //printDebug("%f %f %f %f %f %f %f %f\n",
            //errors[0], errors[1], errors[2], errors[3], errors[4], errors[5], errors[6], errors[7]);
            printDebug("Epoch: %09d RMS: %f\n", epoch, cummError);

            // Append the result to our list
            results.add(cummError);

            if (cummError < Constants.TOTAL_ERROR)
            {
                break;
            }
        }
    }

    *//**
     * Returns the value of a field in an int
     *
     * @param inputInteger The input integer to extract the value from
     * @param fieldWidth   The width of the field to extract
     * @param fieldOffset  The offset of the field to extract
     * @return Returns the value in the selected field
     *//*
    private static int getIntFieldVal(int inputInteger, int fieldWidth, int fieldOffset)
    {
        int returnValue;
        int mask;

        returnValue = inputInteger;

        // Create mask
        mask = ((1 << fieldWidth) - 1) << fieldOffset;
        // Mask out the field from the input
        returnValue &= mask;
        // Shift down to grab it
        returnValue >>>= fieldOffset;

        return returnValue;
    }

    *//**
     * This generates a hash for a given action. Everything is encoded in an int
     *
     * @return Returns a hash based on the selected action
     *//*
    private static int generateActionHash(int moveAction, int fireAction)//, int aimAction)
    {
        // Robot can do two things simultaneously:
        // Move up, down, left, or right                        (4)
        // Don't fire or fire 3                                 (2)
        // 4 * 2 = 8 action possibilities, need at least 3 bits
        int actionHash = 0;

        actionHash = updateIntField(actionHash, ACTION_MOVE_WIDTH, ACTION_MOVE_OFFSET, moveAction);
        actionHash = updateIntField(actionHash, ACTION_FIRE_WIDTH, ACTION_FIRE_OFFSET, fireAction);

        //printDebug("Action hash: 0x%08x\n", actionHash);

        return actionHash;
    }

    *//**
     * Updates a field in an int
     *
     * @param inputInteger The input integer to modify
     * @param fieldWidth   The width of the field to modify
     * @param fieldOffset  The field's offset
     * @param value        The value to update into the field
     * @return The updated input integer
     *//*
    private static int updateIntField(int inputInteger, int fieldWidth, int fieldOffset, int value)
    {
        int returnValue;
        int mask;

        returnValue = inputInteger;

        // Create mask
        mask = ~(((1 << fieldWidth) - 1) << fieldOffset);
        // Mask out field from input
        returnValue &= mask;
        // OR in the new value into the field
        returnValue |= value << fieldOffset;

        return returnValue;
    }

    *//**
     * Conditionally prints a message if the debug flag is on
     *
     * @param format    The string to format
     * @param arguments The string format's variables
     *//*
    private static void printDebug(String format, Object... arguments)
    {
        if (mDebug)
        {
            System.out.format(format, arguments);
        }
    }

    *//**
     * Load the lookup table hashmap
     *
     * @param lutFile The filename to use for the lookup table hashmap
     *//*
    private static void loadLut(File lutFile)
    {
        try
        {
            printDebug("Loading LUT from file...\n");
            FileInputStream fileIn = new FileInputStream(lutFile);
            //ObjectInputStream in = new ObjectInputStream(fileIn);
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(fileIn));
            mReinforcementLearningLUTHashMap = (HashMap<Integer, Double>) in.readObject();
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
        }
    }*/
}
