package aiBots;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

import java.awt.*;
import java.awt.geom.Point2D;

public class JoostemBot extends TeamRobot {

    private final int RAYARC = 45;

    private final double NORTH = 0;
    private final double EAST = Math.PI / 2;
    private final double SOUTH = Math.PI;
    private final double WEST = 3 * Math.PI / 2;

    public double battleFieldWidth, battleFieldHeight,
            currentHeading, leftAngle, rightAngle,
            rayLength, cornerHeight, cornerWidth;
    public Point2D currentPosition, mainRayPos, leftRayPos, rightRayPos;

    public void run() {

        battleFieldWidth = getBattleFieldWidth();
        battleFieldHeight = getBattleFieldHeight();
        rayLength = Math.max(75,getBattleFieldWidth()*0.10);
        cornerHeight = Math.max(80, battleFieldHeight * 0.20);
        cornerWidth = Math.max(80, 1.5 * battleFieldWidth * 0.20);

        while (true) {

            currentPosition = new Point2D.Double(getX(),getY());
            currentHeading = getHeadingRadians();

            // Must be radians...
            leftAngle = currentHeading - Math.toRadians(RAYARC);
            rightAngle = currentHeading + Math.toRadians(RAYARC);

            mainRayPos = calculatePosFromRobot(currentHeading, rayLength);
            leftRayPos = calculatePosFromRobot(leftAngle, rayLength);
            rightRayPos = calculatePosFromRobot(rightAngle, rayLength);

            avoidWalls();

            ahead(10);

        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        System.out.println(e.getName());

    }

    /** Calculates the end position of an object given the angle and the distance
     * between the robot and the object
     * @param angle The angle between the robot and the object (bearing)
     * @param distance The distance between the robot and the object
     * @return the position of the object
     */
    private Point2D calculatePosFromRobot(double angle, double distance) {

        double x = currentPosition.getX() + (Math.sin(angle) * distance);
        double y = currentPosition.getY() + (Math.cos(angle) * distance);

        return new Point2D.Double(x,y);
    }

    /**
     * Obstacle avoidance method
     */
    private void avoidWalls() {

        double newHeading = currentHeading;

        if (mainRayPos.getX() <= 0 ||
                leftRayPos.getX() <= 0 ||
                rightRayPos.getX() >= battleFieldWidth)
            newHeading = NORTH;
        else if (mainRayPos.getX() >= battleFieldWidth ||
                leftRayPos.getX() >= battleFieldWidth ||
                rightRayPos.getX() <= 0)
            newHeading = SOUTH;
        else if (mainRayPos.getY() <= 0 ||
                leftRayPos.getY() >= battleFieldHeight ||
                rightRayPos.getY() <= 0)
            newHeading = EAST;
        else if (mainRayPos.getY() >= battleFieldHeight ||
                leftRayPos.getY() <= 0 ||
                rightRayPos.getY() >= battleFieldHeight)
            newHeading = WEST;

        if (newHeading == NORTH && currentPosition.getY() > battleFieldHeight - cornerHeight)
            newHeading = SOUTH;
        else if (newHeading == SOUTH && currentPosition.getY() < cornerHeight)
            newHeading = NORTH;

        if (newHeading == WEST && currentPosition.getX() < cornerWidth )
            newHeading = EAST;
        else if (newHeading == EAST && currentPosition.getX() > battleFieldWidth - cornerWidth)
            newHeading = WEST;

        adjustBodyTowardsRadians(newHeading);

    }

    // Not used yet
    public void findObstacles() {

    }

    private void adjustCourseParallelToWall() {

        adjustBodyTowardsRadians(Math.PI * 0.75);
        execute();

    }


    @Override
    /** Draws graphical debugging information on the battlefield
     */

    public void onPaint(Graphics2D g) {

        // draw whiskers
        drawRay(Color.GREEN,g,mainRayPos);
        drawRay(Color.YELLOW,g,leftRayPos);
        drawRay(Color.RED,g,rightRayPos);

        g.setColor(Color.RED);
        g.drawRect(0,0,(int)cornerWidth,(int)cornerHeight);
        g.drawRect((int) (battleFieldWidth - cornerWidth) +1 ,0,(int)cornerWidth,(int)cornerHeight);
        g.drawRect(0,(int) (battleFieldHeight - cornerHeight) +1 ,(int)cornerWidth,(int)cornerHeight);
        g.drawRect((int) (battleFieldWidth - cornerWidth) +1 ,(int) (battleFieldHeight - cornerHeight) +1 ,
                (int)cornerWidth,(int)cornerHeight);


    }

    /** Draws the line between the robot and a certain position.
     * @param color The color of the ray
     * @param g     The paint object
     * @param ray   The position of the end-point of the line.
     */
    public void drawRay(Color color, Graphics2D g, Point2D ray) {

        g.setColor(color);
        g.drawLine((int)ray.getX(), (int)ray.getY(),
                (int)currentPosition.getX(), (int)currentPosition.getY());
    }

    /***
     * Adjusts the body towards a certain heading (in range [0,2*Math.PI]
     * @param goal - The new heading.
     */
    private void adjustBodyTowardsRadians(double goal) {

        double adjustment = currentHeading - goal;

        if (adjustment - Math.PI < 0) {
            setTurnLeftRadians(adjustment);
        } else {
            setTurnRightRadians((2*Math.PI)-adjustment);
        }
    }

}
