package lut;

import java.awt.geom.*;

class Target
{
  String name;
  public double bearing;
  public double head;
  public long ctime;
  public double speed;
  public double x, y;
  public double distance;
  public double changehead;
  public double energy;

  public Point2D.Double guessPosition(long when)
  {
    double diff = when - ctime;
    double newY, newX;

    /**if the change in heading is significant, use circular targeting**/
    if (Math.abs(changehead) > 0.00001)
    {
      double radius = speed/changehead;
      double tothead = diff * changehead;
      newY = y + (Math.sin(head + tothead) * radius) - (Math.sin(head) * radius);
      newX = x + (Math.cos(head) * radius) - (Math.cos(head + tothead) * radius);
    }
    /**If the change in heading is insignificant, use linear**/
    else {
      newY = y + Math.cos(head) * speed * diff;
      newX = x + Math.sin(head) * speed * diff;
    }
    return new Point2D.Double(newX, newY);
  }

  public double guessX(long when)
  {
    long diff = when - ctime;
    System.out.println(diff);
    return x+Math.sin(head)*speed*diff;
  }
  public double guessY(long when)
  {
    long diff = when - ctime;
    return y+Math.cos(head)*speed*diff;
  }
}