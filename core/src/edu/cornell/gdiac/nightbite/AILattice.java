package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.entity.HumanoidModel;
import edu.cornell.gdiac.nightbite.entity.ImmovableModel;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;

public class AILattice {
    private boolean[][] staticMap;
    private boolean[][] dynamicMap;
    private int numW;
    private int numH;
    private PooledList<Node> tempQueue;

    private static class Node {
        int x;
        int y;
        Node back;

        public Node(int x, int y, Node back) {
            this.x = x;
            this.y = y;
            this.back = back;
        }
    }

    public AILattice(int w, int h) {
        numW = w;
        numH = h;
        staticMap = new boolean [w][h];
        dynamicMap = new boolean[w][h];
        tempQueue = new PooledList<>();
    }

    public void populateStatic(Iterable<Obstacle> objects) {
        for (Obstacle o : objects) {
            int x = (int) (o.getX() - 0.5f);
            int y = (int) (o.getY() - 0.5f);

            int width = (o instanceof ImmovableModel) ? (int) ((ImmovableModel) o).getWidth() : 1;
            int height = (o instanceof ImmovableModel) ? (int) ((ImmovableModel) o).getHeight() : 1;

            for (int tx = x; tx < x + width; tx ++) {
                for (int ty = y; ty < y + height; ty ++) {
                    if (bounded(tx, 0, numW) && bounded(ty, 0, numH)) {
                        staticMap[tx][ty] = true;
                    }
                }
            }
        }
        for (int x = 0; x < numW; x ++) {
            for (int y = 0; y < numH; y ++) {
//                System.out.print(staticMap[x][y]);
            }
        }
    }

    public void clearDynamic() {
        dynamicMap = new boolean [numW][numH];
    }

    public void populateDynamic(Iterable<Obstacle> objects) {
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

    private Node bfs(Iterable<GridPoint2> target, GridPoint2 position) {
        boolean[][] visited = new boolean[numW][numH];
        tempQueue.clear();
        tempQueue.add(new Node(position.x, position.y, null));

        boolean[][] goal = new boolean[numW][numH];

        for (GridPoint2 t : target) {
            if (t.x >= goal.length || t.y >= goal[0].length) {
                continue;
            }
            goal[t.x][t.y] = true;
        }

        while (!tempQueue.isEmpty()) {
            Node n = tempQueue.poll();

            if (!bounded(n.x, 0, numW) || !bounded(n.y, 0, numH)) {
                continue;
            }

            if ((staticMap[n.x][n.y]) &&
                    (position.x != n.x || position.y != n.y)) {
                continue;
            }

            if (goal[n.x][n.y]) {
                return n;
            }

            if ((dynamicMap[n.x][n.y]) &&
                    (position.x != n.x || position.y != n.y)) {
                continue;
            }

            if (visited[n.x][n.y]) {
                continue;
            }

            visited[n.x][n.y] = true;

            // No diagonals for now
            tempQueue.add(new Node(n.x + 1, n.y, n));
            tempQueue.add(new Node(n.x - 1, n.y, n));
            tempQueue.add(new Node(n.x, n.y + 1, n));
            tempQueue.add(new Node(n.x, n.y - 1, n));
        }

        return null;
    }

    private boolean dfs(int targetX, int targetY, int sourceX, int sourceY,
                        boolean[][] visited, int depth, int maxDepth) {
        // System.out.printf("%d, %d, %d, %d, %d, %d", targetX, targetY, sourceX, sourceY, depth, maxDepth);
        if (depth >= maxDepth) {
            // System.out.println("lol");
            return false;
        }

        if (sourceX < 0 || sourceX >= numW || sourceY < 0 || sourceY >= numH) {
            return false;
        }

        if (visited[sourceX][sourceY]) {
            return false;
        }

        if (staticMap[sourceX][sourceY]) {
            return false;
        }

        if (targetX == sourceX && targetY == sourceY) {
            return true;
        }

        visited[sourceX][sourceY] = true;

        boolean up = dfs(targetX, targetY, sourceX, sourceY+1, visited, depth+1, maxDepth);
        boolean down = dfs(targetX, targetY, sourceX, sourceY-1, visited, depth+1, maxDepth);;
        boolean left = dfs(targetX, targetY, sourceX-1, sourceY, visited, depth+1, maxDepth);;
        boolean right = dfs(targetX, targetY, sourceX+1, sourceY, visited, depth+1, maxDepth);;

        // System.out.println("ha");

        return up || down || left || right;
    }

    private boolean dfs(Vector2 source, Vector2 target, int dist) {
        // System.out.println(source);
        // System.out.println(target);
        boolean[][] visited = new boolean[numW][numH];
        return dfs((int) target.x, (int) target.y, (int) source.x, (int) source.y, visited, 0, dist);
    }

    public boolean isReachable(Vector2 source, Vector2 target) {
        // Manhattan distance as upper bound
        int dist = (int) (Math.ceil(target.x - source.x) + Math.ceil(target.y - source.y)) + 30;
        return dfs(source, target, dist);
    }

    public void findPath(PooledList<GridPoint2> prev, Iterable<GridPoint2> target, GridPoint2 position) {
        prev.clear();

        Node n = bfs(target, position);

        if (n == null) {
            return;
        }

        while (n.back != null) {
            prev.add(0, new GridPoint2(n.x, n.y));
            n = n.back;
        }

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

    public static void drawPath(GameCanvas canvas, PooledList<GridPoint2> path, Vector2 drawScale, Color color) {
        for (int i = 0; i < path.size()-1; i ++) {
            GridPoint2 p1 = path.get(i);
            GridPoint2 p2 = path.get(i+1);

            canvas.drawLine((p1.x + 0.5f) * drawScale.x, (p1.y + 0.5f) * drawScale.y,
                    (p2.x + 0.5f) * drawScale.x, (p2.y + 0.5f) * drawScale.y, color);
        }
    }
}
