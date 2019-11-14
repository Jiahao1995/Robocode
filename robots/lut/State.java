package lut;

public class State {

    private static final int NUM_HEADING = 4;
    private static final int NUM_DISTANCE = 20;
    private static final int NUM_BEARING = 4;
    private static final int NUM_HIT_WALL = 2;
    private static final int NUM_HIT_BY_BULLET = 2;
    public static final int NUM_ROBOT_STATES;
    public static final int[][][][][] MAPPING;

    static {
        MAPPING = new int[NUM_HEADING][NUM_DISTANCE][NUM_BEARING][NUM_HIT_WALL][NUM_HIT_BY_BULLET];
        int index = 0;
        for (int a = 0; a < NUM_HEADING; a++) {
            for (int b = 0; b < NUM_DISTANCE; b++) {
                for (int c = 0; c < NUM_BEARING; c++) {
                    for (int d = 0; d < NUM_HIT_WALL; d++) {
                        for (int e = 0; e < NUM_HIT_BY_BULLET; e++)
                            MAPPING[a][b][c][d][e] = index++;
                    }
                }
            }
        }
        NUM_ROBOT_STATES = index;
    }

    public static int getHeadingIndex(double heading) {
        double angle = 360.0 / NUM_HEADING;
        double newHeading = heading + angle / 2;
        if (newHeading > 360.0)
            newHeading -= 360.0;
        return (int) (newHeading / angle);
    }

    public static int getDistanceIndex(double distance) {
        int index = (int) (distance / 30.0);
        if (index > NUM_DISTANCE - 1)
            index = NUM_DISTANCE - 1;
        return index;
    }

    public static int getBearingIndex(double bearing) {
        double PIx2 = Math.PI * 2;
        if (bearing < 0)
            bearing = PIx2 + bearing;
        double angle = PIx2 / NUM_BEARING;
        double newBearing = bearing + angle / 2;
        if (newBearing > PIx2)
            newBearing -= PIx2;
        return (int) (newBearing / angle);
    }

}
