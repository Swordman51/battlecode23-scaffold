package AdeptusAstartes2;

import battlecode.common.*;
import battlecode.world.Island;

import java.awt.*;
import java.util.Map;

public class Carrier1 {

    static MapLocation hqLoc;
    static MapLocation wellloc;
    static MapLocation islandLoc;
    static boolean anchorMode = false;
    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */

    static void runCarrier(RobotController rc) throws GameActionException {

        if (hqLoc == null) {
            scanHQ(rc);
            rc.setIndicatorString("HQ found");
        }
        if (wellloc == null) scanWells(rc);

        if(islandLoc == null){
            scanIsland(rc);
        }
//

        if (rc.canTakeAnchor(hqLoc, Anchor.STANDARD)) {
            rc.takeAnchor(hqLoc, Anchor.STANDARD);
            anchorMode = true;
        }

        if (anchorMode == true) {
            if(islandLoc == null) {
                RobotPlayer.moveRandom(rc);
            } else {
                Direction dir = rc.getLocation().directionTo(islandLoc);
                if(rc.canMove(dir)){
                    rc.move(dir);
                } else {
                    RobotPlayer.moveTowards(rc, islandLoc);
                }
            }

            if(rc.getLocation().equals(islandLoc)) {
                rc.placeAnchor();
                anchorMode = false;
            }
        } else {

            //collection from well location if close and inventory is not full
            if (wellloc != null && rc.canCollectResource(wellloc, -1)) {
                if (rc.canCollectResource(wellloc, -1)) {
                    rc.collectResource(wellloc, -1);
                    rc.setIndicatorString("Collecting Resource");
                }

            } else {


                if (isFull(rc) == true) { //move toward headquarters
                    //the bots are getting stuck
                    if (rc.getLocation().isAdjacentTo(hqLoc)) {
                        depositResource(rc, ResourceType.ADAMANTIUM);
                        depositResource(rc, ResourceType.MANA);
                        rc.setIndicatorString("Depositing");
                    } else {
                        Direction dir = rc.getLocation().directionTo(hqLoc);
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    }

                } else {
                    if (wellloc != null) {
                        if (rc.getLocation().isAdjacentTo(wellloc) == false && rc.getLocation() != wellloc) {
                            Direction dir = rc.getLocation().directionTo(wellloc);
                            if (rc.canMove(dir)) {
                                rc.move(dir);
                            } else {
                                RobotPlayer.moveTowards(rc, wellloc);
                            }
                        }
                    } else {
                        RobotPlayer.moveRandom(rc);
                    }
                }
            }
            //depositResource(rc, ResourceType.ELIXIR);

            // Occasionally try out the carriers attack
            if (RobotPlayer.rng.nextInt(20) == 1) {
                RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                if (enemyRobots.length > 0) {
                    if (rc.canAttack(enemyRobots[0].location)) {
                        rc.attack(enemyRobots[0].location);
                    }
                }
            }


        }
    }
    static boolean isFull(RobotController rc) {
        if (rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) < GameConstants.CARRIER_CAPACITY) {
            return false;
        } else {
            return true;
        }
    }

    static void scanHQ(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.getType() == RobotType.HEADQUARTERS) {
                hqLoc = robot.getLocation(); //sets hq location for the rest of the game
                break;
            }
        }
    }

    static void scanWells(RobotController rc) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 0) {
            int max = Integer.MAX_VALUE;
            for (WellInfo well : wells) {
                int xdiff = (int) Math.pow(well.getMapLocation().x - rc.getLocation().x, 2);
                int ydiff = (int) Math.pow(well.getMapLocation().y - rc.getLocation().y, 2);
                if((ydiff + xdiff) < max){
                    wellloc = well.getMapLocation();
                }
            }
        }
    }

    static void scanIsland(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for (int id : ids) {
            if(rc.senseTeamOccupyingIsland(id) == Team.NEUTRAL){
                MapLocation[] islandlocs = rc.senseNearbyIslandLocations(id);
                int max = Integer.MAX_VALUE;
                for (MapLocation loc : islandlocs){


                        int xdiffd = (int) Math.pow(loc.x - rc.getLocation().x, 2);
                        int ydiffd = (int) Math.pow(loc.y - rc.getLocation().y, 2);
                        if((ydiffd + xdiffd) < max){
                            islandLoc = loc;

                    }
                }

            }
        }
    }

    static void depositResource(RobotController rc, ResourceType type) throws GameActionException {
        int amount = rc.getResourceAmount(type);
        if (amount > 0){
            if(rc.canTransferResource(hqLoc, type, amount)) rc.transferResource(hqLoc, type, amount);
        }
    }


}
//placing anchor code
//if (rc.getAnchor() != null) {
//            // If I have an anchor singularly focus on getting it to the first island I see
//            int[] islands = rc.senseNearbyIslands();
//            Set<MapLocation> islandLocs = new HashSet<>();
//            for (int id : islands) {
//                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
//                islandLocs.addAll(Arrays.asList(thisIslandLocs));
//            }
//            if (islandLocs.size() > 0) {
//                MapLocation islandLocation = islandLocs.iterator().next();
//                rc.setIndicatorString("Moving my anchor towards " + islandLocation);
//                while (!rc.getLocation().equals(islandLocation)) {
//                    Direction dir = rc.getLocation().directionTo(islandLocation);
//                    if (rc.canMove(dir)) {
//                        rc.move(dir);
//                    }
//                }
//                if (rc.canPlaceAnchor()) {
//                    rc.setIndicatorString("Huzzah, placed anchor!");
//                    rc.placeAnchor();
//                }
//            }
//        }
