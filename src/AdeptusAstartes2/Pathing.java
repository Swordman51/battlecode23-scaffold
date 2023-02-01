package AdeptusAstartes2;

import battlecode.common.*;

public class Pathing {
    //Bug nav- Bug 0
    static MapLocation current;
    static Direction currentDirection = null;
    static void moveTowards(RobotController rc, MapLocation target) throws GameActionException{
        if(rc.getLocation().equals(target)){ //target is an object, so you need to use .equals
            return; //already at location, break off method
        }
        //MapLocation current = rc.getLocation();
        if(!rc.isActionReady()){
            return; //not ready to move
        }
        Direction dir = rc.getLocation().directionTo(target);
        if(rc.canMove(dir)){
            rc.canMove(dir);
            currentDirection = null;
            //if you can move successfully here, there is no obstacle, and you can reset currentDirection because you don't need it
        } else {
            //going around the obstacle in the way, just Bug nav
            //if you're here, you can't move in the dir direction because there is an obstacle in the way.
            //keep the obstacle on the right hand side
            if (currentDirection == null) {
                currentDirection = dir;
            }
            for (int i = 0; i < 8; i++) { //you check all 8 directions to see if you can move in those directions
                if (rc.canMove(currentDirection)) {
                    rc.move(currentDirection);
                    //this checks if the obstacle is curving away from you, because you want to press against the object
                    currentDirection = currentDirection.rotateLeft(); //turn to the right
                    break; //found the direction you can move in
                } else {
                    currentDirection = currentDirection.rotateRight(); //turn to the left
                    rc.move(currentDirection);
                }
            }
        }

    }
}
