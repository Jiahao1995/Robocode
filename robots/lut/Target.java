package lut;

import java.awt.geom.Point2D;

public class Target {

    String name;
    double bearing;
    double heading;
    long time;
    double velocity;
    double x, y;
    double distance;
    double changeHeading;
    double energy;

    public Point2D.Double predictPosition(long when) {
        double deltaTime = when - time;
        double newY, newX;
        if (Math.abs(changeHeading) > 0.00001) {
            double radius = velocity / changeHeading;
            double deltaHeading = deltaTime * changeHeading;
            newY = y + (Math.sin(heading + deltaHeading) * radius) - (Math.sin(heading) * radius);
            newX = x + (Math.cos(heading) * radius) - (Math.cos(heading + deltaHeading) * radius);
        } else {
            newY = y + Math.cos(heading) * velocity * deltaTime;
            newX = x + Math.sin(heading) * velocity * deltaTime;
        }
        return new Point2D.Double(newX, newY);
    }

}
