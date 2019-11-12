package lut;

public class State
{
  public static final int NumHeading = 4;
  public static final int NumTargetDistance = 20;
  public static final int NumTargetBearing = 4;
  public static final int NumHitWall = 2;
  public static final int NumHitByBullet = 2;
  public static final int NumStates;
  public static final int Mapping[][][][][];

  static
  {
    Mapping = new int[NumHeading][NumTargetDistance][NumTargetBearing][NumHitWall][NumHitByBullet];
    int count = 0;
    for (int a = 0; a < NumHeading; a++)
      for (int b = 0; b < NumTargetDistance; b++)
        for (int c = 0; c < NumTargetBearing; c++)
          for (int d = 0; d < NumHitWall; d++)
            for (int e = 0; e < NumHitByBullet; e++)
          Mapping[a][b][c][d][e] = count++;

    NumStates = count;
  }

  public static int getHeading(double heading)
  {
    double angle = 360 / NumHeading;
    double newHeading = heading + angle / 2;
    if (newHeading > 360.0)
      newHeading -= 360.0;
    return (int)(newHeading / angle);
  }

  public static int getTargetDistance(double value)
  {
    int distance = (int)(value / 30.0);
    if (distance > NumTargetDistance - 1)
      distance = NumTargetDistance - 1;
    return distance;
  }

  public static int getTargetBearing(double bearing)
  {
    double PIx2 = Math.PI * 2;
    if (bearing < 0)
      bearing = PIx2 + bearing;
    double angle = PIx2 / NumTargetBearing;
    double newBearing = bearing + angle / 2;
    if (newBearing > PIx2)
      newBearing -= PIx2;
    return (int)(newBearing / angle);
  }
  
}