package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.ArrayList;

import static edu.cornell.gdiac.nightbite.entity.MovableModel.*;

public class PlayerModel extends HumanoidModel {

    // TODO
    private int NUM_ITEMS = 2;

    /**
     * player movement params
     */
    private static final float DEFAULT_THRUST = 10.0f;
    private static final float BOOST_IMP = 100.0f;
    private static final float MOTION_DAMPING = 25f;

    private static final int BOOST_FRAMES = 20;
    private static final int COOLDOWN_FRAMES = 35;

    private final TextureRegion idleTexture = Assets.TEXTURES.get("character/P1_64.png");

    public MoveState state;
    private int boosting;
    private int cooldown;

    private float prevHoriDir;
    private int ticks;

    private Vector2 impulse;
    private Vector2 boost;

    /** player respawn */
    private boolean isAlive;
    private int spawnCooldown;

    /** cooldown for grabbing and throwing items */
    private int grabCooldown;
    private static int GRAB_COOLDOWN_PERIOD = 15;

    /** player identification */
    private String team;

    private Vector2 homeLoc;

    /** player-item */
    private ArrayList<ItemModel> item;
    private ArrayList<Boolean> overlapItem;

    /** player texture */
    private FilmStrip defaultTexture;

    private TextureRegion currentTexture;

    public PlayerModel(float x, float y, String playerTeam) {
        super(x, y, 1, 1);
        setBullet(true);
        setName("ball");
        setTexture(texture);

        impulse = new Vector2();
        boost = new Vector2();

        prevHoriDir = -1;
        ticks = 0;

        cooldown = 0;
        boosting = 0;

        isAlive = true;
        item = new ArrayList<>();
        overlapItem = new ArrayList<>();
        for (int i = 0; i < NUM_ITEMS; i++) {
            overlapItem.add(false);
        }
        // TODO Set this later on in a better way
        homeLoc = new Vector2(x - 0.5f, y - 0.5f);
        team = playerTeam;
        setDensity(MOVABLE_OBJ_DENSITY);
        setFriction(MOVABLE_OBJ_FRICTION);
        setRestitution(MOVABLE_OBJ_RESTITUTION);
    }

    public int getTicks() {
        return ticks;
    }

    public void resetTexture() {
        texture = defaultTexture;
    }

    public FilmStrip getTexture() {
        return (FilmStrip) texture;
    }

    public void setTexture(FilmStrip value) {
        if (defaultTexture == null) {
            defaultTexture = value;
        }
        super.setTexture(value);
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
        // Filter f = cap1.getFilterData();
        // f.groupIndex = -1;
        // cap1.setFilterData(f);
        // core.setFilterData(f);
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

    public float getPrevHoriDir() {
        return prevHoriDir;
    }

    public void setPrevHoriDir(float dir) {
        prevHoriDir = dir;
    }

    public void incrPlayerWalkCounter() {
        ticks++;
    }

    public void resetPlayerWalkCounter() {
        ticks = 0;
    }

    public enum MoveState {
        WALK,
        RUN,
        STATIC,
        DYING
    }

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
        updateGrabCooldown();
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
        return item.size() > 0;
    }

    public int numCarriedItems() {
        return item.size();
    }

    public ArrayList<ItemModel> getItems() {
        return item;
    }

    public void clearInventory() {
        item.clear();
    }

    public void unholdItem(ItemModel i) {
        item.remove(i);
    }

    public void holdItem(ItemModel i) {
        item.add(i);
    }

    /** cooldown between grabbing/throwing */

    public void startgrabCooldown() {
        grabCooldown = GRAB_COOLDOWN_PERIOD;
    }

    private void updateGrabCooldown() {
        if (grabCooldown > 0) {
            grabCooldown -= 1;
        }
    }

    public boolean grabCooldownOver() {
        return grabCooldown == 0;
    }
}

