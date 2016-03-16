package targetingbots;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/***
 *  http://robowiki.net/wiki/One_on_One_Radar
 */
public class TurnMulitplierLock extends AdvancedRobot{


    public void run() {

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        setTurnGunRightRadians(Double.POSITIVE_INFINITY);
        execute();


        while (true) {
            //check for new targets.
            // Only necessary for Narrow Lock because sometimes our radar is already
            // pointed at the enemy and our onScannedRobot code doesn't end up telling
            // it to turn, so the system doesn't automatically call scan() for  us
            // [see the javadocs for scan()].
            scan();
            setTurnRightRadians(Double.POSITIVE_INFINITY);
            setAhead(10);
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Absolute bearing to target - current radar heading
        double radarTurn =
                getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();

        double gunTurn =
                getHeadingRadians() + e.getBearingRadians() - getGunHeadingRadians();

        setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
        setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
        setFire((Math.min(getBattleFieldHeight(),getBattleFieldWidth()) - e.getDistance())
                % Math.min(getBattleFieldHeight(),getBattleFieldWidth()));

    }

}
