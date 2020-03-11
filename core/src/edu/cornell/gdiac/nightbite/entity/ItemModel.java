package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;

public class ItemModel extends BoxObstacle {

    public static TextureRegion itemTexture;
    private int itemCooldown; // used during item grab and item respawn
    private static int COOLDOWN_PERIOD = 25;

    public ItemModel(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

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
}
