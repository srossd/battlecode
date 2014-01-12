package team135;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class RobotPlayer {
	static Random rand;
	static Direction[] directions, roads;

	public static void run(RobotController rc) {
		rand = new Random();
		directions = new Direction[] { Direction.NORTH, Direction.NORTH_EAST,
				Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
				Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
		try {
			roads = roads(rc, rc.senseHQLocation());
		} catch (GameActionException e) {
			System.out.println("roads exception");
		}

		while (true) {
			if (rc.getType() == RobotType.HQ) {
				try {
					// Check if a robot is spawnable and spawn one if it is
					if (rc.isActive() && rc.senseRobotCount() < 25) {
						int r = rand.nextInt(roads.length);
						Direction toRoad = roads[r];
						if (rc.senseObjectAtLocation(rc.getLocation().add(
								toRoad)) == null) {
							rc.spawn(toRoad);
						}
					} else if (rc.isActive()) {
						Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
								Robot.class, 10, rc.getTeam().opponent());
						if (nearbyEnemies.length > 0
								&& rc.senseRobotInfo(nearbyEnemies[0]).type != RobotType.HQ) {
							RobotInfo robotInfo = rc
									.senseRobotInfo(nearbyEnemies[0]);
							rc.attackSquare(robotInfo.location);
						}
					}
				} catch (Exception e) {
					System.out.println("HQ Exception");
				}
			}

			if (rc.getType() == RobotType.SOLDIER) {
				try {
					if (rc.isActive()) {
						if (rc.getRobot().getID() % 2 == 0)
							attackBot(rc);
						else
							pastrBot(rc);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			rc.yield();
		}
	}

	public static void attackBot(RobotController rc) throws GameActionException {
		Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc
				.getTeam().opponent());
		MapLocation target = rc.senseEnemyHQLocation();
		if (rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation()) < 200
				&& nearbyEnemies.length > 0
				&& rc.senseRobotInfo(nearbyEnemies[0]).type != RobotType.HQ)
			target = rc.senseRobotInfo(nearbyEnemies[0]).location;
		if (rc.getLocation().distanceSquaredTo(rc.senseEnemyHQLocation()) < 200
				&& nearbyEnemies.length > 1
				&& rc.senseRobotInfo(nearbyEnemies[1]).type != RobotType.HQ)
			target = rc.senseRobotInfo(nearbyEnemies[1]).location;
		Direction toOpponent = rc.getLocation().directionTo(target);
		if (nearbyEnemies.length > 0
				&& rc.senseRobotInfo(nearbyEnemies[0]).type != RobotType.HQ) {
			RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[0]);
			rc.attackSquare(robotInfo.location);
			return;
		}
		int i = 0;
		if (rc.canMove(toOpponent)
				&& rc.senseTerrainTile(rc.getLocation().add(toOpponent)) == TerrainTile.ROAD) {
			rc.move(toOpponent);
			return;
		}
		i++;
		i = 0;
		for (Direction d = toOpponent; i < 8; d = d.rotateLeft()) {
			if (rc.canMove(d)) {
				rc.move(d);
				return;
			}
			i++;
		}

	}

	public static void pastrBot(RobotController rc) throws GameActionException {
		if (rand.nextInt(50) < 1
				&& rc.senseCowsAtLocation(rc.getLocation()) > 100)
			rc.construct(RobotType.PASTR);
		double[] cows = new double[8];
		for (int i = 0; i < 8; i++)
			cows[i] = rc.senseCowsAtLocation(rc.getLocation()
					.add(directions[i]));
		int index = maxIndex(cows);
		if (index < 0) {
			Direction[] roads = roads(rc, rc.getLocation());
			for (Direction d : roads) {
				if (rc.canMove(d)) {
					rc.sneak(d);
					return;
				}
			}
			for (Direction d : directions) {
				if (rc.canMove(d)) {
					rc.sneak(d);
					return;
				}
			}
		}
		if (rc.senseCowsAtLocation(rc.getLocation()) > Math.max(cows[index],
				100))
			rc.construct(RobotType.PASTR);
		else if (rc.canMove(directions[index]))
			rc.sneak(directions[index]);
		else {
			Direction d = directions[rand.nextInt(8)];
			if (rc.canMove(d))
				rc.move(d);
		}
	}

	public static Direction[] roads(RobotController rc, MapLocation loc)
			throws GameActionException {
		int n = 0;
		for (Direction d : directions) {
			if (rc.senseTerrainTile(loc.add(d)).equals(TerrainTile.ROAD))
				n++;
		}
		if (n == 0)
			return directions;
		Direction[] roads = new Direction[n];
		int i = 0;
		for (Direction d : directions) {
			if (rc.senseTerrainTile(loc.add(d)).equals(TerrainTile.ROAD))
				roads[i++] = d;
		}
		return roads;
	}

	public static int maxIndex(double[] xs) {
		int index = -1;
		for (int i = 0; i < xs.length; i++)
			if ((index == -1 && xs[i] > xs[0])
					|| (index != -1 && xs[i] > xs[index]))
				index = i;
		return index;
	}
}