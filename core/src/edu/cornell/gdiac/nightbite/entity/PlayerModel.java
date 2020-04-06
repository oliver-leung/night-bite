package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static edu.cornell.gdiac.nightbite.entity.MovableModel.*;


public class PlayerModel extends CapsuleObstacle {

    // TODO
    private int NUM_ITEMS = 2;

    /**
     * player movement params
     */
    private static final float DEFAULT_THRUST = 10.0f;
    private static final float BOOST_IMP = 100.0f;
    private static final float MOTION_DAMPING = 25f;

    private static final int BOOST_FRAMES = 20;
    private static final int COOLDOWN_FRAMES = 60;

    public enum MoveState {
        WALK,
        RUN,
        STATIC
    }

    public MoveState state;
    private int boosting;
    private int cooldown;

    private Vector2 impulse;
    private Vector2 boost;

    /** player respawn */
    private boolean isAlive;
    private int spawnCooldown;

    /** player identification */
    private String team;
    private Vector2 homeLoc;

    /** player-item */
    public ArrayList<Boolean> item;
    private ArrayList<Boolean> overlapItem;

    /** player texture */
    public final FilmStrip playerTexture;
    private FilmStrip defaultTexture;

    public void setTexture(FilmStrip value) {
        if (defaultTexture == null) {
            defaultTexture = value;
        }
        super.setTexture(value);
    }

    public void resetTexture() { texture = defaultTexture;
    }

    public PlayerModel(float x, float y, float width, float height, FilmStrip texture, String playerTeam) {
        super(2 * x + 1f, 2 * y + 1f, width, height);
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
        item = new ArrayList<Boolean>();
        overlapItem = new ArrayList<Boolean>();
        for (int i = 0; i < NUM_ITEMS; i++) {
            item.add(false);
            overlapItem.add(false);
        }

        homeLoc = new Vector2(2 * x + 1f, 2 * y + 1f);
        team = playerTeam;
        setDensity(MOVABLE_OBJ_DENSITY);
        setFriction(MOVABLE_OBJ_FRICTION);
        setRestitution(MOVABLE_OBJ_RESTITUTION);
    }

    /** player identification */
    public String getTeam() {
        return team;
    }

    public Vector2 getHomeLoc() {
        return homeLoc;
    }

    /** physics */
    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setLinearDamping(MOTION_DAMPING);
        body.setFixedRotation(true);
        return true;
    }

    /** player movement */

    public Vector2 getImpulse() { return impulse; }

    public void setIX(float value) { impulse.x = value; }

    public void setIY(float value) { impulse.y = value; }

    public void setBoostImpulse(float hori, float vert) {
        if (cooldown > 0 || hasItem()) { return; }
        state = MoveState.RUN;
        boosting = BOOST_FRAMES;
        cooldown = COOLDOWN_FRAMES;
        boost.x = hori;
        boost.y = vert;
    }

    public void applyImpulse() {
        if (!isActive()) {
            return;
        }
        body.applyLinearImpulse(impulse.nor().scl(DEFAULT_THRUST).add(boost.nor().scl(BOOST_IMP)), getPosition(), true);
        boost.setZero();
    }

    /** movement state */
    public void setWalk() {
        if (boosting > 0) { return; }
        state = MoveState.WALK;
    }

    public void setStatic() {
        if (boosting > 0) { return; }
        state = MoveState.STATIC;
    }

    public void update() {
        cooldown = Math.max(0, cooldown - 1);
        boosting = Math.max(0, boosting - 1);
    }

    public void resetBoosting() {
        boosting = 0;
    }

    /** player respawn */
    public boolean isAlive() {
        return isAlive;
    }

    public void setDead() {
        isAlive = false;
        draw = false;
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
        Collections.fill(item, false);
        resetTexture();

        setLinearVelocity(Vector2.Zero);
    }

    /** player-item */
    public void setOverlapItem(int id, boolean b) {
        overlapItem.set(id, b);
    }

    public boolean getOverlapItem(int id) {
        return overlapItem.get(id);
    }

    public boolean hasItem() {
        return item.contains(true);
    }

    public ArrayList<Integer> getItemId() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < item.size(); i++) {
            if (item.get(i)) {
                ids.add(i);
            }
        }
        return ids;
    }
}

