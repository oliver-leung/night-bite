package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.AILattice;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.WorldModel;
import edu.cornell.gdiac.util.LightSource;

public class FireEnemyModel extends EnemyModel {
    private static final int MAX_THROW_COOLDOWN = 2*60;
    private static final int MIN_THROW_COOLDOWN = 1*60;
    private static final float THROW_DIST = 4;
    private static final float STOP_DIST = 2.5f;
    private static final float THROW_FORCE = 2f;
    private static final float THROW_TIME = 0.7f; // fudged in seconds
    private static final float MIN_DIST_DEV = 0.4f;
    private static final float MAX_DIST_DEV = 1.4f;
    private static final float MAX_DEVIATION = 20f;
    private static final float MIN_DEVIATION = -20f;
    private static final float TOO_CLOSE_DIST = 1.5f;
    private int throwCooldown;

    private final Vector2 source, target, targetPred, cache;

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

        aiClass = 1;
    }

    public Vector2 attack(PlayerModel p, AILattice aiLattice) {
        Vector2 dir = move(p.getPosition(), p.getDimension(), aiLattice);
        Vector2 imp = throwFirecracker(p.getPosition(), p.getLinearVelocity(), aiLattice);
        if (imp != null) {
            FirecrackerModel f = worldModel.addFirecracker(getPosition().x, getPosition().y);
            LightSource light = worldModel.createPointLight(new float[]{0.15f, 0f, 0f, 1.0f}, 1.5f);
            light.attachToBody(f.getBody());
            f.setLight(light);
            f.throwItem(imp.scl(imp.len()).scl(THROW_FORCE).scl(MathUtils.random(MIN_DIST_DEV, MAX_DIST_DEV)));
            walkCooldown = WALK_COOLDOWN;
        }
        return dir;
    }

    // TODO: Shouldn't only do shot prediction -- randomized or controlled?
    public Vector2 throwFirecracker(Vector2 targetPos, Vector2 targetVelocity, AILattice aiLattice) {
        source.set(getPosition());
        target.set(targetPos);

        // TODO: Move this after throwCooldown
        // TODO: average walk velocity? or is that overkill
        // Estimated walk vector
        Vector2 walk = cache.set(targetVelocity).scl(THROW_TIME);
        cache.add(targetPos);
        // cache.rotate(MathUtils.random(MIN_DEVIATION, MAX_DEVIATION));
        targetPred.set(cache);
        // ;System.out.println(aiLattice.isReachable(cache, targetPos));

        if (throwCooldown > 0) {
            throwCooldown --;
            // source.set(-3, -3);
            return null;
        }


        if (aiController.canTarget(getPosition(), cache, THROW_DIST)) { // && aiLattice.isReachable(cache, targetPos)) {// && !targetVelocity.epsilonEquals(Vector2.Zero)) {
            resetThrowCooldown();
            cache.sub(getPosition()).rotate(MathUtils.random(MIN_DEVIATION, MAX_DEVIATION));
            targetPred.set(getPosition()).add(cache);
            if (cache.len() > TOO_CLOSE_DIST) {
                if (cache.len() > THROW_DIST) {
                    cache.nor().scl(THROW_DIST);
                }
                return cache.cpy();
            }
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

        if (aiController.canSee(getPosition(), targetPos)
                && getPosition().sub(targetPos).len() < THROW_DIST) {
            resetThrowCooldown();
            cache.set(targetPos).sub(getPosition());
            if (cache.len() < TOO_CLOSE_DIST) {
                cache.nor().scl(TOO_CLOSE_DIST).scl(TOO_CLOSE_DIST).cpy();
            }
            return cache.cpy();
        }
        return null;
    }

    @Override
    public Vector2 move(Vector2 targetPos, Vector2 targetDims, AILattice aiLattice) {
//        System.out.println(walkCounter);
        if (getPosition().sub(targetPos).len() < STOP_DIST && aiController.canTarget(getPosition(), targetPos, STOP_DIST)) {
            return Vector2.Zero;
        }

        return super.move(targetPos, targetDims, aiLattice);
    }

    private void resetThrowCooldown() {
        throwCooldown = MathUtils.random(MIN_THROW_COOLDOWN, MAX_THROW_COOLDOWN);
    }

    @Override
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        if (state == State.ATTACK && (throwCooldown / 15) % 2 == 0) {
            canvas.draw(EXCLAMATION,
                    (getPosition().x - 0.5f) * worldModel.getScale().x,
                    (getPosition().y + 0.5f) * worldModel.getScale().y);
        }
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        // cuz screw this particular point in general
        if (source.epsilonEquals(-3, -3)) {
            return;
        }
        aiController.drawRays(canvas, source, target, Color.CORAL, drawScale);
        aiController.drawRays(canvas, source, targetPred, Color.FIREBRICK, drawScale);
    }
}
