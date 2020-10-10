import java.awt.geom.Point2D;

class Target {
    String name;
    public double bearing;
    public double head;
    public long ctime;
    public double speed;
    public double x, y;
    public double distance;
    public double changehead;
    public double energy;

    /* anti-gravity and circular movement */
    public Point2D.Double targetEnemy(long when) {
        double diff = when - ctime;
        double newY, newX;

        /* if the change in heading is significant, use circular targeting */
        //if (Math.abs(update_head) > 0.00001) {
        if (false) {
            double radius = speed/changehead;
            double tothead = diff * changehead;
            newY = y + (Math.sin(head + tothead) * radius) - (Math.sin(head) * radius);
            newX = x + (Math.cos(head) * radius) - (Math.cos(head + tothead) * radius);
        }
        /*If the change in heading is insignificant, use linear */
        // comment if statement for simple targeting...
        else {
            newY = y + Math.cos(head) * speed * diff;
            newX = x + Math.sin(head) * speed * diff;
        }
        return new Point2D.Double(newX, newY);
    }
}