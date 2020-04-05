package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.WorldModel;
import edu.cornell.gdiac.util.FilmStrip;

public class LevelController {
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
        createItems(world, levelFormat.get("items"));
    }

    private void createItems(WorldModel world, JsonValue items) {
        ItemModel item;
        for (JsonValue itemJson : items.iterator()) {
            item = new ItemModel(
                    itemJson.getFloat("x"),
                    itemJson.getFloat("y"),
                    1, 1, Assets.FISH_ITEM
            );
            item.setName("item");
            item.setDrawScale(world.scale);
            world.addItem(item);
        }
    }

    private void createTeams(WorldModel world, JsonValue teams) {
        PlayerModel player;
        HomeModel home;
        int playerNum = 0;
        for (JsonValue teamJson : teams.iterator()) {
            float x = teamJson.getFloat("x");
            float y = teamJson.getFloat("y");
            FilmStrip filmStrip = Assets.PLAYER_FILMSTRIPS[playerNum];
            float pWidth = filmStrip.getRegionWidth() / world.scale.x;
            float pHeight = filmStrip.getRegionHeight() / world.scale.y;
            String teamName = teamJson.name;

            player = new PlayerModel(x, y, pWidth, pHeight, filmStrip, teamName);
            player.setDrawScale(world.scale);
            player.setName("player " + teamName);
            world.addPlayer(player);

            home = new HomeModel(x, y, teamName);
            home.setName("home " + teamName);
            home.setDrawScale(world.scale);
            world.addStaticObject(home);
            playerNum++;
        }
    }

    private void createHoles(WorldModel world, JsonValue holes) {
        HoleModel hole;
        for (JsonValue wallJson : holes.iterator()) {
            hole = new HoleModel(
                    wallJson.getFloat("x"),
                    wallJson.getFloat("y"),
                    wallJson.getInt("rotate"));
            hole.setDrawScale(world.scale);
            hole.setName(wallJson.name());
            world.addStaticObject(hole);
        }
    }

    void createWalls(WorldModel world, JsonValue walls) {
        WallModel wall;
        for (JsonValue wallJson : walls.iterator()) {
            wall = new WallModel(
                    wallJson.getFloat("x"),
                    wallJson.getFloat("y"),
                    wallJson.getInt("rotate"));
            wall.setDrawScale(world.scale);
            wall.setName(wallJson.name());
            world.addStaticObject(wall);
        }
    }
}
