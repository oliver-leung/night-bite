package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;

public class AIController extends MechanicController {

    private int[][] staticMap;
    private int[][] dynamicMap;
    private int numW;
    private int numH;
    private float partX;
    private float partY;

    public AIController(int w, int h, Rectangle bounds) {
        numW = w;
        numH = h;
        partX = bounds.x/numW;
        partY = bounds.y/numH;
        staticMap = new int [h][w];
        dynamicMap = new int [h][w];
    }

    public void populateStatic() {

    }

    public void updateAI() {
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

    public void drawDebug(GameCanvas canvas) {
    }
}
