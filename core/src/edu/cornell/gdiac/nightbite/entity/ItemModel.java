package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;

public class ItemModel extends BoxObstacle {

    public static TextureRegion itemTexture;

    private boolean respawning;

    private int itemCooldown; // used during item grab and item respawn
    private static int COOLDOWN_PERIOD = 25;

    private float THROW_FORCE = 500f;

    // physics config
    private static final float DEFAULT_DENSITY = 1.0f;
    private static final float DEFAULT_FRICTION = 0.1f;
    private static final float DEFAULT_RESTITUTION = 0.4f;
    private float MOTION_DAMPING = 25f;


    public ItemModel(float x, float y, float width, float height) {
        super(x, y, width, height);
        setDensity(DEFAULT_DENSITY);
        setFriction(DEFAULT_FRICTION);
        setRestitution(DEFAULT_RESTITUTION);
        setName("item");
        setBullet(true);
    }

    public void throwItem(Vector2 impulse) {
        getBody().applyLinearImpulse(impulse.scl(THROW_FORCE), getPosition(), true);
    }

    // manage cooldown
    public void startCooldown() {
        itemCooldown = COOLDOWN_PERIOD;
    }

    public void updateCooldown() {
        if (itemCooldown > 0) {
            itemCooldown -= 1;
        }
    }

    public boolean cooldownStatus() {
        return itemCooldown == 0;
    }

    // respawn
    public void setRespawning(boolean b) {
        respawning = b;
    }

    public boolean getRespawning() {
        return respawning;
    }

    // add physics
    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setLinearDamping(MOTION_DAMPING);
        body.setFixedRotation(true);
        return true;
    }
}
