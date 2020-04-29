package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.*;
import edu.cornell.gdiac.util.PooledList;

import static edu.cornell.gdiac.nightbite.entity.MovableModel.*;

public class FireEnemyModel extends EnemyModel {
    private static final int THROW_COOLDOWN = 80;
    private static final float THROW_DIST = 3;
    private static final float THROW_FORCE = 5f;
    private int throwCooldown;

    public FireEnemyModel(float x, float y, WorldModel world) {
        super(x, y, Assets.FIRE_ENEMY_WALK, Assets.FIRE_ENEMY_FALL, world);

        //TODO: FIX BELOW
        setPosition(x,y+0.1f); // this is mmoved up so they dont spawn and die
        setHomePosition(new Vector2(x+0.5f, y+0.6f));
    }

    public void attack(PlayerModel p) {
        Vector2 imp = throwFirecracker(p.getPosition());
        if (imp != null) {
            FirecrackerModel f = worldModel.addFirecracker(getPosition().x, getPosition().y);
            f.throwItem(imp);
        }
    }

    public Vector2 throwFirecracker(Vector2 targetPos) {
        if (throwCooldown > 0) {
            throwCooldown --;
            return null;
        }

        if (aiController.canSee(getPosition(), targetPos)
                && getPosition().sub(targetPos).len() < THROW_DIST) {
            throwCooldown = THROW_COOLDOWN;
            return targetPos.cpy().sub(getPosition()).scl(THROW_FORCE);
        }
        return null;
    }
}
