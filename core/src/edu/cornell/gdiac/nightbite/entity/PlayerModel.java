package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.util.FilmStrip;

import static edu.cornell.gdiac.nightbite.WorldModel.*;


public class PlayerModel extends CapsuleObstacle {

    /**
     * player movement params
     */
    private static final float DEFAULT_THRUST = 10.0f;
    private static final float BOOST_IMP = 100.0f;
    private static final float MOTION_DAMPING = 25f;

    private static final int BOOST_FRAMES = 20;
    private static final int COOLDOWN_FRAMES = 60;
    /**
     * player texture
     */
    public final FilmStrip playerTexture;

    public MoveState state;
    private int boosting;
    private int cooldown;

    private Vector2 impulse;
    private Vector2 boost;
    /**
     * player-item
     */
    public boolean item;
    /**
     * player respawn
     */
    private boolean isAlive;
    private int spawnCooldown;
    /**
     * player identification
     */
    private String team;
    private Vector2 homeLoc;
    private boolean overlapItem;
    private TextureRegion defaultTexture;

    public PlayerModel(float x, float y, float width, float height, FilmStrip texture, String playerTeam) {
        super(x, y, width, height);
        setOrientation(Orientation.VERTICAL);
        setBullet(true);
        setName("ball");

        playerTexture = texture;
        setTexture(playerTexture);

        impulse = new Vector2();
        boost = new Vector2();

        cooldown = 0;
        boosting = 0;

        isAlive = true;
        overlapItem = false;

        homeLoc = new Vector2(x, y);
        team = playerTeam;

        setDensity(MOVABLE_OBJ_DENSITY);
        setFriction(MOVABLE_OBJ_FRICTION);
        setRestitution(MOVABLE_OBJ_RESTITUTION);
    }

    @Override
    public void setTexture(TextureRegion value) {
        if (defaultTexture == null) {
            defaultTexture = value;
        }
        super.setTexture(value);
    }

    public void resetTexture() {
        texture = defaultTexture;
    }

    /**
     * player identification
     */
    public String getTeam() {
        return team;
    }

    /**
     * physics
     */
    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setLinearDamping(MOTION_DAMPING);
        body.setFixedRotation(true);
        return true;
    }

    public Vector2 getHomeLoc() {
        return homeLoc;
    }

    /**
     * player movement
     */

    public Vector2 getImpulse() {
        return impulse;
    }

    public void setIX(float value) {
        impulse.x = value;
    }

    public void applyImpulse() {
        if (!isActive()) {
            return;
        }
        body.applyLinearImpulse(impulse.nor().scl(DEFAULT_THRUST).add(boost.nor().scl(BOOST_IMP)), getPosition(), true);
        boost.setZero();
    }

    public void setIY(float value) {
        impulse.y = value;
    }

    public void setBoostImpulse(float hori, float vert) {
        if (cooldown > 0 || item) {
            return;
        }
        state = MoveState.RUN;
        boosting = BOOST_FRAMES;
        cooldown = COOLDOWN_FRAMES;
        boost.x = hori;
        boost.y = vert;
    }

    /**
     * movement state
     */
    public void setWalk() {
        if (boosting > 0) {
            return;
        }
        state = MoveState.WALK;
    }

    public void setStatic() {
        if (boosting > 0) {
            return;
        }
        state = MoveState.STATIC;
    }

    public void update() {
        cooldown = Math.max(0, cooldown - 1);
        boosting = Math.max(0, boosting - 1);
    }

    public void resetBoosting() {
        boosting = 0;
    }

    /**
     * player respawn
     */
    public boolean isAlive() {
        return isAlive;
    }

    public void setDead() {
        isAlive = false;
        draw = false;
    }

    /**
     * player-item
     */
    public void setOverlapItem(boolean b) {
        overlapItem = b;
    }

    public void respawn() {
        if (spawnCooldown == 0) {
            spawnCooldown = 60;
        }
        spawnCooldown--;
        if (spawnCooldown == 0) {
            setPosition(homeLoc);
            isAlive = true;
            draw = true;
        }
        item = false;
        resetTexture();

        setLinearVelocity(Vector2.Zero);
    }

    public enum MoveState {
        WALK,
        RUN,
        STATIC
    }

    public boolean getOverlapItem() {
        return overlapItem;
    }
}