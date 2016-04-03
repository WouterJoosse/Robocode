package wiki;

import helper.EnemyWave;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by Wouter on 4/3/2016.
 */
public class BasicSurfing extends AdvancedRobot {

    public static int BINS = 47;
    public static double[] _surfStats = new double[BINS];
    public Point2D.Double _myLocation;
    public Point2D.Double _enemyLocation;

    public ArrayList<EnemyWave> _enemyWaves;
    public ArrayList<Integer> _surfDirections;
    public ArrayList<Double> _surfAbsBearings;

    public static double _oppEnergy = 100.0;

    public static Rectangle2D.Double _fieldRect;
    public static double WALL_STICK = 160;

    public void run() {

        _enemyWaves = new ArrayList<>();
        _surfDirections = new ArrayList<>();
        _surfAbsBearings = new ArrayList<>();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);


        while (true) {

            turnRadarRightRadians(Double.POSITIVE_INFINITY);

        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        _myLocation = new Point2D.Double(getX(), getY());

        double lateralVelocity = getVelocity() * Math.sin(e.getBearingRadians());
        double absBearing = e.getBearingRadians() + getHeadingRadians();

        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()));

        _surfDirections.add(0,(lateralVelocity >= 0) ? 1 : -1);
        _surfAbsBearings.add(0,absBearing + Math.PI);

        double enemyBulletPower = _oppEnergy - e.getEnergy();
        if (enemyBulletPower < 3.01 && enemyBulletPower > 0.09
                && _surfDirections.size() > 2) {
            EnemyWave ew = new EnemyWave();
            ew.setFireTime(getTime() - 1);
            ew.setBulletVelocity(bulletVelocity(enemyBulletPower));
            ew.setDistanceTraveled(bulletVelocity(enemyBulletPower));
            ew.setDirection(_surfDirections.get(2));
            ew.setFireLocation((Point2D.Double) _enemyLocation.clone()); // last tick

            _enemyWaves.add(ew);
        }

        _oppEnergy = e.getEnergy();

        _enemyLocation = project(_myLocation, absBearing, e.getDistance());

        updateWaves();
        doSurfing();
    }

    public void updateWaves() {
        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = _enemyWaves.get(x);

            ew.setDistanceTraveled((getTime() - ew.getFireTime()) * ew.getBulletVelocity());
            if (ew.getDistanceTraveled() >
                    _myLocation.distance(ew.getFireLocation()) + 50) {
                _enemyWaves.remove(x);
                x--;
            }
        }
    }

    public EnemyWave getClosestSurfableWave() {

        double closesDistance = Double.MAX_VALUE;
        EnemyWave surfWave = null;

        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = _enemyWaves.get(x);
            double distance = _myLocation.distance(ew.getFireLocation())
                    - ew.getDistanceTraveled();

            if (distance > ew.getBulletVelocity() && distance < closesDistance) {
                surfWave = ew;
                closesDistance = distance;
            }

        }

        return surfWave;
    }

    public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
        double offsetAngle = (absoluteBearing(ew.getFireLocation(), targetLocation)
            - ew.getDirectAngle());

        double factor = Utils.normalRelativeAngle(offsetAngle)
                / maxEscapeAngle(ew.getBulletVelocity() * ew.getDirection());

        return (int) limit(0,
                (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2),
                BINS - 1);
    }

    public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
        int index = getFactorIndex(ew, targetLocation);

        for (int x = 0; x < BINS; x++) {
            _surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        // If the _enemyWaves collection is empty, we must have mised the
        // detection of this wave somehow.
        //TODO: implement learning algorithm for bullet detection

        if (!_enemyWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(
                    e.getBullet().getX(), e.getBullet().getY());
            EnemyWave hitWave = null;

            for (int x = 0; x < _enemyWaves.size(); x++) {
                EnemyWave ew = _enemyWaves.get(x);

                if (Math.abs(ew.getDistanceTraveled() -
                        _myLocation.distance(ew.getFireLocation())) < 50
                        && Math.abs(bulletVelocity(e.getBullet().getPower()) -
                            ew.getBulletVelocity()) < 0.001) {
                    hitWave = ew;
                    break;
                }
            }

            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);
                _enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
            }
        }
    }

    public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {

        Point2D.Double predictedPosition = (Point2D.Double) _myLocation.clone();
        double predictedVelocity = getVelocity();
        double predictedHeading = getHeadingRadians();
        double maxTurning, moveAngle, moveDir;

        int counter = 0;
        boolean intercepted = false;

        do {
            moveAngle =
                    wallSmoothing(predictedPosition, absoluteBearing(surfWave.getFireLocation(),
                            predictedPosition) + (direction * (Math.PI / 2)), direction) -
                            predictedHeading;
            moveDir = 1;

            if (Math.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            maxTurning = Math.PI /720d * (40d - 3d * Math.abs(predictedVelocity));
            predictedHeading = Utils.normalRelativeAngle(predictedHeading +
                limit(-maxTurning, moveAngle, maxTurning));

            predictedVelocity += (predictedVelocity * moveDir < 0 ? 2 * moveDir : moveDir);
            predictedVelocity = limit(-8,predictedVelocity, 8);

            predictedPosition = project(predictedPosition, predictedHeading, predictedVelocity);

            counter++;

            if (predictedPosition.distance(surfWave.getFireLocation()) <
                surfWave.getDistanceTraveled() + (counter * surfWave.getBulletVelocity()) +
                        surfWave.getBulletVelocity()) {
                intercepted = true;
            }
        } while (!intercepted && counter < 500);

        return predictedPosition;
    }

    public double checkDanger(EnemyWave surfWave, int direction) {
        int index = getFactorIndex(surfWave,
                predictPosition(surfWave, direction));

        return _surfStats[index];
    }

    public void doSurfing() {
        EnemyWave surfWave = getClosestSurfableWave();

        if (surfWave == null) {return;}

        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        double goAngle = absoluteBearing(surfWave.getFireLocation(), _myLocation);
        if (dangerLeft < dangerRight) {
            goAngle = wallSmoothing(_myLocation, goAngle - (Math.PI / 2), -1);
        } else {
            goAngle = wallSmoothing(_myLocation, goAngle + (Math.PI / 2), 1);
        }

        setBackAsFront(this, goAngle);
    }

    public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while(!_fieldRect.contains(project(botLocation, angle, 160))) {
            angle += orientation * 0.05;
        }
        return angle;
    }

    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
                sourceLocation.y * Math.cos(angle) * length);
    }

    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static double bulletVelocity(double power) {
        return (20D - (3D * power));
    }

    public static double maxEscapeAngle(double velocity) {
        return Math.asin(8.0 / velocity);
    }

    public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
        double angle =
                Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
        if (Math.abs(angle) > (Math.PI / 2)) {
            if (angle < 0) {
                robot.setTurnRightRadians(Math.PI + angle);
            } else {
                robot.setTurnLeftRadians(Math.PI - angle);
            }
            robot.setBack(100);
        } else {
            if (angle < 0) {
                robot.setTurnLeftRadians(-1*angle);
            } else {
                robot.setTurnRightRadians(angle);
            }
            robot.setAhead(100);
        }
    }

    public void onPaint(Graphics2D g) {
        g.setColor(Color.RED);
        for (int i = 0; i < _enemyWaves.size(); i++) {
            EnemyWave w = (EnemyWave) _enemyWaves.get(i);
            Point2D.Double center = w.getFireLocation();

            int radius = (int) w.getDistanceTraveled();

            if (radius - 40 < center.distance(_myLocation)) {
                g.drawOval(
                        (int) (center.x - radius),
                        (int) (center.y - radius),
                        radius * 2,
                        radius * 2
                );
            }
        }
    }
}
