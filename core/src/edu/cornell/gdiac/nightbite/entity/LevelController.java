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
        JsonValue levelFormat = jsonReader.parse(Gdx.files.internal("jsons/level_funnel.json"));
        createGrounds(world, levelFormat.get("grounds"));

        createWalls(world, levelFormat.get("walls"));
        createHoles(world, levelFormat.get("holes"));
        createTeams(world, levelFormat.get("teams"));
        createItems(world, levelFormat.get("items"));

        createGrounds(world, levelFormat.get("decorations"));

        createBounds(world);
    }

    //TODO: switch x/y back once it's fixed in the level builder
    private void createGrounds(WorldModel world, JsonValue grounds) {
        WallModel wall;
        for (JsonValue groundJson : grounds.iterator()) {
            wall = new WallModel(
                    groundJson.getFloat("y"),
                    11 - groundJson.getFloat("x"),
                    groundJson.getInt("rotate")
            );
            wall.setTexture(Assets.FILES.get(groundJson.getString("texture")));
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName(groundJson.name());
            wall.setSensor(true);
            world.addStaticObject(wall);
        }
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
                    itemJson.getFloat("y"),
                    11 - itemJson.getFloat("x"),
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
            float x = teamJson.getFloat("y");
            float y = 11 - teamJson.getFloat("x");
            FilmStrip filmStrip = Assets.PLAYER_FILMSTRIPS[playerNum];
            float pWidth = filmStrip.getRegionWidth() / world.getScale().x;
            float pHeight = filmStrip.getRegionHeight() / world.getScale().y;
            String teamName = teamJson.name;

            player = new PlayerModel(x, y, pWidth, pHeight, filmStrip, teamName);
            player.setDrawScale(world.getScale());
            player.setActualScale(world.getActualScale());
            player.setName("player " + teamName);
            world.addPlayer(player);

            home = new HomeModel(x, y, teamName);
            home.setName("home " + teamName);
            home.setDrawScale(world.getScale());
            home.setActualScale(world.getActualScale());
            world.addStaticObject(home);
            playerNum++;
        }
    }

    private void createHoles(WorldModel world, JsonValue holes) {
        HoleModel hole;
        for (JsonValue holeJson : holes.iterator()) {
            hole = new HoleModel(
                    holeJson.getFloat("y"),
                    11 - holeJson.getFloat("x"),
                    holeJson.getInt("rotate"));
            hole.setDrawScale(world.getScale());
            hole.setActualScale(world.getActualScale());
            hole.setName(holeJson.name());
            String texture = holeJson.getString("texture");
            System.out.println(texture);
            hole.setTexture(Assets.FILES.get(texture));
            world.addStaticObject(hole);
        }
    }

    void createWalls(WorldModel world, JsonValue walls) {
        WallModel wall;
        for (JsonValue wallJson : walls.iterator()) {
            wall = new WallModel(
                    wallJson.getFloat("y"),
                    11 - wallJson.getFloat("x"),
                    wallJson.getInt("rotate"));
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName(wallJson.name());
            String texture = wallJson.getString("texture");
            Gdx.app.log("Texture", "Setting texture: " + texture);
            wall.setTexture(Assets.FILES.get(texture));
            world.addStaticObject(wall);
        }
    }
}
