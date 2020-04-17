package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.WorldModel;
import edu.cornell.gdiac.util.FilmStrip;

public class LevelController {
    private static LevelController instance;
    private static JsonReader jsonReader;
    private JsonValue background;
    private JsonValue decorations;

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
        background = levelFormat.get("grounds");

        createWalls(world, levelFormat.get("walls"));
        createHoles(world, levelFormat.get("holes"));
        createTeams(world, levelFormat.get("teams"));
        createItems(world, levelFormat.get("items"));

        decorations = levelFormat.get("decorations");

        createBounds(world);
    }

    //TODO: switch x/y back once it's fixed in the level builder
    public void drawBackground(WorldModel world) {
        for (JsonValue groundJson : background.iterator()) {
            Vector2 pos = new Vector2(
                    1 + groundJson.getFloat("y"),
                    10 - groundJson.getFloat("x")
            );
            world.transformTileToWorld(pos);
            TextureRegion texture = Assets.get(groundJson.getString("texture"));
            GameCanvas.getInstance().draw(texture, Color.WHITE, 0, 0, pos.x, pos.y, 0, world.getActualScale().x, world.getActualScale().y);
        }
    }

    private void createBounds(WorldModel world) {
        float width = world.getBounds().width;
        float height = world.getBounds().height;
        WallModel wall;
        for (int i = 0; i < width; i++) {
            wall = new WallModel(i, 0, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            world.addStaticObject(wall);

            wall = new WallModel(i, height + 1f, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            world.addStaticObject(wall);
        }
        for (int i = 0; i < height; i++) {
            wall = new WallModel(0, i, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            world.addStaticObject(wall);

            wall = new WallModel(width + 1f, i, 0);
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
                    itemJson.getFloat("y") + 1,
                    10 - itemJson.getFloat("x"),
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
            float x = 1.5f + teamJson.getFloat("y");
            float y = 9.5f - teamJson.getFloat("x");
            FilmStrip filmStrip = Assets.PLAYER_FILMSTRIPS[playerNum];
            float pWidth = (filmStrip.getRegionWidth() - 30f) / world.getScale().x;
            float pHeight = filmStrip.getRegionHeight() / world.getScale().y;
            String teamName = teamJson.name;

            player = new PlayerModel(x, y, pWidth, pHeight, filmStrip, teamName);
            player.setDrawScale(world.getScale());
            player.setActualScale(world.getActualScale());
            player.setName("player " + teamName);
            world.addPlayer(player);

            home = new HomeModel(x, y, teamName, playerNum);
            home.setName(teamName);
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
                    1 + holeJson.getFloat("y"),
                    10 - holeJson.getFloat("x"),
                    holeJson.getInt("rotate"));
            hole.setDrawScale(world.getScale());
            hole.setActualScale(world.getActualScale());
            hole.setName(holeJson.name());
            String texture = holeJson.getString("texture");
            hole.setTexture(Assets.FILES.get(texture));
            world.addStaticObject(hole);
        }
    }

    void createWalls(WorldModel world, JsonValue walls) {
        WallModel wall;
        for (JsonValue wallJson : walls.iterator()) {
            wall = new WallModel(
                    1 + wallJson.getFloat("y"),
                    10 - wallJson.getFloat("x"),
                    wallJson.getInt("rotate"));
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName(wallJson.name());
            String texture = wallJson.getString("texture");
            Gdx.app.log("Texture", "Setting texture: " + texture);
            wall.setTexture(Assets.FILES.get(texture));
            if (wall.getTexture().getRegionWidth() > 65) {
                Vector2 pos = wall.getPosition();
                pos.x += 0.5f;
                pos.y -= 0.5f;
                wall.setPosition(pos);
                wall.setWidth(2);
                wall.setHeight(2);
            }
            world.addStaticObject(wall);
        }
    }
}
