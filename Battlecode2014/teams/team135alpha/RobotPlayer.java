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

	static boolean docked = true; // for soldiers docked to HQ

	/* channel numbers */
	static int releaseAttackChannel = 0;

	static int[] targetChannels = new int[] { 1, // pastr
			2, // guard 1
			3, // guard 2
			4 // noise tower
	};

	static MapLocation[] targets;

	static MapLocation target = null;

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
		while (true) {
			try {
				if (docked) {
					while (rc.readBroadcast(releaseAttackChannel) == 0) {
						while (!rc.isActive())
							rc.yield();
						attack();
					}
					docked = false;
				}
				for (int i = 0; i < targetChannels.length; i++) {
					int t = rc.readBroadcast(targetChannels[i]);
					if (t < 4) {
						//System.out.println(i+" "+t);
						rc.broadcast(targetChannels[i], t + 1);
						setTarget(t);
						navigate();
						//System.out.println("done navigating");
						if (i == 0) {
							while (!rc.isActive())
								rc.yield();
							//rc.construct(RobotType.PASTR);
							while (true)
								rc.yield();
						} else if (i == 4) {
							//System.out.println("noisinngg");
							rc.construct(RobotType.NOISETOWER);
							while (true)
								rc.yield();
						} else {
							while (true) {
								rc.yield();
							}
						}
					}
				}

			} catch (Exception e) {
				System.out.println("SOLDIER Exception");
				e.printStackTrace();
			}

		}
	}

	private static void setTarget(int t) {
		try {
			int width = rc.getMapWidth();
			int height = rc.getMapHeight();
			targets = new MapLocation[] {
					new MapLocation(width - 3, height - 3),
					new MapLocation(width - 3, 2),
					new MapLocation(2, height - 3), new MapLocation(2, 2) };
			MapLocation center = new MapLocation(width / 2, height / 2);
			for (int i = 0; i < 4; i++) {

				for (TerrainTile tt = rc.senseTerrainTile(targets[i]); tt != TerrainTile.NORMAL
						&& tt != TerrainTile.ROAD; tt = rc
						.senseTerrainTile(targets[i]))
					targets[i] = targets[i].add(targets[i].directionTo(center));
			}
			target = targets[t];
		} catch (Exception e) {
			System.out.println("Targeting Exception");
			e.printStackTrace();
		}
	}

	private static void navigate() {
		//System.out.println(target);
		try {
			int dirTo = rc.getLocation().directionTo(target).ordinal();
			int from = rc.getLocation().directionTo(rc.senseHQLocation())
					.ordinal();
			for (int distTo = rc.getLocation().distanceSquaredTo(target); distTo > 2
					|| distTo > 0 && rc.canMove(directions[dirTo]); distTo = rc
					.getLocation().distanceSquaredTo(target)) {
				attack();
				for (int i = dirTo + 8; i > dirTo; i--) {
					if (rc.canMove(directions[i % 8]) && i % 8 != from) {
						from = directions[i % 8].opposite().ordinal();
						rc.move(directions[i % 8]);
						while (!rc.isActive())
							rc.yield();
						break;
					}
				}
				dirTo = rc.getLocation().directionTo(target).ordinal();
			}
			//System.out.println("reached target");
		} catch (Exception e) {
			System.out.println("Navigation Exception");
			e.printStackTrace();
		}
	}

	private static void attack() {
		try {
			while (!rc.isActive())
				rc.yield();
			for (Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,
					10, rc.getTeam().opponent()); nearbyEnemies.length > 0; nearbyEnemies = rc
					.senseNearbyGameObjects(Robot.class, 10, rc.getTeam()
							.opponent())) {
				rc.attackSquare(rc.senseRobotInfo(nearbyEnemies[0]).location);
				while (!rc.isActive())
					rc.yield();
			}
		} catch (Exception e) {
			System.out.println("Attacking Exception");
			e.printStackTrace();
		}
	}

	private static void runPastr() {
		try {
			while (true)
				rc.yield();
		} catch (Exception e) {
			System.out.println("PASTR Exception");
			e.printStackTrace();
		}
	}

	private static void runNoisetower() {
		try {
			while (true)
				rc.yield();
		} catch (Exception e) {
			System.out.println("PASTR Exception");
			e.printStackTrace();
		}
	}

}
