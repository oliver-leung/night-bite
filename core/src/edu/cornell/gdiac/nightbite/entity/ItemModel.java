package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;

public class ItemModel extends BoxObstacle {

    public static TextureRegion itemTexture;

    private float respawning;
    private boolean held;

    public boolean throw_item;
    private float prev_x;
    private float prev_y;
    private float scale = 0.001f;

    private int itemCooldown; // used during item grab and item respawn
    private static int ITEM_COOLDOWN_PERIOD = 15;

    private int RESPAWN_TIME = 150;

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

        held = false;
    }

//    @Override
//    protected void defineFixtures() {
//        super.defineFixtures();
//        fixture.filter.categoryBits = 0x002;
//        fixture.filter.maskBits = 0x004;
//    }

    public void throwItem(Vector2 impulse) {
        getBody().applyLinearImpulse(impulse.scl(THROW_FORCE), getPosition(), true);
    }

    // manage cooldown
    public void startCooldown() {
        itemCooldown = ITEM_COOLDOWN_PERIOD;
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
    public void startRespawning() {
        respawning = RESPAWN_TIME;
    }

    public void updateRespawning() {
        respawning -= 1;
    }

    public boolean getRespawning() {
        return respawning == 0;
    }

    // held
    public boolean getHeldStatus() {
        return held;
    }

    public void setHeldStatus(boolean b) {
        held = b;
    }

    /*
    * Throw item goes through two stages: thrown -> throw_item is true -> velocity check -> throw_item is false
    * During these two stages collisions are turned on.
    * */

    public void setThrow(boolean b) {
        prev_x = -2000; // TODO fix with max_int or something
        prev_y = -2000;
        throw_item = b;
    }

    public boolean getThrow() {
        return throw_item;
    }

    public boolean checkStopped() {
        float curr_x = Math.round(getX() / scale) * scale;
        float curr_y = Math.round(getY() / scale) * scale;
        System.out.println(curr_x == prev_x && curr_y == prev_y);
        System.out.println(curr_x);
        System.out.println(prev_x);
        System.out.println("--------");
        if (curr_x == prev_x && curr_y == prev_y) {
            setThrow(false);
            setSensor(true);
            return true;
        }
        prev_x = curr_x;
        prev_y = curr_y;
        return false;
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
