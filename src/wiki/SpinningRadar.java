package wiki;

import robocode.*;

/***
 *  http://robowiki.net/wiki/One_on_One_Radar
 */
public class SpinningRadar extends AdvancedRobot{

    public void run() {

        while(true) {
            setTurnRadarRight(Double.POSITIVE_INFINITY);
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {

    }
}
