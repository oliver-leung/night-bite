package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.WorldModel;


public class OilEnemyModel extends EnemyModel {
    public OilEnemyModel(float x, float y, float width, float height,  WorldModel world) {
        super(x, y, width, height, Assets.OIL_ENEMY_WALK, Assets.OIL_ENEMY_FALL, world);
    }

    public void attack(PlayerModel p) {
        Vector2 targetPosition = p.getPosition();
        Vector2 enemyPosition = getPosition();
        float distance = Vector2.dst(targetPosition.x, targetPosition.y, enemyPosition.x, enemyPosition.y);
        if (distance <= 1) { // If close to target, drop oil
            OilModel oil = new OilModel(enemyPosition.x * worldModel.getScale().x, enemyPosition.y * worldModel.getScale().y);
            worldModel.addOil(enemyPosition.x, enemyPosition.y);
        }
    }
}