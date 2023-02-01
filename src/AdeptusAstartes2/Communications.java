package AdeptusAstartes2;

import battlecode.common.*;

import java.awt.*;
import java.util.*;
import java.util.List;

class Message{
    public int idx;
    public int value;
    public int turnAdded;

    Message(int idx, int value, int turnAdded){
        this.idx = idx;
        this.value = value;
        this.turnAdded = turnAdded;
    }
}
 class Communications {
    private static final int OUTDATED_TURNS_AMOUNT = 30;
    private static final int AREA_RADIUS = RobotType.CARRIER.visionRadiusSquared;
    static final int STARTING_ISLAND_IDX = GameConstants.MAX_STARTING_HEADQUARTERS;
    private  static final int START_ENEMY_IDX = 35 + GameConstants.MAX_STARTING_HEADQUARTERS;
    private static final int TOTAL_BITS = 16;
    private static final int MAPLOC_BITS = 12;
    private static final int TEAM_BITS = 1;
    private static final int HEALTH_BITS = 3;
    private static final int HEALTH_SIZE = (int) Math.ceil(Anchor.ACCELERATING.totalHealth/8.0); //percentage of the health, not the actual health

     private static List<Message> messagesQueue = new ArrayList<>(); //these two variables, messagesQueue and headquarter Locs, have a copy created for every bot made
     //this is because every robot created will have a copy of the Communications class, which contains these tow variables, and each object
     //will maintain its own headquarterLocs array and messagesQueue list to be used as needed
     private static MapLocation[] headquarterLocs = new MapLocation[GameConstants.MAX_STARTING_HEADQUARTERS];

     static void addHeadquarter(RobotController rc) throws GameActionException{
         MapLocation temp = rc.getLocation();
         for(int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++){
             if(rc.readSharedArray(i) == 0){ //not being used
                 rc.writeSharedArray(i, locationToInt(rc, temp));
                 break;
             }
         }
     }

     static void updateHeadquarterInfo(RobotController rc) throws GameActionException{
         if(RobotPlayer.turnCount == 2){
             for(int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++){
                headquarterLocs[i] = (intToLocation(rc, rc.readSharedArray(i)));
                if (rc.readSharedArray(i) == 0) { //reached end of the headquarters section
                    break;
                }
             }
         }
     }

     static void tryWriteMessages(RobotController rc) throws GameActionException{
         messagesQueue.removeIf(msg -> msg.turnAdded + OUTDATED_TURNS_AMOUNT < RobotPlayer.turnCount); //TODO look up this function later
         if(rc.canWriteSharedArray(0, 0)){
             while(messagesQueue.size() > 0){
                 Message msg = messagesQueue.remove(0);
                 if(rc.canWriteSharedArray(msg.idx, msg.value)){
                     rc.writeSharedArray(msg.idx, msg.value);
                 }
             }
         }
     }

     static void updateIslandInfo(RobotController rc, int id) throws GameActionException{
         MapLocation[] islandLocations = rc.senseNearbyIslandLocations(null, -1, id);
         if(islandLocations.length > 0){
             int idx_to_write = id + STARTING_ISLAND_IDX;
             int value = locationToInt(rc, islandLocations[0]);
             if(value != rc.readSharedArray(idx_to_write)){ //check if the element at the index is equal, if it is equal, then there is no point in writing to it again and wasting bytecode
                 rc.canWriteSharedArray(idx_to_write, value);
             }
         }
     }

     static void clearObsoleteEnemies(RobotController rc) throws GameActionException{
         for(int i = START_ENEMY_IDX; i < GameConstants.SHARED_ARRAY_LENGTH; i++){
             if(rc.readSharedArray(i) != 0) {
                 MapLocation enemy = intToLocation(rc, rc.readSharedArray(i));
                 if (rc.senseRobotAtLocation(enemy) == null) {
                     messagesQueue.add(new Message(i, 0, RobotPlayer.turnCount));
                 }
             }
         }
     }

     static void reportEnemy(RobotController rc) throws GameActionException{
        RobotInfo[] enemies = rc.senseNearbyRobots();
        for(int i = 0; i < enemies.length; i++){
            for(int j = START_ENEMY_IDX + 1; j < GameConstants.SHARED_ARRAY_LENGTH; j++){
                if(rc.readSharedArray(j) == 0){
                    if(rc.readSharedArray(j-1) != 0 && RobotPlayer.calcDistance(Communications.intToLocation(rc, rc.readSharedArray(j)), Communications.intToLocation(rc, rc.readSharedArray(j-1))) < RobotType.CARRIER.visionRadiusSquared){

                    } else {
                        messagesQueue.add(new Message(j, Communications.locationToInt(rc, enemies[i].location), RobotPlayer.turnCount));
                    }
                }
            }
        }
     }

     static int bitPackIslandInfo(RobotController rc, int islandId, MapLocation closestLoc) throws GameActionException{
        int intIsland = locationToInt(rc, closestLoc);
        intIsland = intIsland << (TOTAL_BITS - MAPLOC_BITS); //bitshift to the left by 4 bits
         try{
             Team team = rc.senseTeamOccupyingIsland(islandId);
             intIsland += team.ordinal(); //gets the number representing which team is occupying the island
             int health = rc.senseAnchorPlantedHealth(islandId);
             intIsland += (int) Math.ceil((float)health/HEALTH_SIZE);
             return intIsland;
         } catch (GameActionException e) {return intIsland;}
     }

     static Team readTeamHoldingIsland(RobotController rc, int islandID){ //these aren't public or private because they don't need to be accessed outside the package that they are in.
         try{
            islandID = islandID + STARTING_ISLAND_IDX;
            int islandInt = rc.readSharedArray(islandID);
            int healthMask = 0b111;
            int health = islandInt & healthMask; //bitwise "and" operator
            int team = (islandInt >> HEALTH_BITS) % 0b1;
            if(health > 0){
                return Team.values()[team]; //returns whatever the team section of the int is, provided that the island has health
            }
         } catch (GameActionException e) {} //catches the GameActionException just in case there is one
         return Team.NEUTRAL;
     }

     static MapLocation readislandLocation(RobotController rc, int islandID){
         try{
             islandID = islandID + STARTING_ISLAND_IDX;
             int islandInt = rc.readSharedArray(islandID);
             int islandLocIdx = islandInt >> (HEALTH_BITS + TEAM_BITS); //truncates the health and team parts, leaving only the part that contains the island
             return intToLocation(rc, islandLocIdx);
         } catch (GameActionException e) {return null;} //catches the GameActionException just in case there is one
     }

     static int readMaxIslandHealth(RobotController rc, int islandID){
         try{
             islandID = islandID + STARTING_ISLAND_IDX;
             int islandInt = rc.readSharedArray(islandID);
             int healthMask = 0b111;
             int health = islandInt & healthMask;
             return health;
         } catch (GameActionException e) {return -1;}
     }

     private static int locationToInt(RobotController rc, MapLocation m){
         if(m == null){
             return 0;
         }
         return 1 + m.x + m.y * rc.getMapWidth();
     }

      static MapLocation intToLocation(RobotController rc, int m){
         if(m == 0) {
             return null;
         }
         m--;
         return new MapLocation(m % rc.getMapWidth(), m/ rc.getMapWidth());
     }
}
