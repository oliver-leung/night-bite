package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;

public class AIController extends MechanicController {

    private int[][] map;

    public AIController() {
        map = new int [19][33];
    }

    public void updateAI(PooledList<Vector2> objects) {
        // construct lattice
//        for (Vector2 v : objects) {
//            System.out.println(v.x);
//            System.out.println(v.y);
//        }
//        System.out.println("-------");
        // run a star
    }

    public void poll() {

    }
}
