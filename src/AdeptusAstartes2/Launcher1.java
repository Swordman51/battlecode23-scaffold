package AdeptusAstartes2;

import battlecode.common.*;

public class Launcher1 {

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) { //if enemies' length is equal to 0, it makes no sense to run the attack command, so you need to set it so that
            //it'll only attack when there is at least 1 enemy

            for(int i = 0; i < enemies.length; i++){
                if(enemies[i].getType() != RobotType.HEADQUARTERS){ //HEADQUARTERS is an enum in RobotType
                    MapLocation toAttack = enemies[0].location;
                    if (rc.canAttack(toAttack)) {
                        rc.setIndicatorString("Attacking" + toAttack);
                        rc.attack(toAttack);
                    }
                    break;
                }

            }

            //MapLocation toAttack = rc.getLocation().add(Direction.EAST); //attacks one unit to the east using this command


        }
        RobotInfo[] visibleEnemies = rc.senseNearbyRobots(-1, opponent);
        // Also try to move randomly.
        if (visibleEnemies.length > 0) {
            for(int i = 0; i < visibleEnemies.length; i++){
                if(visibleEnemies[i].getType() != RobotType.HEADQUARTERS){
                    Direction dir = rc.getLocation().directionTo(visibleEnemies[0].getLocation()); //use getlocation() for getting the location of enemy robots
                    //you can only use the location variable on your own robots, not the opponent's variables
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
            }



        }

    RobotPlayer.moveRandom(rc);

    }
}
