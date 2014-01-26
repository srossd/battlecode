package team135alpha;

import battlecode.common.Direction;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {

	static RobotController rc;
	static Direction[] directions = Direction.values();

	/* channel numbers */
	static int releaseAttackChannel = 0;

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
				boolean surrounded = true;
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
						
					}
					surrounded = false;
					break;
				}

				if (surrounded) {
					rc.broadcast(releaseAttackChannel, 1);
				} else {

				}
			} catch (Exception e) {
				System.out.print("HQ Exception");
				e.printStackTrace();
			}
		}

	}

	private static void runSoldier() {

	}

	private static void runPastr() {

	}

	private static void runNoisetower() {

	}

}
