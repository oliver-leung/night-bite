package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;

public class ItemModel extends BoxObstacle {

    /**
     * item texture
     */
    public static TextureRegion itemTexture;
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
     * cooldown for grabbing and throwing items
     */
    private int itemCooldown;
    private static int ITEM_COOLDOWN_PERIOD = 15;

    /**
     * throwing configs
     */
    private float THROW_FORCE = 500f;
    private float MOTION_DAMPING = 25f;


    public ItemModel(float x, float y, float width, float height) {
        super(x, y, width, height);
        setTexture(itemTexture);
        setSensor(true);
        setBullet(true);
        setName("item");

        item_init_position = new Vector2(x, y);
    }

    public void update() {
        updateCooldown();
        updateRespawn();
    }

    /** cooldown between grabbing/throwing */

    public void startCooldown() {
        itemCooldown = ITEM_COOLDOWN_PERIOD;
    }

    private void updateCooldown() {
        if (itemCooldown > 0) {
            itemCooldown -= 1;
        }
    }

    public boolean cooldownOver() {
        return itemCooldown == 0;
    }

    /** respawn */

    public void startRespawn() {
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
        setUnheld();
        setPosition(position);
    }

    /** item held */

    public void setHeld(PlayerModel p) {
        p.item = true;
        holdingPlayer = p;
        lastTouch = p;

        setSensor(true);
    }

    public void setUnheld() {
        if (holdingPlayer != null) {
            holdingPlayer.item = false;
            holdingPlayer = null;
        }

        setSensor(false);
    }

    public boolean isHeld() {
        return holdingPlayer != null;
    }

    /** throw item */
    public void throwItem(Vector2 impulse) {
        getBody().applyLinearImpulse(impulse.scl(THROW_FORCE), getPosition(), true);
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
