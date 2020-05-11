package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.GameCanvas;

import edu.cornell.gdiac.nightbite.WorldModel;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;

import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.SoundController;

import java.util.ArrayList;

public class PlayerModel extends HumanoidModel {

    /** Regular walking impulse as a scalar */
    private static final float WALK_IMPULSE = 10.0f;
    private static final float BOOST_IMP = 100.0f;
    private static final int BOOST_FRAMES = 20;
    private static final int COOLDOWN_FRAMES = 70;
    private static final int SLIDE_FRAMES = 50; // Slide through about 3-4 tiles

    public enum MoveState {
        WALK,
        RUN,
        STATIC,
        SLIDE
    }

    private static int GRAB_COOLDOWN_PERIOD = 15;
    private static int SHADOW_BLINK_FREQUENCY = 15;

    public MoveState state;
    // TODO
    private int NUM_ITEMS = 1;
    private int boosting;
    private int cooldown;
    private int sliding;
    private int ticks;
    private Vector2 impulse;
    private Vector2 boost;
    private float slideHorizontal;
    private float slideVertical;
    /** cooldown for grabbing and throwing items */
    private int grabCooldown;
    /** player identification */
    private String team;
    /** player extra textures */
    private TextureRegion handheld;
    private TextureRegion defaultHandheld;
    private boolean flipHandheld;
    private float angleOffset;
    private float targetAngle;
    private float prevAngleOffset;
    private float clickAngle;
    private float SWING_RADIUS = 0.7f;
    private boolean swinging;
    private int swingCooldown;
    private int SWING_COOLDOWN_PERIOD = 30;
    private TextureRegion shadow;
    private TextureRegion arrow;
    private float arrowAngle;
    private float arrowXOffset;
    private float arrowYOffset;
    /** blinking shadow while dashing */
    private boolean alternateShadow;
    /** wok hitbox */
    private PolygonObstacle wokHitbox;
    private World world;

    private int PLAYER_REFLECT_DIST = 2;
    private int FIRECRACKER_REFLECT_DIST = 15;
    private float REFLECT_RANGE = 2f;
    private HomeModel home;

    public PlayerModel(float x, float y, float width, float height, World world, String playerTeam, HomeModel home) {
        super(
                x, y, width, height,
                Assets.getFilmStrip("character/Filmstrip/Player_1/Dash_FS_5_NoArms.png"),
                Assets.getFilmStrip("character/P1_Falling_5.png")
        );
        setBullet(true);

        impulse = new Vector2();
        boost = new Vector2();

        ticks = 0;

        cooldown = 0;
        boosting = 0;

        this.home = home;
        team = playerTeam;
        setHomePosition(home.getPosition());

        defaultHandheld = Assets.getTextureRegion("character/panarm.png"); //panarm
        handheld = Assets.getTextureRegion("character/panarm.png"); // wok_64_nohand
        flipHandheld = false;
        angleOffset = 0;
        prevAngleOffset = 0;
        swinging = false;
        shadow = Assets.getTextureRegion("character/shadow.png");
        arrow = Assets.getTextureRegion("character/arrow.png");
        swingCooldown = 0;
        alternateShadow = false;

        setHoldTexture(Assets.getFilmStrip("character/Filmstrip/Player_1/P1_Holding_8.png"));
    }

    public void playWalkSound() {
        SoundController soundController = SoundController.getInstance();
        if (state == MoveState.WALK && !soundController.isActive("audio/walking.wav")) {
            soundController.play("audio/walking.wav", "audio/walking.wav", true, Assets.VOLUME * 1.5f);
        } else if (state != MoveState.WALK && soundController.isActive("audio/walking.wav")) {
            soundController.stop("audio/walking.wav");
        }
    }

    public int getTicks() {
        return ticks;
    }

    /**
     * Flips the current texture and the weapon horizontally
     */
    public void flipTexture() {
        super.flipTexture();
        handheld.flip(true, false);
        flipHandheld = !flipHandheld;
    }

    /** player identification */
    public String getTeam() {
        return team;
    }

    @Override
    public void setWalkTexture() {
        FilmStrip filmStrip = (FilmStrip) this.texture;
        filmStrip.setFrame((walkCounter / 5) % filmStrip.getSize());
        if (prevHoriDir == 1) {
            texture.flip(true, false);
        }
        walkCounter++;
    }

    /** player movement */

    public Vector2 getImpulse() {
        return impulse;
    }

    public void setIX(float value) {
        impulse.x = value;
    }

    public void setIY(float value) {
        impulse.y = value;
    }

    public void setBoostImpulse(float hori, float vert) {
        // you can't boost
        return;
        // if (cooldown > 0 || hasItem()) { return; }
        // state = MoveState.RUN;
        // boosting = BOOST_FRAMES;
        // cooldown = COOLDOWN_FRAMES;
        // boost.x = hori;
        // boost.y = vert;
    }

    public void applyImpulse() {
        if (!isActive()) {
            return;
        }
        body.applyLinearImpulse(impulse.nor().scl(WALK_IMPULSE).add(boost.nor().scl(BOOST_IMP)), getPosition(), true);
        boost.setZero();
    }

    public boolean isBoostCooldown() {
        return cooldown > 0;
    }

    public void incrTicks() {
        ticks++;
    }

    public void resetTicks() {
        ticks = 0;
    }

    public void setWalk() {
        if (boosting > 0 || sliding > 0) { return; }
        state = MoveState.WALK;
        setWalkTexture();
    }

    public void setStatic() {
        if (boosting > 0 || sliding > 0) { return; }
        state = MoveState.STATIC;
        resetTicks();
        setStaticTexture();
    }

    public boolean isSliding(){
        return sliding > 0;
    }

    public void setSlide() {
        state = MoveState.SLIDE;
        sliding = SLIDE_FRAMES;
    }

    public void setSlideDirection(float horizontal, float vertical) {
        if (state != MoveState.SLIDE) { // set direction if player wasn't sliding before
            slideHorizontal = horizontal;
            slideVertical = vertical;
        }
    }

    public Vector2 getSlideDirection() {
        return new Vector2(slideHorizontal, slideVertical);

    }

    public void update(Vector2 pointWokDir) {
        updateGrabCooldown();
        if (cooldown > 0) {
            if (cooldown % SHADOW_BLINK_FREQUENCY == 0) {
                alternateShadow = !alternateShadow;
            }
            cooldown--;
        }
        boosting = Math.max(0, boosting - 1);
        if (swinging) {
            updateSwingCooldown();
        }
        if (!swinging) {
            System.out.println(pointWokDir);
            updateWokDirection(pointWokDir);
        }
        sliding = Math.max(0, sliding - 1);
    }

    public void resetBoosting() {
        boosting = 0;
    }

    public void resetSliding() {
        sliding = 0;
    }

    public void clearInventory() {
        super.clearInventory();
        handheld = defaultHandheld;
    }

    /** cooldown between grabbing/throwing */

    public void startgrabCooldown() {
        grabCooldown = GRAB_COOLDOWN_PERIOD;
    }

    private void updateGrabCooldown() {
        if (grabCooldown > 0) {
            grabCooldown -= 1;
        }
    }

    public boolean grabCooldownOver() {
        return grabCooldown == 0;
    }

    /** swings wok */
    public void swingWok(Vector2 clickPos, PooledList<FirecrackerModel> firecrackers, PooledList<HumanoidModel> enemies) {

        // animation
        if (swingCooldown == 0) {
            startSwing();
        }

        Vector2 clickVector = new Vector2(clickPos.x, clickPos.y);
        clickVector.sub(getPosition());

        if (!hasItem() && isAlive()) {
            for (FirecrackerModel firecracker: firecrackers) {
                Vector2 firecrackerVector = firecracker.getPosition();
                firecrackerVector.sub(getPosition());
                if (firecrackerVector.angleRad(clickVector) < SWING_RADIUS
                        && firecrackerVector.angleRad(clickVector) > -SWING_RADIUS
                        && firecrackerVector.len() < REFLECT_RANGE
                        && !firecracker.isDetonating()) {
                    Vector2 reflectDirection = new Vector2(firecrackerVector.nor().scl(FIRECRACKER_REFLECT_DIST));
                    firecracker.throwItem(reflectDirection);
                    SoundController.getInstance().play("audio/whack4.wav", "audio/whack4.wav", false, Assets.VOLUME * 1.8f);
                }
            }

            for (HumanoidModel enemy : enemies) {
                Vector2 enemyVector = enemy.getPosition();
                enemyVector.sub(getPosition());
                if (enemyVector.angleRad(clickVector) < SWING_RADIUS && enemyVector.angleRad(clickVector) > -SWING_RADIUS && enemyVector.len() < REFLECT_RANGE) {
                    Vector2 reflectDirection = new Vector2(enemyVector.nor().scl(PLAYER_REFLECT_DIST));
                    enemy.getBody().applyLinearImpulse(reflectDirection.scl(200f), getPosition(), true);
                    if (enemy instanceof EnemyModel) {
                        ((EnemyModel) enemy).forceReplan();
                    }
                }
                SoundController.getInstance().play("audio/whack4.wav", "audio/whack4.wav", false, Assets.VOLUME * 1.8f);
            }
        }
    }

    public void updateWokDirection(Vector2 pointWokDir) {
        // TODO why is getPos lower than player drawn
        Vector2 playerPos = getPosition();
        playerPos.y -= 0.8f;

        pointWokDir.sub(playerPos);
        pointWokDir.scl(-1);
        float wokAngle = pointWokDir.angleRad();
        prevAngleOffset = angleOffset;
        angleOffset = wokAngle;

        if ((prevAngleOffset < (float) Math.PI/2 && prevAngleOffset > (float) - Math.PI/2 && (angleOffset <= (float) - Math.PI/2 || angleOffset >= (float) Math.PI/2)) || (angleOffset < (float) Math.PI/2 && angleOffset > (float) - Math.PI/2 && (prevAngleOffset <= (float) - Math.PI/2 || prevAngleOffset >= (float) Math.PI/2))) {
            flipTexture();
            if (getPrevHoriDir() == 1) {
                setPrevHoriDir(-1);
            } else {
                setPrevHoriDir(1);
            }
        }
    }

    static class DetectionCallback implements QueryCallback {
        ArrayList<Fixture> foundFixtures = new ArrayList<>();

        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixture.getUserData() == HumanoidModel.HitArea.HITBOX) {
                foundFixtures.add(fixture);
            }
            return true;
        }
    }

    public void startSwing() {
        startSwingCooldown();
//        if (angleOffset < (float) Math.PI/2 && angleOffset > (float) -Math.PI/2) {
//            targetAngle = angleOffset + (float) Math.PI;
//        } else {
//            targetAngle = angleOffset - (float) Math.PI;
//        }


//        if (!flipHandheld) {
//            clickAngle = angleOffset - (float)Math.PI/4;
//        } else {
//            clickAngle = angleOffset - (float)Math.PI * 3/4;
//        }
//        angleOffset = clickAngle - SWING_RADIUS;
//        angleOffset -= Math.PI;
//        if (angleOffset <= Math.PI) {
//            angleOffset = (float) Math.PI - angleOffset;
//        }
        targetAngle = angleOffset + (float) Math.PI;
        swinging = true;
    }

    private void startSwingCooldown() {
        swingCooldown = SWING_COOLDOWN_PERIOD;
    }

    private void updateSwingCooldown() {
        if (swingCooldown > 0) {
            swingCooldown--;
        } else {
            swinging = false;
        }
    }

    /** player arrow direction */
    // TODO: Make everything x, y
    public void updateDirectionState(float vert, float hori) {
        arrowAngle = new Vector2(hori, vert).angleRad();
        if (hori == -1) {
            arrowXOffset = -1.0f * texture.getRegionWidth() / 2.0f;
        } else if (hori == 1) {
            arrowXOffset = texture.getRegionWidth() / 2.0f;
        } else {
            arrowXOffset = 0;
        }
        if (vert == -1) {
            arrowYOffset = -1.0f * texture.getRegionHeight() * 3.0f / 4.0f;
        } else if (vert == 1){
            arrowYOffset = -1.0f * texture.getRegionHeight() / 5.0f;
        } else {
            arrowYOffset = -1.0f * texture.getRegionHeight() / 2.0f;
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (isAlive) {
            if (!isBoostCooldown() || alternateShadow) {
                canvas.draw(shadow, Color.WHITE, origin.x - texture.getRegionWidth() / 4.0f, origin.y + texture.getRegionHeight() / 15.0f, getX() * drawScale.x, getY() * drawScale.y,
                        getAngle(), actualScale.x, actualScale.y);
            }

            canvas.draw(arrow, Color.WHITE, arrow.getRegionWidth() / 2.0f, arrow.getRegionHeight() / 2.0f, getX() * drawScale.x + arrowXOffset, getY() * drawScale.y + arrowYOffset,
                    arrowAngle, actualScale.x, actualScale.y);
        }

        super.draw(canvas);

        if (isAlive && !hasItem()) {
            float originX;
            float originY;
            float ox;
            if (flipHandheld) {
                originX = - texture.getRegionWidth() / 11.0f;
                originY = - texture.getRegionHeight() / 10.0f;
                ox = handheld.getRegionWidth() * 17/20;
            } else {
                originX = texture.getRegionWidth() / 10.0f;
                originY = - texture.getRegionHeight() / 9.0f;
                ox = texture.getRegionWidth() / 9.0f;
            }
            if (!swinging) {
                canvas.draw(handheld, tint,ox,0,getX() * drawScale.x + originX, getY() * drawScale.y + originY,
                        getAngle() + angleOffset - (float) Math.PI/2,actualScale.x,actualScale.y);
            } else {
                canvas.draw(handheld, tint,ox,0,getX() * drawScale.x + originX, getY() * drawScale.y + originY,
                        getAngle() + targetAngle - (float) Math.PI/2,actualScale.x,actualScale.y);
            }
        }
    }
}