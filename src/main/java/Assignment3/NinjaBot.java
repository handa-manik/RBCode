package Assignment3;

import com.sun.javafx.geom.Point2D;
import robocode.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;

import com.sun.javafx.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RobocodeFileOutputStream;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static robocode.util.Utils.normalRelativeAngleDegrees;
/**
 * This class implements the Robot as a part of Assignment 2
 * The LUT data generated is stored in .dat file and requires
 * robocode.robot.filesystem.quota to be set to a higher value
 * @date 13 November 2018
 * @author @Neha, @Manik
 *
 */
@SuppressWarnings("unused")
public class NinjaBot extends AdvancedRobot{

    public static final boolean IS_RLNN = true;

    final double ALPHA = 0.1; //Fraction of difference used
    final double GAMMA = 0.9; //Discount Factor
    final double EPSILON = 0.9; //Probability of Exploration

    //LUT table initialization
    int[] action=new int[4];
    int[] all_Actions =new int[4];
    double[] train_X =new double[5];
    double[] value_X =new double[5];
    double[] newTrain_X =new double[5];
    double[] newTrain_Y =new double[1];
    static String [][] weight = new String[22][3];
    Map<String, Object> valuesMap = new HashMap<String, Object>();

    //standard robocode parameters
    double turnGunValue;
    double bearing;
    double absbearing=0;
    double enemy_distance =0;
    private double getVelocity;
    private double getBearing;

    //quantized parameters
    double quant_X =0;
    double quant_Y =0;
    double q_Distance =0;
    double quantizeAbsBearing =0;

    //initialize reward related variables
    double reward=0;
    double new_QValue;
    double err_Value;
    static double [] err_All = new double[10000];
    int random_action =0;
    int selectAction = 0;
    double prevQValue;
    double latestQValue;
    int action_QGreedy =0;
    int[] actionsMatched =new int[all_Actions.length];
    double[] valuesQ =new double[all_Actions.length];
    double[] possQValue =new double[all_Actions.length];
    DecimalFormat decFormat = new DecimalFormat("#0.00");
    String[] truncTrain_X;
    String[] truncValue_X;

    private double firePower;
    private short isHitWall = 0;
    private short isHitByBullet = 0;
    private int hit_enemy_counter = 0;
    private int fire_enemy_counter = 0;
    private int ram_counter = 0;
    private int win = 0;

    int counter = 0;
    boolean match;
    static int [] winning_Rate = new int[10000];


    public void run(){

        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(Color.RED);
        setScanColor(Color.WHITE);
        setBulletColor(Color.PINK);

        if(IS_RLNN){
            //do nothing
        }

        if(getRoundNum() != 0)
        {
            try {
                weight_Update();}
            catch(IOException e) {
                e.printStackTrace();
            }
        }

        while(true){

            //draw a random value for E greedy
            Random random_num = new Random();
            double check_EPSILON = random_num.nextDouble();

            //turn gun to scan
            turnGunRight(360);

            //random action with prob = EPSILON
            if (check_EPSILON <= EPSILON) {
                random_action = int_generateRandom(1, all_Actions.length);

                train_X[0]= quant_X;
                train_X[1]= quant_Y;
                train_X[2]= q_Distance;
                train_X[3]= quantizeAbsBearing;
                train_X[4]= random_action;

                try {
                    if (getRoundNum() == 0)
                        valuesMap = NeuralNet.start(train_X, newTrain_Y,true,false,weight);
                    else
                        valuesMap = NeuralNet.start(train_X, newTrain_Y,false,false,weight);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                new_QValue = (double) valuesMap.get("value");
                weight = (String[][]) valuesMap.get("array");
            }
            else if (check_EPSILON > EPSILON) {
                try {
                    new_QValue = QLearning();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            reward=0;

            if (check_EPSILON <= EPSILON) {
                actionSet(random_action);
                selectAction = random_action;
            }

            else if (check_EPSILON > EPSILON) {
                actionSet(action_QGreedy);
                selectAction = action_QGreedy;
            }

            turnGunRight(360);

            try {
                prevQValue = QLearning();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            new_QValue = new_QValue +ALPHA*(reward+ GAMMA * prevQValue - new_QValue);
            newTrain_Y[0] = new_QValue;

            err_Value = prevQValue - new_QValue;
            err_All[getRoundNum()] = err_Value;

            try {
                valuesMap = NeuralNet.start(train_X, newTrain_Y,false,true,weight);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            latestQValue = (double) valuesMap.get("value");
            weight = (String[][]) valuesMap.get("array");

            weight_Save();

        }

    }

    public double QLearning() throws IOException
    {
        for(int j = 1; j<= all_Actions.length; j++)
        {
            train_X[0]= quant_X;
            train_X[1]= quant_Y;
            train_X[2]= q_Distance;
            train_X[3]= quantizeAbsBearing;
            train_X[4]=j;

            valuesMap =NeuralNet.start(train_X, newTrain_Y,false,false,weight);

            possQValue[j-1] = (double) valuesMap.get("value");
            weight = (String[][]) valuesMap.get("array");
        }

        action_QGreedy = getMaxQValue(possQValue)+1;

        return possQValue[action_QGreedy -1];

    }

    public void onHitRobot(HitRobotEvent e) {
        double change = -2.0;
        reward += change;
        ram_counter++;
        out.println("Ramming the enemy!!! "+ ram_counter +" ::");
        setDebugProperty("onHitRobot", " "+change);
    }

    public void onBulletHit(BulletHitEvent e) {
        double change = 3.0;
        reward += change;
        hit_enemy_counter++;
        setDebugProperty("onBulletHit", " "+change);
    }
    public void onHitByBullet(HitByBulletEvent e) {
        double change = -3.0;
        reward += change; //
        isHitByBullet = 1;
        setDebugProperty("onHitByBullet", " "+change);
    }

    /**
     * Generates a random value within upper and lower bound provided by the user
     * The random number generated is used to choose the random action
     * @param min lower bound of the random number to be generated
     * @param max upper bound of the random number to be generated
     * @return a random number within lower and upper bound
     */
    public static int int_generateRandom(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    /**
     * This function is used to get the maximum QValue
     * @param array of Q values
     * @return index of maximum Q value
     */
    public static int getMaxQValue(double[] array){
        int index = 0;
        double maxQValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if ( array[i] >= maxQValue ) {
                maxQValue = array[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * This function is used to quantize enemy_distance to four values
     * @param distance to be quantised
     * @return quantized enemy_distance
     */
    private double quantize_Distance(double distance) {
        q_Distance = distance/100;
        return q_Distance;
    }

    public void onScannedRobot(ScannedRobotEvent e)
    {
        double Velocity=e.getVelocity();
        this.getVelocity=Velocity;
        double absbearing=e.getBearing();
        this.getBearing=absbearing;

        this.turnGunValue = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading() -15);

        enemy_distance = e.getDistance();
        q_Distance = quantize_Distance(enemy_distance);

        //fire depending on the quantized enemy_distance
        if(q_Distance <=2.50){fire(3);}
        if(q_Distance >2.50&& q_Distance <5.00){fire(2);}
        if(q_Distance >5.00&& q_Distance <7.50){fire(1);}

        quant_X = quantizedPosition(getX());
        quant_Y = quantizedPosition(getY());

        double enemyAngle   = e.getBearing();
        double angle = Math.toRadians((getHeading() + enemyAngle   % 360));
        double positionX = (getX() + Math.sin(angle) * e.getDistance());
        double positionY = (getY() + Math.cos(angle) * e.getDistance());

        this.absbearing =getAbsBearing((float) getX(),(float) getY(),(float) positionX,(float) positionY);
        quantizeAbsBearing = quantizedAngle(this.absbearing); //state number 4

    }

    double getAbsBearing(float xPos, float yPos, float targetX, float targetY) {
        double PrevX = targetX-xPos;
        double PrevY = targetY-yPos;
        double targetEnemy = Point2D.distance(xPos, yPos, targetX, targetY);
        double arcSin = Math.toDegrees(Math.asin(PrevX / targetEnemy));
        double bearing = 0;

        if (PrevX > 0 && PrevY > 0) {
            bearing = arcSin;
        } else if (PrevX < 0 && PrevY > 0) {
            bearing = 360 + arcSin;
        } else if (PrevX > 0 && PrevY < 0) {
            bearing = 180 - arcSin;
        } else if (PrevX < 0 && PrevY < 0) {
            bearing = 180 - arcSin;
        }

        return bearing;
    }

    /**
     * This function is used to quantize bearing to four values
     * @param absBearing to be quantised
     * @return quantized angle
     */
    private double quantizedAngle(double absBearing) {

        return absBearing/90;
    }

    public void actionSet(int x)
    {
        switch(x){
            case 1:
                int moveDirection=+1;
                setTurnRight(getBearing + 90);
                setAhead(150 * moveDirection);
                break;
            case 2:
                int moveDirection1=-1;
                setTurnRight(getBearing + 90);
                setAhead(150 * moveDirection1);
                break;
            case 3:
                setTurnGunRight(turnGunValue);
                turnRight(getBearing-25);
                ahead(150);
                break;
            case 4:
                setTurnGunRight(turnGunValue);
                turnRight(getBearing-25);
                back(150);
                break;
        }
    }

    /**
     * This function is used to quantize position into 8 X and 6 Y values
     * @param position to be quantised
     * @return quantized angle
     */
    private double quantizedPosition(double position) {
        return position/100;
    }

    public void weight_Save()
    {
        PrintStream S = null;
        try {
            S = new PrintStream(new RobocodeFileOutputStream(getDataFile("weight.txt")));
            for (int k=0;k<weight.length;k++) {
                S.println(weight[k][0]+" "+weight[k][1]+"    "+weight[k][2]);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }finally {
            S.flush();
            S.close();
        }
    }

    public void weight_Update() throws IOException
    {
        BufferedReader fileReader = new BufferedReader(new FileReader(getDataFile("weight.txt")));
        String rowLine = fileReader.readLine();
        try {
            int z=0;
            while (rowLine != null) {
                String[] splitLine = rowLine.split("    ");
                String[] splitAgain = splitLine[0].split(" ");
                weight[z][0]=splitAgain[0];
                weight[z][1]=splitAgain[1];
                weight[z][2]=splitLine[1];
                z=z+1;
                rowLine= fileReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            fileReader.close();
        }
    }

    /**
     * This function is used to save the win rate in a file
     * @throws IOException
     */
    public void save_WinRate()
    {
        PrintStream w = null;
        try
        {
            w = new PrintStream(new RobocodeFileOutputStream(getDataFile("winning_Rate.txt")));
            for(int i = 0; i< winning_Rate.length; i++)
                w.println(winning_Rate[i]);
        }
        catch (IOException e) {
            e.printStackTrace();
        }finally {
            w.flush();
            w.close();
        }


    }

    public void save_ErrorTrack()
    {
        PrintStream w = null;
        try
        {
            w = new PrintStream(new RobocodeFileOutputStream(getDataFile("err_All.txt")));
            for(int i = 0; i< err_All.length; i++)
                w.println(err_All[i]);
        }
        catch (IOException e) {
            e.printStackTrace();
        }finally {
            w.flush();
            w.close();
        }
    }



    public void onBattleEnded(BattleEndedEvent event) {
        save_WinRate();
        save_ErrorTrack();
    }

    public void onDeath(DeathEvent event)
    {
        double change =-5.0; //terminal reward
        reward += change;
        winning_Rate[getRoundNum()] = 0;
    }

    public void onWin(WinEvent event)
    {
        double change =5.0;
        reward += change; //terminal reward
        winning_Rate[getRoundNum()] = 1;
    }

}
