package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.WorldModel;


public class OilEnemyModel extends EnemyModel {
    private static final int DROP_COOLDOWN = 150;
    private int dropCooldown = 0;

    public OilEnemyModel(float x, float y,  WorldModel world) {
        super(x, y, Assets.OIL_ENEMY_WALK, Assets.OIL_ENEMY_FALL, world);
    }

    public void attack(PlayerModel p) {
        // Cool down before dropping another oil
        if (dropCooldown > 0) {
            dropCooldown--;
            return;
        }

        // Drop oil if close to player
        Vector2 targetPosition = p.getPosition();
        Vector2 enemyPosition = getPosition();
        float distance = Vector2.dst(targetPosition.x, targetPosition.y, enemyPosition.x, enemyPosition.y);
        if (distance <= 1.5) {
            worldModel.addOil(enemyPosition.x, enemyPosition.y);
            dropCooldown = DROP_COOLDOWN;
        }
    }
}