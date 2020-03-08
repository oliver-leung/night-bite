package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.Vector2;

public class LevelController {
    public static final float[] WALL2 = {-0.5f, 5.0f, 0.5f, 5.0f, 0.5f, 0.0f, -0.5f, 0.0f};
    /**
     * Wall
     */
    public static final float[] WALL1 = {-2.0f, 10.5f, 2.0f, 10.5f, 2.0f, 9.5f, -2.0f, 9.5f};
    /**
     * Wall for screen edge
     */
    public static final float[] VERT_WALL = {-0.5f, 18.0f, 0.5f, 18.0f, 0.5f, 0.0f, -0.5f, 0.0f};
    public static final float[] HORI_WALL = {0.0f, 0.5f, 32.0f, 0.5f, 32.0f, -0.5f, 0.0f, -0.5f};
    public static Vector2 p1_position = new Vector2(26, 3);
    public static Vector2 p2_position = new Vector2(6, 3);
}
