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

    /** Reference to the ball texture */
    private static final String BALL_TEXTURE = "ball/ballSprite.png";
    private static final String BALLITEM_TEXTURE = "ball/ballItem.png";
    /** Texture assets for the ball */
    private TextureRegion ballTexture;
    private TextureRegion ballItemTexture;
    private TextureRegion itemTexture;

    /** Ball 1's position */
    private static Vector2 BALL_POS_1 = new Vector2(26, 3);
    /** Ball 2's position */
    private static Vector2 BALL_POS_2 = new Vector2(6, 3);
    /** Item's position */
    private static Vector2 ITEM_POS = new Vector2(16, 12);

    /** Reference to the ball/player avatar */
    private BallModel ballA;
    /** Reference to the ball/player avatar */
    private BallModel ballB;

    private BoxObstacle item;
    private boolean itemActive = true;

    /** Density of objects */
    private static final float BASIC_DENSITY   = 0.0f;
    /** Friction of objects */
    private static final float BASIC_FRICTION  = 1f;
    /** Collision restitution for all objects */
    private static final float BASIC_RESTITUTION = 0f;

    private static final float[] WALL1 = { -2.0f, 10.5f, 2.0f, 10.5f,
            2.0f,  9.5f,  -2.0f,  9.5f };
    private static final float[] WALL2 = { -0.5f, 5.0f, 0.5f, 5.0f,
            0.5f,  0.0f,  -0.5f,  0.0f };

    public void preLoadContent(AssetManager manager) {
        manager.load(BALL_TEXTURE, Texture.class);
        assets.add(BALL_TEXTURE);
        manager.load(BALLITEM_TEXTURE, Texture.class);
        assets.add(BALLITEM_TEXTURE);
        manager.load("item/item.png", Texture.class);
        assets.add("item/item.png");
        super.preLoadContent(manager);
    }

    public void loadContent(AssetManager manager) {
        ballTexture = createTexture(manager,BALL_TEXTURE,false);
        ballItemTexture = createTexture(manager, BALLITEM_TEXTURE, false);
        itemTexture = createTexture(manager, "item/item.png", false);
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

        // adds a hole
        PolygonObstacle obj;
        obj = new HoleModel(WALL1, 16, 5);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(goalTile);
        addObject(obj);

        // adds a hole
        obj = new HoleModel(WALL2, 2, 4);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(goalTile);
        addObject(obj);

        // adds a hole
        obj = new HoleModel(WALL2, 30, 4);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(goalTile);
        addObject(obj);

        // add an obstacle
        obj = new PolygonObstacle(WALL2, 9.5f, 8);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall2");
        addObject(obj);

        // add an obstacle
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

        // add item
        ddwidth  = itemTexture.getRegionWidth()/scale.x;
        ddheight = itemTexture.getRegionHeight()/scale.y;
        item = new BoxObstacle(ITEM_POS.x, ITEM_POS.y, ddwidth, ddheight);
        item.setDrawScale(scale);
        item.setTexture(itemTexture);
        item.setName("item");
        item.setSensor(true);
        addObject(item);

        // add ball
        float dwidth = ballTexture.getRegionWidth() / scale.x;
        float dheight = ballTexture.getRegionHeight() / scale.y;
        ballA = new BallModel(BALL_POS_1.x, BALL_POS_1.y, dwidth, dheight, "a");
        ballA.setDrawScale(scale);
        ballA.setTexture(ballTexture);
        addObject(ballA);

        // add ballA home
        HomeModel obj1 = new HomeModel(ballA.getHome_loc().x, ballA.getHome_loc().y, 2.5f, 2.5f, "a");
        obj1.setBodyType(BodyDef.BodyType.StaticBody);
        obj1.setDrawScale(scale);
        obj1.setTexture(standTile);
        obj1.setName("homeA");
        addObject(obj1);

        // add ball B
        ballB = new BallModel(BALL_POS_2.x, BALL_POS_2.y, dwidth, dheight, "b");
        ballB.setDrawScale(scale);
        ballB.setTexture(ballTexture);
        addObject(ballB);

        // add ballB home
        obj1 = new HomeModel(ballB.getHome_loc().x, ballB.getHome_loc().y, 2.5f, 2.5f, "b");
        obj1.setBodyType(BodyDef.BodyType.StaticBody);
        obj1.setDrawScale(scale);
        obj1.setTexture(standTile);
        obj1.setName("homeB");
        addObject(obj1);
    }

    public void update(float dt) {
        if (InputController.getInstance().getHorizontalA()!= 0 || InputController.getInstance().getVerticalA() != 0) {
            ballA.setWalk();
        } else {
            ballA.setStatic();
        }
        ballA.setIX(InputController.getInstance().getHorizontalA());
        ballA.setIY(InputController.getInstance().getVerticalA());
        if (InputController.getInstance().didBoostA() && (InputController.getInstance().getHorizontalA()!= 0 || InputController.getInstance().getVerticalA() != 0)) {
            ballA.setBoostImpulse(InputController.getInstance().getHorizontalA(), InputController.getInstance().getVerticalA());
        }
        ballA.applyImpulse();

        if (InputController.getInstance().getHorizontalB()!= 0 || InputController.getInstance().getVerticalB() != 0) {
            ballB.setWalk();
        } else {
            ballB.setStatic();
        }
        ballB.setIX(InputController.getInstance().getHorizontalB());
        ballB.setIY(InputController.getInstance().getVerticalB());
        if (InputController.getInstance().didBoostB() && (InputController.getInstance().getHorizontalB()!= 0 || InputController.getInstance().getVerticalB() != 0)) {
            ballB.setBoostImpulse(InputController.getInstance().getHorizontalB(), InputController.getInstance().getVerticalB());
        }
        ballB.applyImpulse();

        if (!ballA.isAlive()) {
            ballA.respawn();
        }
        if (!ballB.isAlive()) {
            ballB.respawn();
        }
        ballA.setActive(ballA.isAlive());
        ballB.setActive(ballB.isAlive());

        if (!itemActive && ! ballA.item && !ballB.item) {
            addItem(ITEM_POS);
        }

        if (! itemActive) { removeItem(); }
        ballA.cooldown();
        ballB.cooldown();
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
                ((BallModel) a).setTexture(ballTexture);
            }
        }

        if (a instanceof HomeModel) {
            HomeModel bHome = (HomeModel) a;
            if (b instanceof BallModel && ((BallModel) b).getTeam().equals(bHome.getTeam())) {
                if (((BallModel) b).item) {
                    bHome.incrementScore();
                }
                ((BallModel) b).item = false;
                ((BallModel) b).setTexture(ballTexture);
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
