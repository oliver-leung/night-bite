package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;

public class ItemModel extends BoxObstacle {

    /**
     * item-player
     */
    public PlayerModel holdingPlayer;
    public PlayerModel lastTouch;

    /**
     * item parameters
     */
    private Vector2 item_init_position;

    /**
     * item respawn
     */
    private float respawn;
    private int RESPAWN_TIME = 150;

    /**
     * throwing configs
     */
    private float THROW_FORCE = 10f;
    private float MOTION_DAMPING = 40f;
    /**
     * item identification
     */
    private int id;

    // TODO temp
    private static final float MOVABLE_OBJECT_DENSITY = 1.0f;
    private static final float MOVABLE_OBJECT_FRICTION = 0.1f;
    private static final float MOVABLE_OBJECT_RESTITUTION = 0.4f;

    public ItemModel(float x, float y, int itemId, TextureRegion itemTexture) {
        super(x, y, 1, 1);
        setTexture(itemTexture);

        setTexture(itemTexture);
        setDensity(MOVABLE_OBJECT_DENSITY);
        setFriction(MOVABLE_OBJECT_FRICTION);
        setRestitution(MOVABLE_OBJECT_RESTITUTION);

        setSensor(false);
        setBullet(true);
        setName("item");

        item_init_position = new Vector2(x + 0.5f, y + 0.5f);  // this is mad sus
        id = itemId;

        maskBits = 0x0002 | 0x0008;
        categoryBits = 0x0001;
    }

    public void update(float dt) {
        super.update(dt);

        respawn -= 1;
        if (respawn == 0) {
            addItem(item_init_position);
        }
    }

    /** item identification */
    public int getId() {
        return id;
    }

    /** respawn */

    public void startRespawn() {
        draw = false;
        holdingPlayer = null;
        respawn = RESPAWN_TIME;
    }

    private void addItem(Vector2 position) {
        draw = true;
        holdingPlayer = null;
        setPosition(position);
    }

    /** item held */

    public void setHeld(PlayerModel p) {
        p.holdItem(this);
        holdingPlayer = p;
        lastTouch = p;
    }

    public boolean isHeld() {
        return holdingPlayer != null;
    }

    /** throw item */
    public void throwItem(Vector2 playerPosition, Vector2 impulse) {
        setPosition(playerPosition);
        getBody().applyLinearImpulse(impulse.scl(THROW_FORCE), getPosition(), true);
        holdingPlayer = null;
    }

    /** physics */
    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setLinearDamping(MOTION_DAMPING);
        body.setFixedRotation(true);
        Filter f = geometry.getFilterData();
        f.groupIndex = 1;
        geometry.setFilterData(f);
        return true;
    }

    public boolean isDead() {
        return respawn > 0;
    }

    public float getBottom() {
        if (isHeld()) {
            return holdingPlayer.getBottom();
        }
        return super.getBottom();
    }
}
