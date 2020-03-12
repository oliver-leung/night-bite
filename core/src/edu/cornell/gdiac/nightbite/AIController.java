package edu.cornell.gdiac.nightbite;

import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;

public class AIController extends MechanicController {

    private int[][] map;

    public AIController() {
        map = new int [19][33];
    }

    public void updateAI(PooledList<Obstacle> objects) {
        // construct lattice
        // run a star
    }

    public void poll() {

    }
}
