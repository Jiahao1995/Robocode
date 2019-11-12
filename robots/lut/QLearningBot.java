package lut;

import java.awt.*;
import java.awt.geom.*;
import robocode.*;

import robocode.AdvancedRobot;

public class QLearningBot extends AdvancedRobot
{
  public static final double PI = Math.PI;
  private Target target;
  private QTable table;
  private Learner learner;
  private double reinforcement = 0.0;
  private double firePower;
  private int direction = 1;
  private int isHitWall = 0;
  private int isHitByBullet = 0;

  public void run()
  {
    table = new QTable();
    loadData();
    learner = new Learner(table);
    target = new Target();
    target.distance = 100000;

    setColors(Color.green, Color.white, Color.green);
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    turnRadarRightRadians(2 * PI);
    while (true)
    {
      robotMovement();
      firePower = 400/target.distance;
      if (firePower > 3)
        firePower = 3;
      radarMovement();
      gunMovement();
      if (getGunHeat() == 0) {
        setFire(firePower);
      }
      execute();
    }
  }

  void doMovement()
  {
    if (getTime()%20 == 0)
    {
      direction *= -1;		//reverse direction
      setAhead(direction*300);	//move in that direction
    }
    setTurnRightRadians(target.bearing + (PI/2)); //every turn move to circle strafe the enemy
  }

  private void robotMovement()
  {
    int state = getState();
    int action = learner.selectAction(state, getTime());
    out.println("Action selected: " + action);
    learner.learn(state, action, reinforcement);
    reinforcement = 0.0;
    isHitWall = 0;
    isHitByBullet = 0;

    switch (action)
    {
      case Action.RobotAhead:
        setAhead(Action.RobotMoveDistance);
        break;
      case Action.RobotBack:
        setBack(Action.RobotMoveDistance);
        break;
      case Action.RobotAheadTurnLeft:
        setAhead(Action.RobotMoveDistance);
        setTurnLeft(Action.RobotTurnDegree);
        //setTurnLeft(180 - (target.bearing + 90 - 30));
        break;
      case Action.RobotAheadTurnRight:
        setAhead(Action.RobotMoveDistance);
        setTurnRight(Action.RobotTurnDegree);
        //setTurnRight(target.bearing + 90 - 30);
        break;
      case Action.RobotBackTurnLeft:
        setAhead(Action.RobotMoveDistance);
        setTurnRight(Action.RobotTurnDegree);
        //setTurnRight(target.bearing + 90 - 30);
        break;
      case Action.RobotBackTurnRight:
        setAhead(target.bearing);
        setTurnLeft(Action.RobotTurnDegree);
        //setTurnLeft(180 - (target.bearing + 90 - 30));
        break;
    }
  }

  private int getState()
  {
    int heading = State.getHeading(getHeading());
    int targetDistance = State.getTargetDistance(target.distance);
    int targetBearing = State.getTargetBearing(target.bearing);
    out.println("Stste(" + heading + ", " + targetDistance + ", " + targetBearing + ", " + isHitWall + ", " + isHitByBullet + ")");
    int state = State.Mapping[heading][targetDistance][targetBearing][isHitWall][isHitByBullet];
    return state;
  }

  private void radarMovement()
  {
    double radarOffset;
    if (getTime() - target.ctime > 4) { //if we haven't seen anybody for a bit....
      radarOffset = 4*PI;				//rotate the radar to find a target
    } else {

      //next is the amount we need to rotate the radar by to scan where the target is now
      radarOffset = getRadarHeadingRadians() - (Math.PI/2 - Math.atan2(target.y - getY(),target.x - getX()));
      //this adds or subtracts small amounts from the bearing for the radar to produce the wobbling
      //and make sure we don't lose the target
      radarOffset = NormaliseBearing(radarOffset);
      if (radarOffset < 0)
        radarOffset -= PI/10;
      else
        radarOffset += PI/10;
    }
    //turn the radar
    setTurnRadarLeftRadians(radarOffset);
  }

  private void gunMovement()
  {
    long time;
    long nextTime;
    Point2D.Double p;
    p = new Point2D.Double(target.x, target.y);
    for (int i = 0; i < 20; i++)
    {
      nextTime = (int)Math.round((getrange(getX(),getY(),p.x,p.y)/(20-(3*firePower))));
      time = getTime() + nextTime - 10;
      p = target.guessPosition(time);
    }
    //offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class
    double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(),p.x -  getX()));
    setTurnGunLeftRadians(NormaliseBearing(gunOffset));
  }

  //bearing is within the -pi to pi range
  double NormaliseBearing(double ang) {
    if (ang > PI)
      ang -= 2*PI;
    if (ang < -PI)
      ang += 2*PI;
    return ang;
  }

  //heading within the 0 to 2pi range
  double NormaliseHeading(double ang) {
    if (ang > 2*PI)
      ang -= 2*PI;
    if (ang < 0)
      ang += 2*PI;
    return ang;
  }

  //returns the distance between two x,y coordinates
  public double getrange( double x1,double y1, double x2,double y2 )
  {
    double xo = x2-x1;
    double yo = y2-y1;
    double h = Math.sqrt( xo*xo + yo*yo );
    return h;
  }

  //gets the absolute bearing between to x,y coordinates
  public double absbearing( double x1,double y1, double x2,double y2 )
  {
    double xo = x2-x1;
    double yo = y2-y1;
    double h = getrange( x1,y1, x2,y2 );
    if( xo > 0 && yo > 0 )
    {
      return Math.asin( xo / h );
    }
    if( xo > 0 && yo < 0 )
    {
      return Math.PI - Math.asin( xo / h );
    }
    if( xo < 0 && yo < 0 )
    {
      return Math.PI + Math.asin( -xo / h );
    }
    if( xo < 0 && yo > 0 )
    {
      return 2.0*Math.PI - Math.asin( -xo / h );
    }
    return 0;
  }

  public void onBulletHit(BulletHitEvent e)
  {
    if (target.name == e.getName())
    {
      //double power = e.getBullet().getPower();
      //double change = 4 * power + 2 * (power - 1);
      double change = e.getBullet().getPower() * 9;
      out.println("Bullet Hit: " + change);
      reinforcement += change;
    }
  }

  public void onBulletHitBullet(BulletHitBulletEvent e)
  {
    //
  }

  public void onBulletMissed(BulletMissedEvent e)
  {
    double change = -e.getBullet().getPower();
    out.println("Bullet Missed: " + change);
    reinforcement += change;
  }

  public void onHitByBullet(HitByBulletEvent e)
  {
    if (target.name == e.getName())
    {
      double power = e.getBullet().getPower();
      double change = -(4 * power + 2 * (power - 1));
      out.println("Hit By Bullet: " + change);
      reinforcement += change;
    }
    isHitByBullet = 1;
  }

  public void onHitRobot(HitRobotEvent e)
  {
    if (target.name == e.getName())
    {
      double change = -6.0;
      out.println("Hit Robot: " + change);
      reinforcement += change;
    }
  }

  public void onHitWall(HitWallEvent e)
  {
    
    double change = -(Math.abs(getVelocity()) * 0.5 - 1);
    out.println("Hit Wall: " + change);
    reinforcement += change;
    isHitWall = 1;
  }

  /**
   * onScannedRobot: What to do when you see another robot
   */
  public void onScannedRobot(ScannedRobotEvent e)
  {
    if ((e.getDistance() < target.distance)||(target.name == e.getName()))
    {
      //the next line gets the absolute bearing to the point where the bot is
      double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*PI);
      //this section sets all the information about our target
      target.name = e.getName();
      double h = NormaliseBearing(e.getHeadingRadians() - target.head);
      h = h/(getTime() - target.ctime);
      target.changehead = h;
      target.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the target is
      target.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the target is
      target.bearing = e.getBearingRadians();
      target.head = e.getHeadingRadians();
      target.ctime = getTime();				//game time at which this scan was produced
      target.speed = e.getVelocity();
      target.distance = e.getDistance();
      target.energy = e.getEnergy();
    }
  }

  public void onRobotDeath(RobotDeathEvent e)
  {

    if (e.getName() == target.name)
      target.distance = 10000;
  }

  public void onWin(WinEvent event)
  {
    saveData();
  }

  public void onDeath(DeathEvent event)
  {
    saveData();
  }

  public void loadData()
  {
    try
    {
      table.loadData(getDataFile("movement.dat"));
    }
    catch (Exception e)
    {
    }
  }

  public void saveData()
  {
    try
    {
      table.saveData(getDataFile("movement.dat"));
    }
    catch (Exception e)
    {
      out.println("Exception trying to write: " + e);
    }
  }
}