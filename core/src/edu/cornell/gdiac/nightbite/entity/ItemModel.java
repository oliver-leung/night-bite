package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
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
    private float THROW_FORCE = 500f;
    private float MOTION_DAMPING = 25f;
    /**
     * item identification
     * */
    private int id;


    public ItemModel(float x, float y, float width, float height, int itemId, TextureRegion itemTexture) {
        super(x, y, width, height);
        setTexture(itemTexture);
        setSensor(false);
        setBullet(true);
        setName("item");

        item_init_position = new Vector2(x, y);
        id = itemId;
    }

    public void update() {
        updateRespawn();
    }

    /** item identification */
    public int getId() {
        return id;
    }

    /** respawn */

    public void startRespawn() {
        holdingPlayer = null;
        respawn = RESPAWN_TIME;
        draw = false;
    }

    public void updateRespawn() {
        respawn -= 1;
        if (respawn == 0) {
            addItem(item_init_position);
        }
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
    public void throwItem(Vector2 impulse) {
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
        return true;
    }

}
