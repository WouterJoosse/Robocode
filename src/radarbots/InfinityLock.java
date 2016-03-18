package radarbots;

import robocode.*;

import java.awt.*;

/***
 *  http://robowiki.net/wiki/One_on_One_Radar
 */
public class InfinityLock extends AdvancedRobot{


    public void run() {

        setRadarColor(Color.ORANGE);

        // This doesn't work if you put the setTurnRadar... in the
        // while (true) - loop...
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        execute();
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
    }
}
