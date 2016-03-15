package aiBots;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

public class Tank {
	private static HashMap<String, Tank> tanks = new HashMap<>();
	
	private Tank(String name){
		this.name = name;
		tanks.put(name, this);
	}
	
	public String name;
	public boolean isTeamMate;
	public long lastTimeScanned;
	public Point2D position;
	public double heading;
	public double velocity;
	public double bearing;
	public double energy;
	
	public static void UpdateTank(ScannedRobotEvent e, TeamRobot myRobot){
		Tank tank = tanks.get(e.getName());
		if(tank == null){
			tank = new Tank(e.getName());
			tank.isTeamMate = Arrays.asList(myRobot.getTeammates()).contains(e.getName());
		}
		tank.lastTimeScanned = e.getTime();
		double direction = myRobot.getHeading() + e.getBearing();
		tank.position = calculatePosFromRobot(myRobot.getX(), myRobot.getY(), (2*Math.PI*direction)/360, e.getDistance());
		tank.heading = e.getHeading();
		tank.bearing = e.getBearing();
		tank.energy = e.getEnergy();
	}
	
	private static Point2D calculatePosFromRobot(double x, double y, double angle, double distance) {

        double xPos = x + (Math.sin(angle) * distance);
        double yPos = y + (Math.cos(angle) * distance);

        return new Point2D.Double(xPos,yPos);
    }
	
	public static HashMap<String, Tank> getTanks(){
		return tanks;
	}
	
	public static Tank getTank(String name){
		return tanks.get(name);
	}
}
