package aiBots;

import robocode.*;
import robocode.control.BattlefieldSpecification;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Stack;

public class MyFirstBot extends AdvancedRobot {

    public Stack<String> remainingRobotsNames = new Stack<String>();
    public double battleFieldWidth, battleFieldHeight;

    public void run() {

        battleFieldWidth = getBattleFieldWidth();
        battleFieldHeight = getBattleFieldHeight();

        while (true) {

            findObstacles();
            ahead(100);

        }
    }
 
    public void onScannedRobot(ScannedRobotEvent e) {

        System.out.println(e.getName());
        remainingRobotsNames.push(e.getName());

    }

    public void findObstacles() {

        int rayLength = 25;
        double rayArc = 45;

        setTurnRadarLeft(360);
        execute();

        if (wallInPath(rayLength,rayArc)) {
            adjustCourseParallelToWall();
        }

    }

    private void adjustCourseParallelToWall() {

    }

    private boolean wallInPath(int rayLength, double rayArc) {

        Point2D robotPosition = new Point2D.Double(getX(), getY());
        double heading = getHeading();

        double leftArc = heading + (rayArc / 2);
        double rightArc = heading - (rayArc / 2);

        Point leftRayEnd = new Point();
        Point rigthRayEnd = new Point();

        return false;
        
    }

}
