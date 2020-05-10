package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;

public class Debug {
    // This is a class which I want to put all the debugging stuff inside it.
    // So you can update and draw and have it separated from everything else
    // It stores state stuff that probably shouldn't be relavent anywhere else

    // If we want to do this legitly we should have debug modules that we can enable
    // or disable at will

    private PooledList<GridPoint2> path;
    private WorldModel world;
    private int countdown;
    private ArrayList<GridPoint2> target;

    public Debug() {
        path = new PooledList<>();
        target = new ArrayList<>();
        target.add(new GridPoint2(2, 5));
    }

    public void updatePathfinding(AILattice ai) {
        if (countdown > 0) {
            countdown -= 1;
            return;
        }

        countdown = 60;
        ai.findPath(path, target, new GridPoint2(9,6), -1);

        // for (GridPoint2 p : path) {
        //     System.out.printf("(%x, %x)\n", p.x, p.y);
        // }
        // System.out.println();
    }

    public void drawPathfinding(GameCanvas canvas, Vector2 drawScale) {
        AILattice.drawPath(canvas, path, drawScale, Color.BROWN);
    }
}
