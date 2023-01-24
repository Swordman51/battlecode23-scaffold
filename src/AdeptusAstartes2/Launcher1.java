package AdeptusAstartes2;

import battlecode.common.*;

public class Launcher1 {
    static MapLocation hqLoc;
    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        if (hqLoc == null) {
            scanHQ(rc);
            rc.setIndicatorString("HQ found");
        }
    //write a program that find the location of an enemy and stores it, then calls other bots over to gank the enemy
        //use the shared array in some way to store this location




        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        int lowestHealth = Integer.MAX_VALUE;
        int smallestDist = Integer.MAX_VALUE;
        RobotInfo marked = null;


        if (enemies.length > 0) { //if enemies' length is equal to 0, it makes no sense to run the attack command, so you need to set it so that
            //it'll only attack when there is at least 1 enemy
            for (int i = 0; i < enemies.length; i++) {
                if (enemies[i].getType() != RobotType.HEADQUARTERS) { //HEADQUARTERS is an enum in RobotType
                    int enemyHealth = enemies[i].getHealth();
                    int enemyDist = enemies[i].getLocation().distanceSquaredTo(rc.getLocation());

                    if (enemyHealth < lowestHealth) {
                        marked = enemies[i];
                        lowestHealth = enemyHealth;
                        smallestDist = enemyDist;
                    } else if (enemyHealth == lowestHealth) {
                        if (enemyDist < smallestDist) {
                            marked = enemies[i];
                            lowestHealth = enemyHealth;
                            smallestDist = enemyDist;
                        }

                    }
                }

            }
            if (marked != null) {
                rc.setIndicatorString("marked for death" + marked.getType());
                if (rc.canAttack(marked.location)) {
                    rc.setIndicatorString("Attacking marked target" + marked.getType());
                    rc.attack(marked.location);
                    RobotPlayer.moveDiagonal(rc);
                }

            } else {
//                Direction dir = hqLoc.directionTo(rc.getLocation());
//                if (rc.canMove(dir)) {
//                    rc.move(dir);
//                } else {
//                    RobotPlayer.moveRandom(rc);
//                }
                RobotPlayer.moveRandom(rc);
            }
            //MapLocation toAttack = rc.getLocation().add(Direction.EAST); //attacks one unit to the east using this command


        }
        RobotPlayer.moveRandom(rc);
        RobotInfo[] visibleEnemies = rc.senseNearbyRobots(-1, opponent);
        // Also try to move randomly.
        if (visibleEnemies.length > 0) {
            for (int i = 0; i < visibleEnemies.length; i++) {
                if (visibleEnemies[i].getType() != RobotType.HEADQUARTERS) {
                    Direction dir = rc.getLocation().directionTo(visibleEnemies[0].getLocation()); //use getlocation() for getting the location of enemy robots
                    //you can only use the location variable on your own robots, not the opponent's variables
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
            }
        }

        //RobotPlayer.moveRandom(rc);

    }
    static void moveWell(RobotController rc) throws GameActionException{
        WellInfo[] wells = rc.senseNearbyWells();
        if(wells.length > 0) {
            MapLocation wellLoc = wells[0].getMapLocation();
            Direction dir = rc.getLocation().directionTo(wellLoc);
            if(rc.canMove(dir)){
                rc.move(dir);
            }
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
}

