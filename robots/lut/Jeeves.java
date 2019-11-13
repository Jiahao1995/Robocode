package lut;

import robocode.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.PrintStream;

public class Jeeves extends AdvancedRobot {
    
    private static final double BULLET_HIT_REWARD = 9.0;
    private static final double BULLET_MISSED_REWARD = -1.0;
    private static final double HIT_BY_BULLET_REWARD = -5.0;
    private static final double HIT_ROBOT_REWARD = -6.0;
    private static final double HIT_WALL_REWARD = -10.0;
    private static final double WIN_REWARD = 100.0;
    private static final double DEATH_REWARD = -100.0;

    private static final double PI = Math.PI;
    private Target target;
    private LUT lut;
    private Learner learner;
    private double reward = 0.0;
    private double firePower;
    private int isHitWall = 0;
    private int isHitByBullet = 0;

    private double targetDistance;
    private double targetBearing;

    private int state;
    private int action;

    private boolean isFound = false;

    static private int numRounds = 0;
    static private int numWins = 0;

    private boolean interReward = true;

    public void run() {
        lut = new LUT();
        loadData();
        learner = new Learner(lut);
        target = new Target();
        target.distance = 100000;

        setColors(Color.green, Color.white, Color.green);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
//        turnRadarRightRadians(2 * PI);
        execute();

        while (true) {
            turnRadarRightRadians(2 * PI);
            state = getState();
            action = learner.selectAction(state);

            switch (action) {
                case Action.ROBOT_AHEAD:
                    setAhead(Action.ROBOT_MOVE_DISTANCE);
                    break;
                case Action.ROBOT_BACK:
                    setBack(Action.ROBOT_MOVE_DISTANCE);
                    break;
                case Action.ROBOT_AHEAD_TURN_LEFT:
                    setAhead(Action.ROBOT_MOVE_DISTANCE);
                    setTurnLeft(Action.ROBOT_TURN_DEGREE);
                    break;
                case Action.ROBOT_AHEAD_TURN_RIGHT:
                    setAhead(Action.ROBOT_MOVE_DISTANCE);
                    setTurnRight(Action.ROBOT_TURN_DEGREE);
                    break;
                case Action.ROBOT_BACK_TURN_LEFT:
                    setAhead(Action.ROBOT_MOVE_DISTANCE);
                    setTurnRight(Action.ROBOT_TURN_DEGREE);
                    break;
                case Action.ROBOT_BACK_TURN_RIGHT:
                    setAhead(target.bearing);
                    setTurnLeft(Action.ROBOT_TURN_DEGREE);
                    break;
                case Action.ROBOT_FIRE:
                    ahead(0);
                    turnLeft(0);
                    scanAndFire();
                    break;
                default:
                    break;
            }
            execute();
            turnRadarRightRadians(2 * PI);
            state = getState();
            learner.offPolicyLearn(state, action, reward);

            reward = 0.0;
            isHitWall = 0;
            isHitByBullet = 0;
        }

//        while (true) {
//            robotMove();
//            firePower = 400 / target.distance;
//            if (firePower > 3)
//                firePower = 3;
//            radarRotate();
//            gunRotate();
//            if (getGunHeat() == 0)
//                setFire(firePower);
//            execute();
//        }

    }

    private void robotMove() {
        int state = getState();
        int action = learner.selectAction(state);
        out.println("Action selected: " + action);
        learner.offPolicyLearn(state, action, reward);
        reward = 0.0;
        isHitWall = 0;
        isHitByBullet = 0;

        switch (action) {
            case Action.ROBOT_AHEAD:
                setAhead(Action.ROBOT_MOVE_DISTANCE);
                break;
            case Action.ROBOT_BACK:
                setBack(Action.ROBOT_MOVE_DISTANCE);
                break;
            case Action.ROBOT_AHEAD_TURN_LEFT:
                setAhead(Action.ROBOT_MOVE_DISTANCE);
                setTurnLeft(Action.ROBOT_TURN_DEGREE);
                break;
            case Action.ROBOT_AHEAD_TURN_RIGHT:
                setAhead(Action.ROBOT_MOVE_DISTANCE);
                setTurnRight(Action.ROBOT_TURN_DEGREE);
                break;
            case Action.ROBOT_BACK_TURN_LEFT:
                setAhead(Action.ROBOT_MOVE_DISTANCE);
                setTurnRight(Action.ROBOT_TURN_DEGREE);
                break;
            case Action.ROBOT_BACK_TURN_RIGHT:
                setAhead(target.bearing);
                setTurnLeft(Action.ROBOT_TURN_DEGREE);
                break;
            case Action.ROBOT_FIRE:
                ahead(0);
                turnLeft(0);
                break;
            default:
                break;
        }
    }

    private int getState() {
        int heading = State.getHeadingIndex(getHeading());
        int distance = State.getDistanceIndex(target.distance);
        int bearing = State.getBearingIndex(target.bearing);
        out.println("State(" + heading + ", " + distance + ", " + bearing + ", " + isHitWall + ", " + isHitByBullet + ")");
        return State.MAPPING[heading][distance][bearing][isHitWall][isHitByBullet];
    }

    private void scanAndFire() {
        isFound = false;
        while (!isFound) {
            setTurnRadarLeft(360);
            execute();
        }
        turnGunLeft(getGunHeading() - (getHeading() + targetBearing));
        if (targetDistance < 101)
            fire(6);
        else if (targetDistance < 201)
            fire(4);
        else if (targetDistance < 301)
            fire(2);
        else
            fire(1);
    }

    private void radarRotate() {
        double radarOffset;
        if (getTime() - target.time > 4) { 
            radarOffset = 4 * PI;
        } else {
            radarOffset = getRadarHeadingRadians() - (Math.PI / 2 - Math.atan2(target.y - getY(), target.x - getX()));
            radarOffset = normaliseBearing(radarOffset);
            if (radarOffset < 0)
                radarOffset -= PI / 10;
            else
                radarOffset += PI / 10;
        }
        setTurnRadarLeftRadians(radarOffset);
    }

//    private void gunRotate() {
//        long time;
//        long nextTime;
//        Point2D.Double p;
//        p = new Point2D.Double(target.x, target.y);
//        for (int i = 0; i < 20; i++) {
//            nextTime = (int) Math.round((getRange(getX(), getY(), p.x, p.y) / (20 - (3 * firePower))));
//            time = getTime() + nextTime - 10;
//            p = target.predictPosition(time);
//        }
//        double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(), p.x - getX()));
//        setTurnGunLeftRadians(normaliseBearing(gunOffset));
//    }

    private double normaliseBearing(double bearing) {
        if (bearing > PI)
            bearing -= 2 * PI;
        if (bearing < -PI)
            bearing += 2 * PI;
        return bearing;
    }

//    private double getRange(double x1, double y1, double x2, double y2) {
//        double deltaX = x2 - x1;
//        double deltaY = y2 - y1;
//        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
//    }

    public void onBulletHit(BulletHitEvent event) {
        if (target.name.equals(event.getName())) {
            if (interReward) {
                double change = event.getBullet().getPower() * BULLET_HIT_REWARD;
                out.println("Bullet Hit: " + change);
                reward += change;
            }
        }
    }

    public void onBulletMissed(BulletMissedEvent event) {
        if (interReward) {
            double change = event.getBullet().getPower() * BULLET_MISSED_REWARD;
            out.println("Bullet Missed: " + change);
            reward += change;
        }
    }

    public void onHitByBullet(HitByBulletEvent event) {
        if (target.name.equals(event.getName())) {
            if (interReward) {
                double change = event.getBullet().getPower() * HIT_BY_BULLET_REWARD;
                out.println("Hit By Bullet: " + change);
                reward += change;
            }
        }
        isHitByBullet = 1;
    }

    public void onHitRobot(HitRobotEvent event) {
        if (target.name.equals(event.getName())) {
            if (interReward) {
                double change = HIT_ROBOT_REWARD;
                out.println("Hit Robot: " + change);
                reward += change;
            }
        }
    }

    public void onHitWall(HitWallEvent event) {
        if (interReward) {
            double change = HIT_WALL_REWARD;
            out.println("Hit Wall: " + change);
            reward += change;
        }
        isHitWall = 1;
    }

    public void onScannedRobot(ScannedRobotEvent event) {
        isFound = true;
        targetDistance = event.getDistance();
        targetBearing = event.getBearing();
        if ((event.getDistance() < target.distance) || (target.name.equals(event.getName()))) {
            double bearing = (getHeadingRadians() + event.getBearingRadians()) % (2 * PI);
            double h = normaliseBearing(event.getHeadingRadians() - target.heading);
            target.name = event.getName();
            target.changeHeading = h / (getTime() - target.time);
            target.x = getX()+Math.sin(bearing)*event.getDistance();
            target.y = getY()+Math.cos(bearing)*event.getDistance();
            target.bearing = event.getBearingRadians();
            target.heading = event.getHeadingRadians();
            target.time = getTime();
            target.velocity = event.getVelocity();
            target.distance = event.getDistance();
            target.energy = event.getEnergy();
        }
    }

    public void onRobotDeath(RobotDeathEvent event) {
        if (target.name.equals(event.getName()))
            target.distance = 10000;
    }

    public void onWin(WinEvent event) {
        double change = WIN_REWARD;
        out.println("Win: " + change);
        reward += change;
        saveData();
        numWins++;
        numRounds++;

        PrintStream writer = null;
        try {
            writer = new PrintStream(new RobocodeFileOutputStream(getDataFile("record.txt").getAbsolutePath(), true));
            if (numRounds == 49) {
                double winRate = (double) numWins / 50;
                writer.println(numRounds + "\t" + winRate);
                numRounds = 0;
                numWins = 0;
                if (writer.checkError()) {
                    System.out.println("Could not save the data!");
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onDeath(DeathEvent event) {
        double change = DEATH_REWARD;
        out.println("Death: " + change);
        reward += change;
        saveData();
        numRounds++;

        PrintStream writer = null;
        try {
            writer = new PrintStream(new RobocodeFileOutputStream(getDataFile("record.txt").getAbsolutePath(), true));
            if (numRounds == 49) {
                double winRate = (double) numWins / 50;
                writer.println(numRounds + "\t" + winRate);
                numRounds = 0;
                numWins = 0;
                if (writer.checkError()) {
                    System.out.println("Could not save the data!");
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadData() {
        try {
            lut.loadData(getDataFile("movement.dat"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            lut.saveData(getDataFile("movement.dat"));
        } catch (Exception e) {
            out.println("Exception trying to write: " + e);
        }
    }

}
