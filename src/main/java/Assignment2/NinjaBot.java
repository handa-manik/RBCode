package Assignment2;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.WinEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.BulletHitBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.RoundEndedEvent;
import robocode.BattleEndedEvent;
import robocode.BattleResults;
import robocode.RobocodeFileOutputStream;

/**
 * This class implements the Robot as a part of Assignment 2
 * The LUT data generated is stored in .dat file and requires
 * robocode.robot.filesystem.quota to be set to a higher value
 * @date 13 November 2018
 * @author @Neha, @Manik
 *
 */
public class NinjaBot extends AdvancedRobot {
    public static final boolean IS_RLNN = false;
    public static final double PI = Math.PI;

    private LUTData lutData;
    private Enemy enemy;
    private LUTTrainer lutTrainer;

    private double intermediate_Reward = 0.0;
    private double total_Reward = 0.0;

    private double firePower;
    private short isHitWall = 0;
    private short isHitByBullet = 0;
    private int hit_enemy_counter = 0;
    private int fire_enemy_counter = 0;
    private int ram_counter = 0;
    private int win = 0;

    private int[][] stateIdx = new int[576][6];
    static double[] round_counter = new double[100];
    static int round_Counter = 0;
    static int round_number = 0;

    public void run() {
        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(Color.RED);
        setScanColor(Color.WHITE);
        setBulletColor(Color.PINK);

        if (!IS_RLNN) {
            lutData = new LUTData();
            lutLoad();
        }
        if (!IS_RLNN) {
            lutTrainer = new LUTTrainer(lutData);
        }
        enemy = new Enemy();
        enemy.distance = 1000;

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        turnRadarRightRadians(2 * PI);

        while (true) {
            firePower = 300.0/enemy.distance;
            if (firePower > 3.0)
                firePower = 3.0;
            radarScanSetup();
            gunMoveSetup();
            robotMovementSetup(firePower);
            execute();
        }
    }

    private void robotMovementSetup(double pwr) {

        int current_State = getLutState();
        int current_Actions = lutTrainer.selectAction(current_State);
        out.println("robotMovementSetup() ===");
        out.println("current_State="+current_State+" current_Actions="+current_Actions+" intermediate_Reward="+ intermediate_Reward);
        double diff = lutTrainer.lutTrain(current_State, current_Actions, intermediate_Reward);
        out.println(diff);

        total_Reward = total_Reward + intermediate_Reward;
        setDebugProperty("Reward", " "+ total_Reward);

        intermediate_Reward = 0.0;
        isHitWall = 0;
        isHitByBullet = 0;
        int action = 0;

        if (!IS_RLNN) {
            lutTrainer.selectAction(current_State);
        }

        switch (lutTrainer.selectAction(current_State)) {
            case RobotActions.AHEAD_ACTION:
                setAhead(RobotActions.MOVE_DISTANCE_ACTION);
                break;
            case RobotActions.BACK_ACTION:
                setBack(RobotActions.MOVE_DISTANCE_ACTION);
                break;
            case RobotActions.TURN_LEFT_ACTION:
                setTurnLeft(RobotActions.TURN_DEGREE_ACTION);
                break;
            case RobotActions.TURN_RIGHT_ACTION:
                setTurnRight(RobotActions.TURN_DEGREE_ACTION);
                break;
            case RobotActions.FIRE_ACTION:
                gunFire(pwr);
                break;
        }
    }

    private int getLutState() {
        int heading = RoboLUTState.getHeading(getHeading());
        int enemy_Distance = RoboLUTState.getTargetDistance(enemy.distance);
        int enemy_Bearing = RoboLUTState.getTargetBearing(enemy.bearing);
        int energy = RoboLUTState.getEnergy(getEnergy());
        int state = RoboLUTState.STATE_TABLE[heading][enemy_Distance][enemy_Bearing][isHitWall][isHitByBullet][energy];

        return state;
    }

    private void gunFire(double power) {
        if (getGunHeat() == 0)
            setFire(power);
        fire_enemy_counter++;
    }

    private void radarScanSetup() {
        double radarOffset;
        radarOffset = getRadarHeadingRadians() -
                (PI/2 - Math.atan2(enemy.y - getY(),enemy.x - getX()));
        if (radarOffset > PI)
            radarOffset -= 2*PI;
        if (radarOffset < -PI)
            radarOffset += 2*PI;
        if (radarOffset < 0)
            radarOffset -= PI/10;
        else
            radarOffset += PI/10;
        setTurnRadarLeftRadians(radarOffset);
    }

    /**
     * Turn the gun towards the anticipated next bearing of the target enemy.
     */
    private void gunMoveSetup() {

        long time;
        long nextTime;
        Point2D.Double p;
        p = new Point2D.Double(enemy.x, enemy.y);
        for (int i = 0; i < 20; i++) {
            nextTime = (int)Math.round((getRange(getX(),getY(),p.x,p.y)/(20-(3*firePower))));
            time = getTime() + nextTime - 10;
            p = enemy.targetEnemy(time);
        }
        //offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class
        double gunOffset = getGunHeadingRadians() -
                (PI/2 - Math.atan2(p.y - getY(),p.x -  getX()));
        setTurnGunLeftRadians(normaliseBearing(gunOffset));
    }

    /**
     * If a bearing is not within -pi to pi range,
     * it should be updated to provide the shortest angle.
     * @param angle The original angle.
     * @return The shortest angle.
     */
    double normaliseBearing(double angle) {
        if (angle > PI)
            angle -= 2*PI;
        if (angle < -PI)
            angle += 2*PI;
        return angle;
    }

    /**
     * Returns the distance between two x,y coordinates.
     * @param x1 First x.
     * @param y1 First y.
     * @param x2 Second x.
     * @param y2 Second y.
     * @return The distance between (x1, y1) and (x2, y2).
     */
    public double getRange( double x1,double y1, double x2,double y2 ) {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double h = Math.sqrt( xo * xo + yo * yo );
        return h;
    }

    public void onBulletHit(BulletHitEvent e) {
        double change = 8.0;
        intermediate_Reward += change; //only terminal case
        hit_enemy_counter++;
        setDebugProperty("onBulletHit", " "+change);
    }

    public void onBulletMissed(BulletMissedEvent e) {
        double change = -2.0;
        intermediate_Reward += change; //only terminal case
        setDebugProperty("onBulletMissed", " "+change);
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        double change = -1.0;
        intermediate_Reward += change; //only terminal case
        setDebugProperty("onBulletHitBullet", " "+change);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        double change = -10.0;
        intermediate_Reward += change; //only terminal case
        isHitByBullet = 1;
        setDebugProperty("onHitByBullet", " "+change);
    }

    public void onHitWall(HitWallEvent e) {
        double change = -10.0;
        intermediate_Reward += change; //only terminal case
        isHitWall = 1;
        setDebugProperty("onHitWall", " "+change);
    }

    public void onHitRobot(HitRobotEvent e) {
        double change = -1.0;
        intermediate_Reward += change; //only terminal case
        ram_counter++;
        out.println("Ramming the enemy!!! "+ ram_counter +" ::");
        setDebugProperty("onHitRobot", " "+change);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double absbearing_rad = (getHeadingRadians() + e.getBearingRadians()) % (2 * PI);
        double h = normaliseBearing(e.getHeadingRadians() - enemy.head);
        h = h/(getTime() - enemy.ctime);
        enemy.update_head = h;
        enemy.x = getX()+Math.sin(absbearing_rad)*e.getDistance();
        enemy.y = getY()+Math.cos(absbearing_rad)*e.getDistance();
        enemy.bearing = e.getBearingRadians();
        enemy.head = e.getHeadingRadians();
        enemy.name = e.getName();
        enemy.ctime = getTime();
        enemy.speed = e.getVelocity();
        enemy.distance = e.getDistance();

        if (e.getEnergy() < enemy.energy) {
            double change = e.getEnergy()-enemy.energy;
            if (change != 100 && change <= 3)
                intermediate_Reward += 5.0;//only terminal case
            setDebugProperty("onEnemyEnergyDrop", " "+ intermediate_Reward);
        }
        enemy.energy = e.getEnergy();
    }

    public void onRobotDeath(RobotDeathEvent e) {
        enemy.distance = 1000;
    }

    File winlose;
    public void onWin(WinEvent event) {
  		/* terminal case
  		double change = 6;
  		intermediate_Reward += change;
  		setDebugProperty("onWin", " "+change);
  		*/
        win = 1;
        if (!IS_RLNN)
            lutSave();

        winlose = getDataFile("./winlose.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(winlose));
            String read = br.readLine();
            System.out.println("!!!!!!!!!!Previous!!!!!!!!!!!");
            System.out.println(read);
            System.out.println("!!!!!!!!!!Now!!!!!!!!!!!");
            read += "1;";
            System.out.println(read);
            br.close();
            BufferedOutputStream bos = new BufferedOutputStream(new RobocodeFileOutputStream(winlose));
            bos.write(read.getBytes());
            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        round_counter[round_Counter]=(double) hit_enemy_counter / fire_enemy_counter;
        System.out.println(round_counter[round_Counter]);
        round_Counter++;
        round_number++;
        System.out.println("Fire: "+ fire_enemy_counter +
                " Hit: "+ hit_enemy_counter +
                " Ratio: "+(double) hit_enemy_counter / fire_enemy_counter);
        System.out.println(">>> Reward: " + total_Reward);
    }

    public void onDeath(DeathEvent event) {
  		/* for terminal rewards
  		double change = -6;
  		intermediate_Reward += change;
  		setDebugProperty("onDeath", " "+change);
  		*/
        if (!IS_RLNN)
            lutSave();

        winlose = getDataFile("./winlose.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(winlose));
            String read = br.readLine();
            System.out.println("!!!!!!!!!!Previous!!!!!!!!!!!");
            System.out.println(read);
            System.out.println("!!!!!!!!!!Now!!!!!!!!!!!");
            read += "0;";
            System.out.println(read);
            br.close();
            BufferedOutputStream bos = new BufferedOutputStream(new RobocodeFileOutputStream(winlose));
            bos.write(read.getBytes());
            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        round_counter[round_Counter]=(double) hit_enemy_counter / fire_enemy_counter;
        System.out.println(round_counter[round_Counter]);
        round_Counter++;
        out.println("Fire: "+ fire_enemy_counter +
                " Hit: "+ hit_enemy_counter +
                " Ratio: "+(double) hit_enemy_counter / fire_enemy_counter);
        out.println(">>> Reward: " + total_Reward);
    }
    /**
     * Loads the LUT or neural net weights from file. The load must of course
     * have knowledge of how the data was written out by the save method.
     * You should raise an error in the case that an attempt is being made to
     * load data into a LUT or neural net whose structure does not match
     * the data in the file (e.g. wrong number of hidden neurons).
     * @throws IOException
     */

    public void lutLoad() {
        out.println("Load LUT Data !!!");
        try {
            lutData.lutLoadFile(getDataFile("data.txt"));
        } catch (Exception e) {
        }
    }

    /**
     * A method to write either a LUT or weights of a neural net to a file.
     */
    public void lutSave() {
        out.println("Save LUT Data !!!!");
        try {
            lutData.lutSaveFile(getDataFile("data.txt"));
        } catch (Exception e) {
            out.println("Exception trying to write: " + e);
        }
    }

    public void onRoundEnded(RoundEndedEvent event) {
        if (IS_RLNN) {
            out.println("Save NN weights...");
        } else {
            out.println("LUT implementation...");
        }
    }

    public void onBattleEnded (BattleEndedEvent e) {
        BattleResults result =e.getResults();
        PrintStream s = null;

        RobocodeFileOutputStream writer = null;
        try {
            s = new PrintStream(new RobocodeFileOutputStream(getDataFile("score.txt")));
            writer = new RobocodeFileOutputStream(getDataFile("score.txt"));
            s.println("Score                "+result.getScore());;
            s.println("Survival             "+result.getSurvival());;
            s.println("Bullet Damage        "+result.getBulletDamage());;
            s.println("Bullet Damage Bonus  "+result.getBulletDamageBonus());;
            s.println("Ram Damage           "+result.getRamDamage());;
            s.println("Ram Damage Bonus     "+result.getRamDamageBonus());;
            s.close();
        } catch (IOException a) {
        } finally {
            try {
                if (writer != null)
                    writer.close( );
            } catch (IOException a) {

            }
        }
        PrintStream w = null;
        try {
            w = new PrintStream(new RobocodeFileOutputStream(getDataFile("win.txt")));
            for(int i = 0; i< round_counter.length; i++)
                w.println(round_counter[i]);
            w.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}