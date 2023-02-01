package AdeptusAstartes2;

import battlecode.common.*;

public class Headquarter1 {
    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static boolean wroteHQlocations = false;
    static boolean makeAnchor = false;
    static void runHeadquarters(RobotController rc) throws GameActionException {
        // Pick a direction to build in.
        if (wroteHQlocations == false) {
            //rc.writeSharedArray(rc.getLocation().x, rc.getLocation().y); //update method
            wroteHQlocations = true;
        }
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
//        if (RobotPlayer.turnCount % 9000 == 0) {
//            makeAnchor = true;
//        }
       // if (makeAnchor == true) {
            if(RobotPlayer.turnCount % 10 == 0 && rc.getResourceAmount((ResourceType.ADAMANTIUM)) > 100) {
                if (rc.canBuildAnchor(Anchor.STANDARD)) {
                    // If we can build an anchor do it!
                    rc.buildAnchor(Anchor.STANDARD);
                    rc.setIndicatorString("Building anchor! " + rc.getAnchor());
                    makeAnchor = false;
                }
            }
        //} else {
        else {
            if (RobotPlayer.turnCount % 2 == 0) {

                // Let's try to build a carrier.
                rc.setIndicatorString("Trying to build a carrier");
                if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                    rc.buildRobot(RobotType.CARRIER, newLoc);
                }
            } else {

                    // Let's try to build a launcher.
                    rc.setIndicatorString("Trying to build a launcher");
                    if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                        rc.buildRobot(RobotType.LAUNCHER, newLoc);

                }
            }
        }
    }
}
