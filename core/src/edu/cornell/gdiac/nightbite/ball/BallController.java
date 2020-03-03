package edu.cornell.gdiac.nightbite.ball;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.nightbite.HoleModel;
import edu.cornell.gdiac.nightbite.HomeModel;
import edu.cornell.gdiac.nightbite.InputController;
import edu.cornell.gdiac.nightbite.WorldController;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;

public class BallController extends WorldController implements ContactListener {

    /**
     * Reference to the ball texture
     */
    private static final String PLAYER1_TEXTURE = "ball/char1trimmed.png";
    private static final String PLAYER2_TEXTURE = "ball/char2-f1.png";
    private static final String PLAYER_WITH_ITEM_TEXTURE = "ball/ballItem.png";
    private static final String ITEM_TEXTURE = "ball/fish.png";
    public static final int ITEMS_TO_WIN = 3;

    /**
     * Texture assets for the ball
     */
    private TextureRegion player1Texture;
    private TextureRegion player2Texture;
    private TextureRegion ballItemTexture;
    private TextureRegion itemTexture;


    /** Player 1 */
    private BallModel p1;
    private static Vector2 p1_position = new Vector2(26,3);
    /** Player 2 */
    private BallModel p2;
    private static Vector2 p2_position = new Vector2(6, 3);
    /** Item */
    private BoxObstacle item;
    private static Vector2 item_position = new Vector2(16, 12);
    private boolean itemActive = true;
    /** Wall */
    private static final float[] WALL1 = { -2.0f, 10.5f, 2.0f, 10.5f, 2.0f,  9.5f,  -2.0f,  9.5f };
    private static final float[] WALL2 = { -0.5f, 5.0f, 0.5f, 5.0f, 0.5f,  0.0f,  -0.5f,  0.0f };
    /** Wall for screen edge */
    private static final float[] VERT_WALL = { -0.5f, 18.0f, 0.5f, 18.0f, 0.5f,  0.0f,  -0.5f,  0.0f };
    private static final float[] HORI_WALL = { 0.0f, 0.5f, 32.0f, 0.5f, 32.0f,  -0.5f,  0.0f,  -0.5f };

    /** Density of objects */
    private static final float BASIC_DENSITY   = 0.0f;
    /** Friction of objects */
    private static final float BASIC_FRICTION  = 1f;
    /** Collision restitution for all objects */
    private static final float BASIC_RESTITUTION = 0f;

    private static final float PUSH_IMPULSE = 200f;
    /** Load all assets necessary for the level onto an asset manager */
    public void preLoadContent(AssetManager manager) {
        manager.load(PLAYER1_TEXTURE, Texture.class);
        assets.add(PLAYER1_TEXTURE);
        manager.load(PLAYER2_TEXTURE, Texture.class);
        assets.add(PLAYER2_TEXTURE);
        manager.load(PLAYER_WITH_ITEM_TEXTURE, Texture.class);
        assets.add(PLAYER_WITH_ITEM_TEXTURE);
        manager.load(ITEM_TEXTURE, Texture.class);
        assets.add(ITEM_TEXTURE);
        super.preLoadContent(manager);
    }

    /** Create textures */
    public void loadContent(AssetManager manager) {
        player1Texture = createTexture(manager,PLAYER1_TEXTURE,false);
        player2Texture = createTexture(manager, PLAYER2_TEXTURE, false);
        ballItemTexture = createTexture(manager, PLAYER_WITH_ITEM_TEXTURE, false);
        itemTexture = createTexture(manager, ITEM_TEXTURE, false);
        super.loadContent(manager);
    }

    public BallController() {
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
        world.setGravity(new Vector2(0, 0));
    }

    public void reset() {
        Vector2 gravity = new Vector2( world.getGravity() );

        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity,false);
        world.setContactListener(this);
        setComplete(false);
        setFailure(false);
        populateLevel();
    }

    private void populateLevel() {
        /* Add holes */
        PolygonObstacle obj;
        obj = new HoleModel(WALL1, 16, 5);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(goalTile);
        addObject(obj);

        obj = new HoleModel(WALL2, 2, 4);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(goalTile);
        addObject(obj);

        obj = new HoleModel(WALL2, 30, 4);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(goalTile);
        addObject(obj);

        /* Add walls */
        obj = new PolygonObstacle(WALL2, 9.5f, 8);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addObject(obj);

        obj = new PolygonObstacle(WALL2, 22.5f, 8);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall2");
        addObject(obj);

        BoxObstacle wall;
        float ddwidth  = standTile.getRegionWidth()/scale.x;
        float ddheight = standTile.getRegionHeight()/scale.y;
        wall = new BoxObstacle(16, 3, ddwidth, ddheight);
        wall.setDensity(BASIC_DENSITY);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setDrawScale(scale);
        wall.setTexture(standTile);
        wall.setName("wall3");
        addObject(wall);

        /* Add screen edges */

        // left screen edge
        obj = new PolygonObstacle(VERT_WALL, 32.5f, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addObject(obj);

        // right screen edge
        obj = new PolygonObstacle(VERT_WALL, -0.5f, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addObject(obj);

        // top screen edge
        obj = new PolygonObstacle(HORI_WALL, 0.0f, -0.5f);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addObject(obj);

        // bottom screen edge
        obj = new PolygonObstacle(HORI_WALL, 0.0f, 18.5f);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addObject(obj);

        /* Add items */
        float itemWidth  = itemTexture.getRegionWidth()/scale.x;
        float itemHeight = itemTexture.getRegionHeight()/scale.y;
        item = new BoxObstacle(item_position.x, item_position.y, itemWidth, itemHeight);
        item.setDrawScale(scale);
        item.setTexture(itemTexture);
        item.setName("item");
        item.setSensor(true);
        addObject(item);

        /* Add players */
        // Team A
        float pWidth = player1Texture.getRegionWidth() / scale.x;
        float pHeight = player1Texture.getRegionHeight() / scale.y;
        p1 = new BallModel(p1_position.x, p1_position.y, pWidth, pHeight, "a");
        p1.setDrawScale(scale);
        p1.setTexture(player1Texture);

        /* Add home stalls */
        // Team A
        HomeModel home = new HomeModel(p1.getHome_loc().x, p1.getHome_loc().y, 2.5f, 2.5f, "a");
        home.setBodyType(BodyDef.BodyType.StaticBody);
        home.setDrawScale(scale);
        home.setTexture(standTile);
        home.setName("homeA");
        addObject(home);
        addObject(p1);

        /** Add players */
        // Team B
        p2 = new BallModel(p2_position.x, p2_position.y, pWidth, pHeight, "b");
        p2.setDrawScale(scale);
        p2.setTexture(player2Texture);

        /** Add home stalls */
        // Team B
        home = new HomeModel(p2.getHome_loc().x, p2.getHome_loc().y, 2.5f, 2.5f, "b");
        home.setBodyType(BodyDef.BodyType.StaticBody);
        home.setDrawScale(scale);
        home.setTexture(standTile);
        home.setName("homeB");
        addObject(home);
        addObject(p2);
    }

    public void update(float dt) {
        /* Player 1 */
        float p1_horizontal = InputController.getInstance().getHorizontalA();
        float p1_vertical = InputController.getInstance().getVerticalA();
        boolean p1_didBoost = InputController.getInstance().didBoostA();

        // If player initiated movement, set moveState to WALK
        if (p1_horizontal != 0 || p1_vertical != 0) {
            p1.setWalk();
    } else {
            p1.setStatic();
        }
        // Set player movement impulse
        p1.setIX(p1_horizontal);
        p1.setIY(p1_vertical);
        // If player dashed whiled moving, set boost impulse
        if (p1_didBoost && (p1_horizontal != 0 || p1_vertical != 0)) {
            p1.setBoostImpulse(p1_horizontal, p1_vertical);
        }
        p1.applyImpulse();

        /* Player 2 */
        float p2_horizontal = InputController.getInstance().getHorizontalB();
        float p2_vertical = InputController.getInstance().getVerticalB();
        boolean p2_didBoost = InputController.getInstance().didBoostB();

        // If player initiated movement, set moveState to WALK
        if (p2_horizontal!= 0 || p2_vertical != 0) {
            p2.setWalk();
        } else {
            p2.setStatic();
        }
        // Set player movement impulse
        p2.setIX(p2_horizontal);
        p2.setIY(p2_vertical);
        // If player dashed whiled moving, set boost impulse
        if (p2_didBoost && (p2_horizontal!= 0 || p2_vertical != 0)) {
            p2.setBoostImpulse(p2_horizontal, p2_vertical);
        }
        p2.applyImpulse();

        /* Play state */
        if (!p1.isAlive()) { p1.respawn(); }
        if (!p2.isAlive()) { p2.respawn(); }
        p1.setActive(p1.isAlive());
        p2.setActive(p2.isAlive());

        /* Item */
        if (p1.item) {
            item.setPosition(p1.getX(), p1.getY() + 1f);
        }
        if (p2.item) {
            item.setPosition(p2.getX(), p2.getY() + 1f);
        }
        if (!itemActive && ! p1.item && !p2.item) { addItem(item_position); }
        if (!itemActive) { removeItem(); }

        /* Player cooldown */
        p1.cooldown();
        p2.cooldown();
    }

    public void handlePlayerToObjectContact(BallModel player, Object object) {
        if (object instanceof HoleModel) { // Player-Hole
            player.setAlive(false);
            player.draw = false;
        } else if (object instanceof BoxObstacle && ((BoxObstacle) object).getName().equals("item")) { // Player-Item
            player.item = true;
            itemActive = false;
        } else if (object instanceof HomeModel ) { // Player-Home
            HomeModel homeObject = (HomeModel) object;
            // If players went to their own home, drop off item and increment score
            if (player.getTeam().equals(homeObject.getTeam()) && player.item) {
                homeObject.incrementScore();
                player.item = false;
                player.resetTexture();
                if (homeObject.getScore() >= ITEMS_TO_WIN) {
                    setComplete(true);
                    if (homeObject.getTeam().equals("a")) {
                        winner = "PLAYER B ";
                    } else if (homeObject.getTeam().equals("b")) {
                        winner = "PLAYER A ";
                    }
                }
            }
        }
    }

    public void beginContact(Contact contact) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();

        // Player-Object Contact
        if (a instanceof BallModel) {
            handlePlayerToObjectContact((BallModel) a, b);
        } else if (b instanceof BallModel) {
            handlePlayerToObjectContact((BallModel) b, a);
        }
    }

    public void endContact(Contact contact) {
    }

    public void postSolve(Contact contact, ContactImpulse impulse) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();

        // Player-Player Contact
        if (a instanceof BallModel && b instanceof BallModel) {
            BallModel playerA = (BallModel) a;
            BallModel playerB = (BallModel) b;

            Vector2 flyDirection;
            if (playerA.state == BallModel.MoveState.RUN &&
                    (playerB.state == BallModel.MoveState.WALK || playerB.state == BallModel.MoveState.STATIC)) {
                flyDirection = playerB.getLinearVelocity().nor();
                playerA.resetBoosting();
                playerB.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerB.getPosition(), true);
            } else if (playerB.state == BallModel.MoveState.RUN &&
                    (playerA.state == BallModel.MoveState.WALK || playerA.state == BallModel.MoveState.STATIC)) {
                flyDirection = playerA.getLinearVelocity().nor();
                playerA.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerA.getPosition(), true);
                playerB.resetBoosting();
            } else if (playerB.state == BallModel.MoveState.RUN &&
                    (playerA.state == BallModel.MoveState.RUN)) {
                flyDirection = playerA.getLinearVelocity().nor();
                playerA.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerA.getPosition(), true);
                flyDirection = playerB.getLinearVelocity().nor();
                playerA.resetBoosting();
                playerB.resetBoosting();
                playerB.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerB.getPosition(), true);
            }
        }
    }

    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    private void removeItem() {
        item.setActive(false);
    }

    private void addItem(Vector2 position) {
        item.draw = true;
        itemActive = true;
        item.setActive(true);
        item.setPosition(position);
    }


}
