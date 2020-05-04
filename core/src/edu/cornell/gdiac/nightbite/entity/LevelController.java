package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.WorldModel;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.LightSource;

public class LevelController {
    private static LevelController instance;
    private static JsonReader jsonReader = new JsonReader();
    /** Iterated over to maintain unique item numbers */
    private int itemNum = 0;
    /** Reference to the world that is being populated */
    private WorldModel world;

    public static LevelController getInstance() {
        if (instance == null) {
            instance = new LevelController();
        }
        return instance;
    }

    /**
     * Populate this world as specified in the level file
     *
     * @param world      WorldModel to be populated
     * @param level_file Level specification
     */
    public void populate(WorldModel world, String level_file) {
        this.world = world;
        createBounds();
        JsonValue levelFormat = jsonReader.parse(Gdx.files.internal(level_file));
        JsonValue cellArray = levelFormat.get("assets");
        int x = 0, y = 0;
        // Yeah, I know this is ugly
        for (JsonValue cellRow : cellArray) {
            for (JsonValue cell : cellRow) {
                for (JsonValue asset : cell) {

                    String type = asset.getString("type");
                    switch (type) {
                        case "ground":
                            this.world.setBackground(
                                    Assets.getTextureRegion(asset.getString("texture")),
                                    x, y
                            );
                            break;

                        case "decoration":
                            createDecoration(asset, x, y);
                            break;

                        case "item":
                            createItem(asset, x, y);
                            break;

                        case "team":
                            createTeam(asset, x, y);
                            break;

                        case "enemy":
                            createEnemy(asset, x, y);
                            break;

                        case "hole":
                            createHole(asset, x, y);
                            break;

                        case "wall":
                            createWall(asset, x, y);
                            break;

                    }

                }
                x++;
            }
            y++;
            x = 0;
        }
    }

    private void createDecoration(JsonValue asset, int x, int y) {
        String texture = asset.getString("texture");
        Sprite sprite = new Sprite(Assets.getTextureRegion(asset.getString("texture")));

        int rotate = asset.getInt("rotate") % 4;
        sprite.rotate((float) rotate * -90f);

        boolean flip = asset.getBoolean("flip");
        if (rotate % 2 == 0 ) {
            sprite.flip(flip, false);
        } else {
            sprite.flip(false, flip);
        }

        sprite.setPosition(x * world.getScale().x, y * world.getScale().y);

        if (texture.contains("Lantern")) { // Lantern
            world.setLantern(sprite, x, y);
        } else { // Brick
            world.setBrick(sprite, x, y);
        }

        if (asset.getBoolean("light")) {
            world.addLightBody(x, y);
        }
    }

    private void createBounds() {
        float width = world.getBounds().width;
        float height = world.getBounds().height;
        WallModel wall;
        // TODO: Use four walls rather than n X m
        for (int i = -1; i < width; i++) {
            wall = new WallModel(i, -1, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            wall.setFilterData(makeBoundsFilter());
            world.addStaticObject(wall);

            wall = new WallModel(i, height, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            wall.setFilterData(makeBoundsFilter());
            world.addStaticObject(wall);
        }
        for (int i = -1; i < height; i++) {
            wall = new WallModel(-1, i, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            wall.setFilterData(makeBoundsFilter());
            world.addStaticObject(wall);

            wall = new WallModel(width, i, 0);
            wall.setDrawScale(world.getScale());
            wall.setActualScale(world.getActualScale());
            wall.setName("bound");
            wall.setFilterData(makeBoundsFilter());
            world.addStaticObject(wall);
        }
    }

    private Filter makeBoundsFilter() {
        Filter filter;
        filter = new Filter();
        filter.categoryBits = Obstacle.STATIC_OBSTACLE;
        filter.maskBits = Obstacle.WALKBOX;
        return filter;
    }

    private void createItem(JsonValue itemJson, int x, int y) {
        ItemModel item = new ItemModel(
                x, y, itemNum,
                Assets.getTextureRegion("item/food1_64.png")
        );

        item.setName("item" + itemNum);
        item.setDrawScale(world.getScale());
        item.setActualScale(world.getActualScale());
        world.addItem(item);

        // TODO: Adjust light colors if needed
        if (itemJson.getBoolean("light")) {
            LightSource light = world.createPointLight(new float[]{0.15f, 0.05f, 0f, 1.0f}, 4.0f);
            light.attachToBody(item.getBody(), light.getX(), light.getY(), light.getDirection());
        }
    }

    private void createTeam(JsonValue teamJson, int x, int y) {
        String teamName = teamJson.getString("name");

        HomeModel home = new HomeModel(x, y, teamName);
        home.setDrawScale(world.getScale());
        home.setActualScale(world.getActualScale());

        TextureRegion texture = Assets.getFilmStrip("character/Filmstrip/Player_1/P1_Walk_8.png");
        float pWidth = (texture.getRegionWidth() - 30f) / world.getScale().x;
        float pHeight = texture.getRegionHeight() / world.getScale().y;
        PlayerModel player = new PlayerModel(x, y, pWidth, pHeight, world.getWorld(), teamName, home);
        player.setDrawScale(world.getScale());
        player.setActualScale(world.getActualScale());
        player.setName("player " + teamName);

        world.addStaticObject(home);
        world.addPlayer(player);

        LightSource light = world.createPointLight(new float[]{0.03f, 0.0f, 0.17f, 1.0f}, 4.0f);
        light.attachToBody(player.getBody(), light.getX(), light.getY(), light.getDirection());
    }

    private void createEnemy(JsonValue enemyJson, int x, int y) {
        EnemyModel enemy = null;
        switch (enemyJson.getString("enemyType")) {
            case "OilEnemy":
                enemy = new OilEnemyModel(x, y, world);
                break;
            case "FireEnemy":
                enemy = new FireEnemyModel(x, y, world);
                break;
//            case "ThiefEnemy":
//                break;
        }
        assert enemy != null;
        enemy.setDrawScale(world.getScale());
        enemy.setActualScale(world.getActualScale());
        enemy.setName(enemyJson.name());
        enemy.setFixedRotation(true);
        world.addEnemy(enemy);

        // TODO: Adjust light colors if needed
        if (enemyJson.getBoolean("light")) {
            LightSource light = world.createPointLight(new float[]{0.15f, 0.05f, 0f, 1.0f}, 4.0f);
            light.attachToBody(enemy.getBody(), light.getX(), light.getY(), light.getDirection());
        }
    }

    private void createHole(JsonValue holeJson, int x, int y) {
        HoleModel hole = new HoleModel(x, y, holeJson.getInt("rotate"));
        hole.setDrawScale(world.getScale());
        hole.setActualScale(world.getActualScale());
        hole.setName(holeJson.name());
        String texture = holeJson.getString("texture");
        hole.setTexture(Assets.getTextureRegion(texture));
        world.addStaticObject(hole);
    }

    private void createWall(JsonValue wallJson, int x, int y) {
        WallModel wall = new WallModel(x, y, wallJson.getInt("rotate"));
        wall.setDrawScale(world.getScale());
        wall.setActualScale(world.getActualScale());
        wall.setName(wallJson.getString("name"));
        String texture = wallJson.getString("texture");
        wall.setTexture(Assets.getTextureRegion(texture));

        int width = wall.getTexture().getRegionWidth();
        int height = wall.getTexture().getRegionHeight();
        if (width > 64 || height > 64) {
            int widthFactor = width / 64;
            int heightFactor = height / 64;
            Vector2 pos = wall.getPosition();
            pos.x +=  0.5f * (widthFactor-1);
            pos.y -= 0.5f * (heightFactor-1);
            wall.setPosition(pos);
            wall.setWidth(widthFactor);
            wall.setHeight(heightFactor);
        }

        if (wallJson.getBoolean("light")) {
            world.addLightBody(x, y);
        }

        world.addStaticObject(wall);
    }
}
