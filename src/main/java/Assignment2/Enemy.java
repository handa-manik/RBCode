package Assignment2;

import java.awt.geom.Point2D;

public class Enemy {
    String name;
    public double bearing;
    public double head;
    public double x, y;
    public long ctime;
    public double speed;
    public double distance;
    public double update_head;
    public double energy;

    public Point2D.Double targetEnemy(long when) {
        double diff = when - ctime;
        double newY, newX;

        if (false) {
            double radius = speed/ update_head;
            double tothead = diff * update_head;
            newY = y + (Math.sin(head + tothead) * radius) - (Math.sin(head) * radius);
            newX = x + (Math.cos(head) * radius) - (Math.cos(head + tothead) * radius);
        }
        else {
            newY = y + Math.cos(head) * speed * diff;
            newX = x + Math.sin(head) * speed * diff;
        }
        return new Point2D.Double(newX, newY);
    }
}