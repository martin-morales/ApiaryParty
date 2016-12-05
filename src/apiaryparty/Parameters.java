package apiaryparty;
/**
 * Defines constant variables used in simulation
 */

public class Parameters {
	/**Time limit for initialization in milliseconds*/
    public static final int INIT_TIME = 1000;
    /**Time limit for calculating an action result in milliseconds*/
    public static final int RESULT_TIME = 500;
    /**Time limit for making an action in milliseconds*/
    public static final int ACTION_TIME = 500;
	
    /**Identifies the number of nodes in an initial graph*/
    public static final int NUMBER_OF_NODES = 10;
    /**Identifies the number of public nodes in an initial graph*/
    public static final int NUMBER_OF_PUBLIC_NODES = 2;
    /**Identifies the number of databases in an initial graph*/
    public static final int NUMBER_OF_DATABASE_NODES = 1;
    /**Identifies the maximum number of neighbors in an initial graph for non-router nodes*/
    public static final int MAX_NEIGHBORS = 2;
    /**Identifies the minimum number of neighbors in an initial graph for non-router nodes*/
    public static final int MIN_NEIGHBORS = 1;
    /**The maximum PointValue*/
    public static final int MAX_POINT_VALUE = 20;
    /**The maximum number of router edges*/
    public static final int MAX_ROUTER_EDGES = 2;

    /**The value that the defender gets for each node in a graph*/
    public static final int DEFENDER_RATE = 10;
    /**Defender's Budget*/
    public static final int DEFENDER_BUDGET = DEFENDER_RATE * NUMBER_OF_NODES;
    /**The cost to strengthen a node*/
    public static final int STRENGTHEN_RATE = 7;
    /**The cost for having invalid actions*/
    public static final int INVALID_RATE = 10;
    /**The cost to remove an edge (add a firewall)*/
    public static final int FIREWALL_RATE = 8;
    /**The cost to add a honeypot*/
    public static final int HONEYPOT_RATE = 15;

    /**The value that the attacker gets for each node in a graph*/
    public static final int ATTACKER_RATE = 10;
    /**The maximum value for an attacker roll*/
    public static final int ATTACK_ROLL = 20;
    /**The cost for performing an attack*/
    public static final int ATTACK_RATE = 8;
    /**The maximum value for a super attack*/
    public static final int SUPERATTACK_ROLL = 50;
    /**The cost for performing a super attack*/
    public static final int SUPERATTACK_RATE = 20;
    /**The cost to probe for the point value*/
    public static final int PROBE_POINTS_RATE = 2;
    /**The cost to probe for the point value*/
    public static final int PROBE_HONEY_RATE = 1;
    /**Penalty for successfully capturing a honey pot*/
    public static final int HONEY_PENALTY = -10;
    /**Attacker's Budget*/
    public static final int ATTACKER_BUDGET = ATTACKER_RATE * NUMBER_OF_NODES;
}
