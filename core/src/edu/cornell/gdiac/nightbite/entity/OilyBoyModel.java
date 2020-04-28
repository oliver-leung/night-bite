package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.AIController;
import edu.cornell.gdiac.nightbite.WorldModel;

public class OilyBoyModel extends AIController {
    private enum State {
        IDLE,
        CHASE,
        DROP,
        RETURN
    }
    private int MAX_CHASE_FRAME = 50;
    private int chasing;

    public State state = State.IDLE;
    private HumanoidModel enemy;
    private boolean droppedOil = false;
    private boolean returned = false;

    public OilyBoyModel(int w, int h, Rectangle bounds, WorldModel worldModel, HumanoidModel enemy) {
        super(w,h,bounds,worldModel,enemy);
        enemy = getEnemy();
    }

    public updateState (HumanoidModel target, WorldModel world, int numOils) {
        switch (state) {
            case IDLE: // If idle and spots target, chase
                if (canSee(target)) {
                    state = state.CHASE;
                    chasing = MAX_CHASE_FRAME;
                }
                break;
            case CHASE:
                // TODO: Move to target
                Vector2 targetPosition = target.getPosition();
                Vector2 enemyPosition = enemy.getPosition();
                float distance = Vector2.dst(targetPosition.x, targetPosition.y, enemyPosition.x, enemyPosition.y);
                if (distance <= 1) { // If close to target, drop oil
                    state = State.DROP;
                }
                else if (chasing == 0) { // Timeout in chasing
                    state = State.RETURN;
                }
                break;
            case DROP:
                // TODO: Create oil object
                OilModel oil;
                oil = new OilModel(enemy.getX(),enemy.getY(), numOils);
                world.addStaticObject(oil);
                state= State.RETURN;
                break;
            case RETURN:
                // TODO: Move to origin
                state = State.IDLE;
                break;
            default:
                break;
        }
    }

}
