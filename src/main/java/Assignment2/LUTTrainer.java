package Assignment2;
import java.util.Random;

public class LUTTrainer {
    public static final double ALPHA = 0.1; //Fraction of difference used
    public static final double GAMMA = 0.9; //Discount Factor
    public static final double EPSILON = 0.9; //Probability of Exploration

    private static final boolean IS_QLearning = true; //flag for Q-learning/SARSA
    private double qDiff;
    private int prev_Action;
    private int prev_State;

    private LUTData lutTable;

    public LUTTrainer(LUTData table) {
        this.lutTable = table;
    }

    public double lutTrain(int state, int action, double imRwd) {
        if (IS_QLearning) {
            //Q-Learning (off-policy)
            double prev_QValue = lutTable.getQValue(prev_State, prev_Action);
            double new_QValue = prev_QValue + ALPHA * (imRwd + GAMMA * lutTable.getMaxQValue(state) - prev_QValue);
            qDiff = new_QValue - prev_QValue;
            System.out.println("previousState:: " + prev_State + ", previousAction:: " + prev_Action);
            System.out.println("Old Q-Value:: " + prev_QValue + ", New Q-Value:: " + new_QValue + ", Different: " + (new_QValue - prev_QValue) + " and " + qDiff);
            /* Bootstrapping - backup new Q-value to old ones */
            lutTable.set_QValue(prev_State, prev_Action, new_QValue);
        } else {
            //SARSA learning (on-policy) learning
            double current_QValue = lutTable.getQValue(state, action);
            double prev_QValue = lutTable.getQValue(prev_State, prev_Action);
            double new_QValue = prev_QValue + ALPHA * (imRwd + GAMMA * current_QValue - prev_QValue);
            lutTable.set_QValue(prev_State, prev_Action, new_QValue);
        }
        prev_State = state;
        prev_Action = action;

        return qDiff;
    }

    public int selectAction(int state) {
        Random rn = new Random();
        if(rn.nextDouble() <= EPSILON) {
            /* random move */
            int random_Number = rn.nextInt(RobotActions.NUMBER_OF_ACTIONS);
            return random_Number;
        } else {
            /* greedy move */
            System.out.println("Resulted in greedy move.....");
            return lutTable.getBestAction(state);
        }
    }
}