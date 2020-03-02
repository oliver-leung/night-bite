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
    private static final String PLAYER1_TEXTURE = "ball/char1.png";
    private static final String PLAYER2_TEXTURE = "ball/char2-f1.png";
    private static final String PLAYER_WITH_ITEM_TEXTURE = "ball/ballItem.png";
    private static final String ITEM_TEXTURE = "ball/fish.png";

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

    /** Density of objects */
    private static final float BASIC_DENSITY   = 0.0f;
    /** Friction of objects */
    private static final float BASIC_FRICTION  = 1f;
    /** Collision restitution for all objects */
    private static final float BASIC_RESTITUTION = 0f;

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
        /** Add holes */
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

        /** Add walls */
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

        // add an obstacle
        BoxObstacle wall;
        float ddwidth  = standTile.getRegionWidth()/scale.x;
        float ddheight = standTile.getRegionHeight()/scale.y;
        wall = new BoxObstacle(16, 3, ddwidth, ddheight);
        wall.setDensity(BASIC_DENSITY);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setDrawScale(scale);
        wall.setTexture(standTile);
        wall.setName("wall1");
        addObject(wall);

        /** Add items */
        float itemWidth  = itemTexture.getRegionWidth()/scale.x;
        float itemHeight = itemTexture.getRegionHeight()/scale.y;
        item = new BoxObstacle(item_position.x, item_position.y, itemWidth, itemHeight);
        item.setDrawScale(scale);
        item.setTexture(itemTexture);
        item.setName("item");
        item.setSensor(true);
        addObject(item);

        /** Add players */
        // Team A
        float pWidth = player1Texture.getRegionWidth() / scale.x;
        float pHeight = player1Texture.getRegionHeight() / scale.y;
        p1 = new BallModel(p1_position.x, p1_position.y, pWidth, pHeight, "a");
        p1.setDrawScale(scale);
        p1.setTexture(player1Texture);
        addObject(p1);

        // Team B
        p2 = new BallModel(p2_position.x, p2_position.y, pWidth, pHeight, "b");
        p2.setDrawScale(scale);
        p2.setTexture(player2Texture);
        addObject(p2);

        /** Add home stalls */
        // Team A
        HomeModel home = new HomeModel(p1.getHome_loc().x, p1.getHome_loc().y, 2.5f, 2.5f, "a");
        home.setBodyType(BodyDef.BodyType.StaticBody);
        home.setDrawScale(scale);
        home.setTexture(standTile);
        home.setName("homeA");
        addObject(home);

        // Team B
        home = new HomeModel(p2.getHome_loc().x, p2.getHome_loc().y, 2.5f, 2.5f, "b");
        home.setBodyType(BodyDef.BodyType.StaticBody);
        home.setDrawScale(scale);
        home.setTexture(standTile);
        home.setName("homeB");
        addObject(home);
    }

    public void update(float dt) {
        if (InputController.getInstance().getHorizontalA()!= 0 || InputController.getInstance().getVerticalA() != 0) {
            p1.setWalk();
        } else {
            p1.setStatic();
        }
        p1.setIX(InputController.getInstance().getHorizontalA());
        p1.setIY(InputController.getInstance().getVerticalA());
        if (InputController.getInstance().didBoostA() && (InputController.getInstance().getHorizontalA()!= 0 || InputController.getInstance().getVerticalA() != 0)) {
            p1.setBoostImpulse(InputController.getInstance().getHorizontalA(), InputController.getInstance().getVerticalA());
        }
        p1.applyImpulse();

        if (InputController.getInstance().getHorizontalB()!= 0 || InputController.getInstance().getVerticalB() != 0) {
            p2.setWalk();
        } else {
            p2.setStatic();
        }
        p2.setIX(InputController.getInstance().getHorizontalB());
        p2.setIY(InputController.getInstance().getVerticalB());
        if (InputController.getInstance().didBoostB() && (InputController.getInstance().getHorizontalB()!= 0 || InputController.getInstance().getVerticalB() != 0)) {
            p2.setBoostImpulse(InputController.getInstance().getHorizontalB(), InputController.getInstance().getVerticalB());
        }
        p2.applyImpulse();

        if (!p1.isAlive()) {
            p1.respawn();
        }
        if (!p2.isAlive()) {
            p2.respawn();
        }
        p1.setActive(p1.isAlive());
        p2.setActive(p2.isAlive());

        if (!itemActive && ! p1.item && !p2.item) {
            addItem(item_position);
        }

        if (! itemActive) { removeItem(); }
        p1.cooldown();
        p2.cooldown();
    }

    public void beginContact(Contact contact) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();

        if (a instanceof HoleModel) {
            if (b instanceof BallModel) {
                ((BallModel) b).setAlive(false);
                ((BallModel) b).draw = false;
            }
            return;
        }

        if (b instanceof HoleModel) {
            if (a instanceof BallModel) {
                ((BallModel) a).setAlive(false);
                ((BallModel) a).draw = false;
            }
            return;
        }

        if (a instanceof BoxObstacle && ((BoxObstacle) a).getName().equals("item")) {
            if (b instanceof BallModel) {
                ((BallModel) b).item = true;
                ((BallModel) b).setTexture(ballItemTexture);
                itemActive = false;
            }
        }

        if (b instanceof BoxObstacle && ((BoxObstacle) b).getName().equals("item")) {
            if (a instanceof BallModel) {
                ((BallModel) a).item = true;
                ((BallModel) a).setTexture(ballItemTexture);
                itemActive = false;
            }
        }

        if (b instanceof HomeModel) {
            HomeModel bHome = (HomeModel) b;
            if (a instanceof BallModel && ((BallModel) a).getTeam().equals(bHome.getTeam())) {
                if (((BallModel) a).item) {
                    bHome.incrementScore();
                }
                ((BallModel) a).item = false;
                ((BallModel) a).resetTexture();
            }
        }

        if (a instanceof HomeModel) {
            HomeModel bHome = (HomeModel) a;
            if (b instanceof BallModel && ((BallModel) b).getTeam().equals(bHome.getTeam())) {
                if (((BallModel) b).item) {
                    bHome.incrementScore();
                }
                ((BallModel) b).item = false;
                ((BallModel) b).setTexture(player2Texture);
            }
        }
    }

    public void endContact(Contact contact) {
    }

    public void postSolve(Contact contact, ContactImpulse impulse) {
        Object a = contact.getFixtureA().getBody().getUserData();
        Object b = contact.getFixtureB().getBody().getUserData();
        if (a instanceof BallModel && b instanceof BallModel) {
            Vector2 flyDirection = null;
            if (((BallModel) a).state == BallModel.MoveState.RUN &&
                    (((BallModel) b).state == BallModel.MoveState.WALK || ((BallModel) b).state == BallModel.MoveState.STATIC)) {
                flyDirection = ((BallModel) b).getLinearVelocity().nor();
                ((BallModel) a).resetBoosting();
                ((BallModel) b).getBody().applyLinearImpulse(flyDirection.scl(3000), ((BallModel) b).getPosition(), true);
            } else if (((BallModel) b).state == BallModel.MoveState.RUN &&
                    (((BallModel) a).state == BallModel.MoveState.WALK || ((BallModel) a).state == BallModel.MoveState.STATIC)) {
                flyDirection = ((BallModel) a).getLinearVelocity().nor();
                ((BallModel) a).getBody().applyLinearImpulse(flyDirection.scl(3000), ((BallModel) a).getPosition(), true);
                ((BallModel) b).resetBoosting();
            } else if (((BallModel) b).state == BallModel.MoveState.RUN &&
                    (((BallModel) a).state == BallModel.MoveState.RUN)) {
                flyDirection = ((BallModel) a).getLinearVelocity().nor();
                ((BallModel) a).getBody().applyLinearImpulse(flyDirection.scl(3000), ((BallModel) a).getPosition(), true);
                flyDirection = ((BallModel) b).getLinearVelocity().nor();
                ((BallModel) a).resetBoosting();
                ((BallModel) b).resetBoosting();
                ((BallModel) b).getBody().applyLinearImpulse(flyDirection.scl(3000), ((BallModel) b).getPosition(), true);
            }
        }
    }

    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    private void removeItem() {
        item.draw = false;
        item.setActive(false);
    }

    private void addItem(Vector2 position) {
        item.draw = true;
        itemActive = true;
        item.setActive(true);
        item.setPosition(position);
    }


}
