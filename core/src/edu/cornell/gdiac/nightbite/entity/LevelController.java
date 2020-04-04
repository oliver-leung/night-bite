package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.WorldModel;

public class LevelController {
    /**
     * Initial player positions
     */
    public static Vector2 p1_position = new Vector2(26, 3);
    public static Vector2 p2_position = new Vector2(6, 3);

    private static LevelController instance;
    private static JsonReader jsonReader;

    private LevelController() {
        jsonReader = new JsonReader();
    }

    public static LevelController getInstance() {
        if (instance == null) {
            instance = new LevelController();
        }
        return instance;
    }

    public void populate(WorldModel world) {
        JsonValue levelFormat = jsonReader.parse(Gdx.files.internal("jsons/level.json"));
        createWalls(world, levelFormat.get("walls"));
        createHoles(world, levelFormat.get("holes"));
        createTeams(world, levelFormat.get("teams"));
    }

    private void createTeams(WorldModel world, JsonValue players) {
        
    }

    private void createHoles(WorldModel world, JsonValue holes) {
        HoleModel hole;
        for (JsonValue wallJson : holes.iterator()) {
            hole = new HoleModel(
                    wallJson.getFloat("x"),
                    wallJson.getFloat("y"));
            hole.setDrawScale(world.scale);
            hole.setTexture(Assets.HOLE);
            hole.setName(wallJson.name());
            world.addStaticObject(hole);
        }
    }

    void createWalls(WorldModel world, JsonValue walls) {
        WallModel wall;
        for (JsonValue wallJson : walls.iterator()) {
            wall = new WallModel(
                    wallJson.getFloat("x"),
                    wallJson.getFloat("y"));
            wall.setDrawScale(world.scale);
            wall.setTexture(Assets.WALL);
            wall.setName(wallJson.name());
            world.addStaticObject(wall);
        }
    }
}
