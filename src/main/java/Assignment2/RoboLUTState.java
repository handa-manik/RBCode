package Assignment2;

public class RoboLUTState {
    public static final int NUMBER_OF_HEADINGS = 4; //includes 4 headings
    public static final int NUMBER_TARGET_DIST = 3; //includes 3 distances
    public static final int NUMBER_TARGET_BEARING = 4; //includes 4 bearings
    public static final int NUMBER_OF_STATES;
    public static final int IS_HITWALL = 2;
    public static final int IS_HITBYBULLET = 2;
    public static final int ENERGY_LEVELS = 3;
    public static final double TOTAL_ANGLE = 360.0;
    public static final double NORMALIZE_FACTOR = 150.0;
    public static final int STATE_TABLE[][][][][][];
    public static final boolean SMART_CODE = true;
    public static final boolean NORMALIZED = true;

    static {
        System.out.println("About to generate data in the State Table!!!");
        STATE_TABLE = new int[NUMBER_OF_HEADINGS][NUMBER_TARGET_DIST][NUMBER_TARGET_BEARING][IS_HITWALL][IS_HITBYBULLET][ENERGY_LEVELS];

        int count = 0;
        for (int i = 0; i < NUMBER_OF_HEADINGS; i++)
            for (int j = 0; j < NUMBER_TARGET_DIST; j++)
                for (int k = 0; k < NUMBER_TARGET_BEARING; k++)
                    for (int l = 0; l < IS_HITWALL; l++)
                        for (int m = 0; m < IS_HITBYBULLET; m++)
                            for (int n = 0; n < ENERGY_LEVELS; n++)
                                STATE_TABLE[i][j][k][l][m][n] = count++;

        NUMBER_OF_STATES = count;
    }

    public static int getEnergy(double energy) {
        int new_Energy;
        if (energy < 0 )
            new_Energy = 0;
        if (energy < 40.0)
            new_Energy = 0;
        else if (energy >= 40.0 && energy < 80.0)
            new_Energy = 1;
        else
            new_Energy = 2;
        return new_Energy;
    }

    public static int getHeading(double heading) {
        if (SMART_CODE) {
            double angle = TOTAL_ANGLE / NUMBER_OF_HEADINGS;
            double newHeading = heading + angle / 2;
            if (newHeading > TOTAL_ANGLE)
                newHeading -= TOTAL_ANGLE;
            return (int)(newHeading / angle);
        } else {
            int newHeading;
            if (heading < 90.0)
                newHeading = 0;
            else if (heading >= 90.0 && heading < 180.0)
                newHeading = 1;
            else if (heading >= 180.0 && heading < 270.0)
                newHeading = 2;
            else if (heading >= 270.0 && heading < 360.0)
                newHeading = 3;
            else
                newHeading = 0;
            return newHeading;
        }

    }

    public static int getTargetDistance(double value) {
        int distance;
        if (SMART_CODE) {
            distance = (int)(value / NORMALIZE_FACTOR);
            if (distance > NUMBER_TARGET_DIST - 1)
                distance = NUMBER_TARGET_DIST - 1;
        } else {
            if (value < 0)
                distance = 0;

            if (value <= 200)
                distance = 0; //0~200 pixels distance
            else if (value > 200 && value <= 600)
                distance = 1; // 200~600 pixels distance
            else
                distance = 2;
        }
        return distance;
    }

    public static int getTargetBearing(double bearing) {
        double twoPI = Math.PI * 2;
        if (bearing < 0.0)
            bearing = twoPI + bearing;
        double rad = twoPI / NUMBER_TARGET_BEARING;
        double newBearing = bearing + rad / 2;
        if (newBearing > twoPI)
            newBearing -= twoPI;
        return (int)(newBearing / rad);
    }
}
