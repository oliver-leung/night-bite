package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.AILattice;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.WorldModel;

public class FireEnemyModel extends EnemyModel {
    private static final int THROW_COOLDOWN = 50;
    private static final float THROW_DIST = 5;
    private static final float THROW_FORCE = 3f;
    private static final float THROW_TIME = 1f; // fudged in seconds
    private int throwCooldown;

    private Vector2 source, target, targetPred, cache;

    public FireEnemyModel(float x, float y, WorldModel world) {
        super(
                x, y,
                Assets.getFilmStrip("character/Enemies/E1_64_Walk_FS_8.png"),
                Assets.getFilmStrip("character/Enemies/E1_64_Falling_FS_5.png"),
                world
        );

        source = new Vector2();
        target = new Vector2();
        targetPred = new Vector2();
        cache = new Vector2();

        //TODO: FIX BELOW
        setPosition(x, y + 0.1f); // this is moved up so they dont spawn and die
        setHomePosition(new Vector2(x + 0.5f, y + 0.6f));
    }

    public void attack(PlayerModel p, AILattice aiLattice) {
        Vector2 imp = throwFirecracker(p.getPosition(), p.getLinearVelocity(), aiLattice);
        if (imp != null) {
            FirecrackerModel f = worldModel.addFirecracker(getPosition().x, getPosition().y);
            f.throwItem(imp.scl(imp.len()).scl(THROW_FORCE));
        }
    }

    // TODO: Shouldn't only do shot prediction -- randomized or controlled?
    public Vector2 throwFirecracker(Vector2 targetPos, Vector2 targetVelocity, AILattice aiLattice) {
        source.set(getPosition());
        target.set(targetPos);

        // TODO: Move this after throwCooldown
        // TODO: average walk velocity? or is that overkill
        // Estimated walk vector
        Vector2 walk = cache.set(targetVelocity).scl(THROW_TIME);
        System.out.println(walk);
        cache.add(targetPos);
        targetPred.set(cache);
        // ;System.out.println(aiLattice.isReachable(cache, targetPos));

        if (throwCooldown > 0) {
            throwCooldown --;
            // source.set(-3, -3);
            return null;
        }



        if (aiController.canTarget(getPosition(), cache, THROW_DIST)) { // && aiLattice.isReachable(cache, targetPos)) {// && !targetVelocity.epsilonEquals(Vector2.Zero)) {
            throwCooldown = THROW_COOLDOWN;
            cache.sub(getPosition());
            if (cache.len() < THROW_DIST) {
                cache.nor().scl(THROW_DIST);
            }
            return cache.cpy();
        }

        // Can you imagine cosine rule being useful ever?
        // Haha it actually isn't i'm commenting out all of these
        // float a2 = cache.set(targetPos).sub(getPosition()).len2();
        // float b2 = cache.set(targetPos).add(walk).sub(getPosition()).len2();
        // float cosC = (a2 + b2 - walk.len2())/(2 * (float) Math.sqrt(b2 * a2));
        //
        // // DO NOT CHANGE THE VALUE OF CACHE
        //
        // // Determine quadrant
        // if (cache.x >= 0) {
        //     if (cache.y >= 0) {
        //
        //     } else {
        //
        //     }
        // } else {
        //
        // }

        // if (aiController.canSee())

        if (aiController.canSee(getPosition(), targetPos)
                && getPosition().sub(targetPos).len() < THROW_DIST) {
            throwCooldown = THROW_COOLDOWN;
            return targetPos.cpy().sub(getPosition());
        }
        return null;
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        if (source.epsilonEquals(-3, -3)) {
            return;
        }
        aiController.drawRays(canvas, source, target, Color.CORAL, drawScale);
        aiController.drawRays(canvas, source, targetPred, Color.FIREBRICK, drawScale);
    }
}
