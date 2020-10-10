package Assignment2;

import java.io.File;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import robocode.RobocodeFileOutputStream;
import java.util.Random;

public class LUTData {
    private double[][] lutData;
    private static final boolean zeroInit = true;
    //private int cnt = 0;

    public LUTData() {
        lutData = new double[RoboLUTState.NUMBER_OF_STATES][RobotActions.NUMBER_OF_ACTIONS];
        init();
    }

    private void init() {
        for (int i = 0; i < RoboLUTState.NUMBER_OF_STATES; i++) {
            for (int j = 0; j < RobotActions.NUMBER_OF_ACTIONS; j++) {
                if (zeroInit) {
                    lutData[i][j] = 0.0;
                } else {
                    Random nn = new Random();
                    lutData[i][j] = nn.nextInt(4)-2;
                }
            }
        }
    }

    public double getMaxQValue(int state) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < lutData[state].length; i++) {
            if (lutData[state][i] > max)
                max = lutData[state][i];
        }
        return max;
    }

    public int getBestAction(int state) {
        double max = Double.NEGATIVE_INFINITY;
        int best_Action = 0;
        for (int i = 0; i < lutData[state].length; i++) {
            double qValue = lutData[state][i];
            if (qValue > max) {
                max = qValue;
                best_Action = i;
            }
        }
        return best_Action;
    }

    public double getQValue(int state, int action) {
        return lutData[state][action];
    }

    public void set_QValue(int state, int action, double value) {
        lutData[state][action] = value;
    }

    /**
     * Loads the LUT or neural net weights from file. The load must of course
     * have knowledge of how the data was written out by the save method.
     * You should raise an error in the case that an attempt is being made to
     * load data into a LUT or neural net whose structure does not match
     * the data in the file (e.g. wrong number of hidden neurons).
     * @throws IOException
     */
    public void lutLoadFile(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            for (int i = 0; i < RoboLUTState.NUMBER_OF_STATES; i++)
                for (int j = 0; j < RobotActions.NUMBER_OF_ACTIONS; j++)
                    lutData[i][j] = Double.parseDouble(reader.readLine());
        } catch (IOException e) {
            System.out.println("IOException occurred:: " + e);
            init();
        } catch (NumberFormatException e) {
            init();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                System.out.println("IOException occurred:: " + e);
            }
        }
    }

    /**
     * A method to write either a LUT or weights of a neural net to a file.
     */
    public void lutSaveFile(File file) {
        PrintStream writer = null;
        try {
            writer = new PrintStream(new RobocodeFileOutputStream(file));
            for (int i = 0; i < RoboLUTState.NUMBER_OF_STATES; i++)
                for (int j = 0; j < RobotActions.NUMBER_OF_ACTIONS; j++)
                    writer.println(new Double(lutData[i][j]));
            if (writer.checkError())
                System.out.println("Data not saved to file...");
            writer.close();
        } catch (IOException e) {
            System.out.println("IOException occurred:: " + e);
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (Exception e) {
                System.out.println("Exception when closing the file: " + e);
            }
        }
    }
}
