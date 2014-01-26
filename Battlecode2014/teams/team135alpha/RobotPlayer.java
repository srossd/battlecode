package team135alpha;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class RobotPlayer {

	static RobotController rc;
	static Direction[] directions = new Direction[] { Direction.NORTH,
			Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
			Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
			Direction.NORTH_WEST };

	/* channel numbers */
	static int releaseAttackChannel = 0;

	/* TODO replace temp */
	static int[] cornerChannels = new int[] { 1, 2, 3, 4 }; // corner pastr,
															// guard1, guard2,
															// noise
	static MapLocation[] corners;

	public static void run(RobotController rc) {

		RobotPlayer.rc = rc;

		while (true) {
			try {
				if (rc.getType() == RobotType.HQ) {
					runHQ();
				} else if (rc.getType() == RobotType.SOLDIER) {
					runSoldier();
				} else if (rc.getType() == RobotType.PASTR) {
					runPastr();
				} else if (rc.getType() == RobotType.NOISETOWER) {
					runNoisetower();
				}
			} catch (Exception e) {
				System.out.println("UNIDENTIFIED EXCEPTION");
				e.printStackTrace();
			}
		}

	}

	private static void runHQ() {
		while (true) {
			try {
				for (Direction d : directions) {
					while (!rc.isActive())
						rc.yield();
					for (Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
							Robot.class, 15, rc.getTeam().opponent()); nearbyEnemies.length > 0; nearbyEnemies = rc
							.senseNearbyGameObjects(Robot.class, 15, rc
									.getTeam().opponent())) {
						rc.attackSquare(rc.senseRobotInfo(nearbyEnemies[0]).location);
						while (!rc.isActive())
							rc.yield();
					}
					if (rc.senseRobotCount() < 25 && rc.canMove(d)) {
						if (rc.readBroadcast(releaseAttackChannel) != 0) {
							rc.broadcast(releaseAttackChannel, 0);
						}
						rc.spawn(d);
					}
				}
				// TODO REMOVE IF-STATEMENT AFTER ADDING CODE FOR ROBOTS
				// RESPONDING TO BROADCAST
				if (rc.readBroadcast(releaseAttackChannel) != 1) {
					rc.broadcast(releaseAttackChannel, 1);
				}
				rc.yield();
			} catch (Exception e) {
				System.out.println("HQ Exception");
				e.printStackTrace();
			}
		}

	}

	private static void runSoldier() {
		boolean docked = true;
		while (true) {
			try {
				while (!rc.isActive())
					rc.yield();
				for (Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
						Robot.class, 10, rc.getTeam().opponent()); nearbyEnemies.length > 0; nearbyEnemies = rc
						.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()
								.opponent())) {
					rc.attackSquare(rc.senseRobotInfo(nearbyEnemies[0]).location);
					while (!rc.isActive())
						rc.yield();
				}
				int width = rc.getMapWidth();
				int height = rc.getMapHeight();
				corners = new MapLocation[] {
						new MapLocation(width - 3, height - 3),
						new MapLocation(width - 3, 2),
						new MapLocation(2, height - 3), new MapLocation(2, 2) };
				MapLocation center = new MapLocation(width / 2, height / 2);
				for (int i = 0; i < 4; i++) {
					TerrainTile tt = rc.senseTerrainTile(corners[i]);
					if (tt != TerrainTile.NORMAL && tt != TerrainTile.ROAD)
						corners[i] = corners[i].add(corners[i]
								.directionTo(center));
				}
				if (docked) {
					if (rc.readBroadcast(releaseAttackChannel) == 1) {
						for (int i = 0; i < cornerChannels.length; i++) {
							int corner = rc.readBroadcast(cornerChannels[i]);
							if (corner < 4) {
								rc.broadcast(cornerChannels[i], corner + 1);
								navigate(corners[corner]);
								if (i == 0) {
									while (!rc.isActive())
										rc.yield();
									rc.construct(RobotType.PASTR);
									while (true)
										rc.yield();
								} else if (i == 4) {
									rc.construct(RobotType.NOISETOWER);
									while (true)
										rc.yield();
								} else {

								}

								break;
							}
						}
						docked = false;
					}
				}

			} catch (Exception e) {
				System.out.println("SOLDIER Exception");
				e.printStackTrace();
			}

		}
	}

	private static void navigate(MapLocation target) {
		System.out.println(target);
		try {
			int dirTo = rc.getLocation().directionTo(target).ordinal();
			int from = rc.getLocation().directionTo(rc.senseHQLocation())
					.ordinal();
			for (int distTo = rc.getLocation().distanceSquaredTo(target); distTo > 1
					|| distTo == 1 && rc.canMove(directions[dirTo]); distTo = rc
					.getLocation().distanceSquaredTo(target)) {
				while (!rc.isActive())
					rc.yield();
				for (Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
						Robot.class, 10, rc.getTeam().opponent()); nearbyEnemies.length > 0; nearbyEnemies = rc
						.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()
								.opponent())) {
					rc.attackSquare(rc.senseRobotInfo(nearbyEnemies[0]).location);
					while (!rc.isActive())
						rc.yield();
				}
				for (int i = dirTo + 8; i > dirTo; i--) {
					if (rc.canMove(directions[i % 8]) && i % 8 != from) {
						from = directions[i % 8].opposite().ordinal();
						rc.move(directions[i % 8]);
						break;
					}
				}
				dirTo = rc.getLocation().directionTo(target).ordinal();
			}
			System.out.println("reached target");
			rc.yield();
		} catch (Exception e) {
			System.out.println("Navigation Exception");
			e.printStackTrace();
		}
	}

	private static void runPastr() {

	}

	private static void runNoisetower() {

	}

}
