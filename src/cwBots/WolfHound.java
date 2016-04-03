package cwBots;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.*;

/**
 * This bot will try to chase the opponent and make save shots. It measures the energy
 * of the opponent, and will try to dodge bullets as much as possible,
 * and it will wait for the opportunity to strike.
 * This will happen when the energy of the opponent is low,
 *
 * Created by Wouter.
 */
//TODO: define low energylevel
public class WolfHound extends AdvancedRobot {

    public void run() {

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        execute();

        while (true) {
            //TODO: implement movement strategy -- Maybe also in onScannedRobot
            scan();
        }

    }

    public void onScannedRobot(ScannedRobotEvent e) {

        //TODO: update global variables for tracking the enemy


        double absoluteEnemyHeading = getHeadingRadians() + e.getBearingRadians();
        double radarTurn = Utils.normalRelativeAngle(absoluteEnemyHeading - getRadarHeadingRadians());
        double gunTurn = Utils.normalRelativeAngle(absoluteEnemyHeading - getGunHeadingRadians());

        double radarFactor = 1.5;
        setTurnRadarRightRadians(radarFactor * radarTurn);
        setTurnGunRightRadians(gunTurn);



        //TODO: update movement strategy

        //TODO: classify the power of the bullet with linear classification, or neural network
        // The learning will take place after death or when the round ends.



    }

    //TODO: add painting for target position prediction
    public void onPaint(Graphics2D g) {



    }
}
