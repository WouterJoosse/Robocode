package radarbots;

import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * http://robowiki.net/wiki/One_on_One_Radar
 */
public class WidthLock extends AdvancedRobot{

    public void run() {

        while (true) {
            if (getRadarTurnRemaining() == 0.0) {
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            }
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        // absolute angle towards target
        double angleToEnemy = getHeadingRadians() + e.getBearingRadians();

        // Substract current radar heading to get the turn required to face the enemy, be sure to normalize it.

        double radarTurnUnnormalized = angleToEnemy - getRadarHeadingRadians();
        System.out.println(radarTurnUnnormalized);

        double radarTurn = Utils.normalRelativeAngle(radarTurnUnnormalized);

        System.out.println(radarTurn);
        // the distance we want to scan from the middle of enemey to either side
        // the 36.0 is how many units from the center of the robot it scans
        double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);

        // Adjust the radar turn so it goes that much further in the direction it is going to turn
        // Basically if we were going to turn it left, turn it even more left, if right, turn more right.
        // This allows us to overshoot our enemy so we get a good sweep that will not slip.
        radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);

        // Turn the radar
        setTurnRadarRightRadians(radarTurn);
    }
}
