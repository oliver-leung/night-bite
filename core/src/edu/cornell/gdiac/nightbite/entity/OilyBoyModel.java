package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.AIController;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.WorldModel;


public class OilyBoyModel extends EnemyModel {
    public OilyBoyModel(float x, float y, float width, float height,  WorldModel world) {
        super(x, y, width, height, Assets.OIL_ENEMY_WALK, Assets.OIL_ENEMY_FALL, world);
    }

    public void attack(PlayerModel p) {
        OilModel oil;
        oil = new OilModel(getPosition());
        worldModel.addStaticObject(oil);
    }
}