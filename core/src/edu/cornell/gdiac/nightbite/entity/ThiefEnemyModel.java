package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.AILattice;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.WorldModel;

public class ThiefEnemyModel extends EnemyModel {
    /** Cooldown after attempting an attack */
    private static final int ATTACK_COOLDOWN = 70;
    private int attackCooldown = 0;

    /** Cooldown after an exchange happens between player & thief
     * NOTE: caused by collision & applies to both player and thief*/
    private static final int CONTACT_COOLDOWN = 50;
    public int contactCooldown = 0;

    private static final float THROW_FORCE = 50f;

    /** Indicates whether previous iteration was on phase 1 of attack */
    private boolean previousPhaseOne = true;
    private int flashCooldown = 0;

    public ThiefEnemyModel(float x, float y, WorldModel world) {
        super(
                x, y,
                Assets.getFilmStrip("character/Enemies/E3_64_Walk_FS_8.png"),
                Assets.getFilmStrip("character/Enemies/E3_64_Falling_FS_5.png"),
                world
        );
        setHoldTexture(Assets.getFilmStrip("character/Enemies/E3_64_holdfilmstrip.png"));
        setStopDist(0);
    }

    public Vector2 attack(PlayerModel p, AILattice aiLattice) {
        Vector2 dir = new Vector2(0,0);
        ItemModel item = worldModel.getItem(0);

        // Don't attack if there is cooldown time OR item is on ground
        if (attackCooldown > 0 || !item.isHeld()) {
            dir = move(getHomePosition(), p.getDimension(), worldModel.getAILattice());
            setIsDoneAttacking(true);
            attackCooldown--;
        } else if (p.hasItem()) { // Attack phase 1 : Player is holding an item - chase to steal
            previousPhaseOne = true;
            setIsDoneAttacking(false);
            dir = move(p.getPosition(), p.getDimension(), aiLattice);
        } else if (!isDoneAttacking) { // Attack phase 2 : Stole the item - return item to origin
            // If changed phase, force AI to replan path to target item's origin location
            if (previousPhaseOne) {
                aiController.forceReplan();
                previousPhaseOne = false;
            }

            // Move to item origin
            Vector2 initPosition = item.getItemInitPosition();
            dir = move(initPosition, item.getDimension(), worldModel.getAILattice());

            // Drop item if near origin
            float distance = initPosition.cpy().dst(getPosition());
            if (distance <= 1.5) {
                item.throwItem(getPosition(), initPosition.cpy().sub(getPosition()), THROW_FORCE );
                clearInventory();
                resetTexture();

                resetThief(); // Reset for next attack
            }
        }

        return dir;
    }

    public void resetContactcooldown() { contactCooldown = CONTACT_COOLDOWN; }

    public void resetThief() {
        setIsDoneAttacking(true);
        attackCooldown = ATTACK_COOLDOWN;
    }

    @Override
    public Vector2 update(PlayerModel p) {
        contactCooldown--;
        return super.update(p);
    }

    @Override
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
//        if (state == State.ATTACK && (flashCooldown / 15) % 2 == 0) {
//            canvas.draw(EXCLAMATION,
//                    (getPosition().x - 0.5f) * worldModel.getScale().x,
//                    (getPosition().y + 0.5f) * worldModel.getScale().y);
//        }
        flashCooldown++;
    }
}
