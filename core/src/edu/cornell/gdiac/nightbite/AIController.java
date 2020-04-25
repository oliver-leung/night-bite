package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.entity.HumanoidModel;
import edu.cornell.gdiac.nightbite.entity.ImmovableModel;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.nightbite.obstacle.SimpleObstacle;
import edu.cornell.gdiac.util.PooledList;

public class AIController extends MechanicController {
    private boolean[][] staticMap;
    private boolean[][] dynamicMap;
    private int numW;
    private int numH;
    private float partX;
    private float partY;

    public AIController(int w, int h) {
        numW = w;
        numH = h;
        staticMap = new boolean [w][h];
        // dynamicMap = new boolean [w][h];
    }

    public void populateStatic(Iterable<Obstacle> objects) {
        System.out.println("pop");
        for (Obstacle o : objects) {
            int x = (int) (o.getX() - 0.5f);
            int y = (int) (o.getY() - 0.5f);

            int width = (o instanceof ImmovableModel) ? (int) ((ImmovableModel) o).getWidth() : 1;
            int height = (o instanceof ImmovableModel) ? (int) ((ImmovableModel) o).getHeight() : 1;

            System.out.println("whatl");
            System.out.println(width + " " + height);

            for (int tx = x; tx < x + width; tx ++) {
                for (int ty = y; ty < y + height; ty ++) {
                    System.out.println("a");
                    if (bounded(tx, 0, numW) && bounded(ty, 0, numH)) {
                        staticMap[tx][ty] = true;
                    }
                }
            }
        }
        for (int x = 0; x < numW; x ++) {
            for (int y = 0; y < numH; y ++) {
                System.out.print(staticMap[x][y]);
                System.out.print(" ");
            }
            System.out.println();
        }
        System.out.println("endPop");
    }

    public void populateDynamic(Iterable<Obstacle> objects) {
        dynamicMap = new boolean [numW][numH];
        for (Obstacle o : objects) {
            // TODO: this assumes all dynamic things have height and width of 1
            // (Which probably isn't true for projectiles, if it matters)

            float x = (o.getX());
            float y = (o.getY());

            float offsetX = (o instanceof HumanoidModel ? (((HumanoidModel) o).getWidth()/2) : 0.5f);
            float offsetY = (o instanceof HumanoidModel ? (((HumanoidModel) o).getHeight()/2) : 0.5f);

            int ty = (int) (y + offsetY);
            int by = (int) (y - offsetY);
            int rx = (int) (x + offsetX);
            int lx = (int) (x - offsetX);

            boolean topy = bounded(ty, 0, numH);
            boolean boty = bounded(by, 0, numH);
            boolean rightx  = bounded(rx, 0, numW);
            boolean leftx = bounded( lx, 0, numW);

            if (leftx && boty) {
                dynamicMap[lx][by] = true;
            }
            if (rightx && boty) {
                dynamicMap[rx][by] = true;
            }
            if (leftx && topy) {
                dynamicMap[lx][ty] = true;
            }
            if (rightx && topy) {
                dynamicMap[rx][ty] = true;
            }
        }
    }

    private boolean bounded(int val, int min, int max) {
        return val >= min && val < max;
    }

    private boolean bounded(float val, float min, float max) {
        return val >= min && val < max;
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

    public void drawDebug(GameCanvas canvas, Vector2 drawScale) {
        for (int x = 0; x < numW; x ++) {
            for (int y = 0; y < numH; y ++) {
                if (staticMap[x][y]) {
                    canvas.drawPoint((x + 0.5f) * drawScale.x, (y + 0.5f) * drawScale.y, Color.RED);
                }
                if (dynamicMap[x][y]) {
                    canvas.drawPoint((x + 0.5f) * drawScale.x, (y + 0.5f) * drawScale.y, Color.GREEN);
                }
            }
        }
    }
}
