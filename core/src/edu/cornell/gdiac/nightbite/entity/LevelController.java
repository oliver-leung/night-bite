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

    public void populate(WorldModel world, String level_file) {
        JsonValue levelFormat = jsonReader.parse(Gdx.files.internal(level_file));
        createWalls(world, levelFormat.get("walls"));
        createHoles(world, levelFormat.get("holes"));
        createTeams(world, levelFormat.get("teams"));
        createItems(world, levelFormat.get("items"));
        createBounds(world);
    }

    private void createBounds(WorldModel world) {
        float width = world.getBounds().width;
        float height = world.getBounds().height;
        WallModel wall;
        for (int i = 0; i < width; i++) {
            wall = new WallModel(i, -1, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            world.addStaticObject(wall);

            wall = new WallModel(i, height + 1, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            world.addStaticObject(wall);
        }
        for (int i = 0; i < height; i++) {
            wall = new WallModel(-1, i, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            world.addStaticObject(wall);

            wall = new WallModel(width + 1, i, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            world.addStaticObject(wall);
        }
    }

    private void createItems(WorldModel world, JsonValue items) {
        ItemModel item;
        int itemNum = 0;
        for (JsonValue itemJson : items.iterator()) {
            item = new ItemModel(
                    itemJson.getFloat("x"),
                    itemJson.getFloat("y"),
                    1, 1, itemNum, Assets.FISH_ITEM
            );
            item.setName("item");
            item.setDrawScale(world.getScale());
            item.setActualScale(world.getActualScale());
            world.addItem(item);
            itemNum++;
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
            float pWidth = filmStrip.getRegionWidth() / world.getScale().x;
            float pHeight = filmStrip.getRegionHeight() / world.getScale().y;
            String teamName = teamJson.name;

            player = new PlayerModel(x, y, pWidth, pHeight, filmStrip, teamName);
            player.setDrawScale(world.getScale());
            player.setActualScale(world.getActualScale());
            player.setName("player " + teamName);
            world.addPlayer(player);

            home = new HomeModel(x, y, teamName, playerNum);
            home.setName("home " + teamName);
            home.setDrawScale(world.getScale());
            home.setActualScale(world.getActualScale());
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
            hole.setDrawScale(world.getScale());
            hole.setActualScale(world.getActualScale());
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
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName(wallJson.name());
            world.addStaticObject(wall);
        }
    }
}
