package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;

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

    public MoveState state;
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
    private static int GRAB_COOLDOWN_PERIOD = 15;

    /** player identification */
    private String team;

    /** player-item */
    private ArrayList<ItemModel> item;

    /** player texture */
    private FilmStrip holdTexture;

    /** player extra textures */
    private TextureRegion handheld;
    private TextureRegion defaultHandheld;
    private boolean flipHandheld;
    private float angleOffset;
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
    private static int SHADOW_BLINK_FREQUENCY = 15;

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
                Assets.getFilmStrip("character/Filmstrip/Player_1/P1_Dash_5.png"),
                Assets.getFilmStrip("character/P1_Falling_5.png"),
                , );
        setBullet(true);

        impulse = new Vector2();
        boost = new Vector2();

        ticks = 0;

        cooldown = 0;
        boosting = 0;

        item = new ArrayList<>();

        this.home = home;
        team = playerTeam;
        setHomePosition(home.getPosition());

        defaultHandheld = Assets.getTextureRegion("character/wok_64_nohand.png");
        handheld = Assets.getTextureRegion("character/wok_64_nohand.png");
        flipHandheld = false;
        angleOffset = 0;
        swinging = false;
        shadow = Assets.getTextureRegion("character/shadow.png");
        arrow = Assets.getTextureRegion("character/arrow.png");
        swingCooldown = 0;
        alternateShadow = false;

        this.holdTexture = Assets.getFilmStrip("character/Filmstrip/Player_1/P1_Holding_8.png");
        this.world = world;
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

    /** player movement */

    public Vector2 getImpulse() { return impulse; }

    public void setIX(float value) { impulse.x = value; }

    public void setIY(float value) { impulse.y = value; }

    public void setBoostImpulse(float hori, float vert) {
        if (cooldown > 0 || hasItem()) { return; }
        state = MoveState.RUN;
        boosting = BOOST_FRAMES;
        cooldown = COOLDOWN_FRAMES;
        boost.x = hori;
        boost.y = vert;
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

    public void update() {
        updateGrabCooldown();
        updateSwingCooldown();
        if (cooldown > 0) {
            if (cooldown % SHADOW_BLINK_FREQUENCY == 0) {
                alternateShadow = !alternateShadow;
            }
            cooldown--;
        }
        boosting = Math.max(0, boosting - 1);
        if (swinging) {
            updateSwing();
        }
        sliding = Math.max(0, sliding - 1);
    }

    public void resetBoosting() {
        boosting = 0;
    }

    public void resetSliding() {
        sliding = 0;
    }

    public boolean hasItem() {
        return item.size() > 0;
    }

    public ArrayList<ItemModel> getItems() {
        return item;
    }

    public void clearInventory() {
        item.clear();
        setTexture(defaultTexture);
        handheld = defaultHandheld;
    }

    public void holdItem(ItemModel i) {
        item.add(i);
        setCurrentTexture(holdTexture);
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
    public void swingWok(Vector2 clickPos, PooledList<FirecrackerModel> firecrackers, PooledList<EnemyModel> enemies) {
        Vector2 clickVector = new Vector2(clickPos.x, clickPos.y);
        clickVector.sub(getPosition());

        // animation
        if (swingCooldown == 0) {
            startSwing(clickVector.angleRad());
        }

        // hit things // TODO
//        clickVector.nor();
        if (!hasItem()) {
            for (FirecrackerModel firecracker: firecrackers) {
                Vector2 firecrackerVector = firecracker.getPosition();
                firecrackerVector.sub(getPosition());
                if (firecrackerVector.angleRad(clickVector) < SWING_RADIUS && firecrackerVector.angleRad(clickVector) > -SWING_RADIUS && firecrackerVector.len() < REFLECT_RANGE) {
                    Vector2 reflectDirection = new Vector2(firecrackerVector.nor().scl(FIRECRACKER_REFLECT_DIST));
                    firecracker.throwItem(reflectDirection);
                }
            }
        }

        for (EnemyModel enemy : enemies) {
            Vector2 enemyVector = enemy.getPosition();
            enemyVector.sub(getPosition());
            if (enemyVector.angleRad(clickVector) < SWING_RADIUS && enemyVector.angleRad(clickVector) > -SWING_RADIUS && enemyVector.len() < REFLECT_RANGE) {
                Vector2 reflectDirection = new Vector2(enemyVector.nor().scl(PLAYER_REFLECT_DIST));
                enemy.getBody().applyLinearImpulse(reflectDirection.scl(200f), getPosition(), true);
                enemy.forceReplan();
            }
        }



//        DetectionCallback callback = new DetectionCallback();
//        float lowerX = Math.min(getX(), getX()+clickVector.x);
//        float lowerY = Math.min(getY(), getY()+clickVector.y);
//        float upperX = Math.max(getX(), getX()+clickVector.x);
//        float upperY = Math.max(getY(), getY()+clickVector.y);
//        world.QueryAABB(callback, lowerX, lowerY, upperX, upperY);
//        for (Fixture f : callback.foundFixtures) {
//            // TODO check item
//            if (f.getUserData() != HitArea.HITBOX) {
//                Vector2 hitDirection = clickPos;
//                Body b = f.getBody();
//                hitDirection.sub(b.getPosition());
//                b.applyLinearImpulse(hitDirection.nor().scl(100), b.getPosition(), true);
//            }
//        }
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

    public void startSwing(float swingAngle) {
        startSwingCooldown();
//        angleOffset = clickAngle - (float)Math.PI/4;
        if (!flipHandheld) {
            clickAngle = swingAngle - (float)Math.PI/4;
        } else {
            clickAngle = swingAngle - (float)Math.PI * 3/4;
        }
        angleOffset = clickAngle - SWING_RADIUS;
        swinging = true;
    }

    public void updateSwing() {
        if (angleOffset < clickAngle + SWING_RADIUS) {
            angleOffset += 0.1;
        } else {
            angleOffset = 0;
            swinging = false;
        }
    }

    private void startSwingCooldown() {
        swingCooldown = SWING_COOLDOWN_PERIOD;
    }

    private void updateSwingCooldown() {
        if (swingCooldown > 0) {
            swingCooldown--;
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
        if (!isBoostCooldown() || alternateShadow) {
            canvas.draw(shadow, tint, origin.x - texture.getRegionWidth() / 4.0f, origin.y + texture.getRegionHeight() / 15.0f, getX() * drawScale.x, getY() * drawScale.y,
                    getAngle(), actualScale.x, actualScale.y);
        }

        canvas.draw(arrow, tint, arrow.getRegionWidth() / 2.0f, arrow.getRegionHeight() / 2.0f, getX() * drawScale.x + arrowXOffset, getY() * drawScale.y + arrowYOffset,
                arrowAngle, actualScale.x, actualScale.y);

        super.draw(canvas);

        float originX;
        float originY;
        float ox;
        if (flipHandheld) {
            originX = -texture.getRegionWidth() / 5.0f;
            ox = handheld.getRegionWidth();
        } else {
            originX = texture.getRegionWidth() / 5.0f;
            ox = 0;
        }

        if (((FilmStrip) texture).getFrame() == 1) {
            originY = -texture.getRegionHeight() / 3.0f;
        } else {
            originY = -texture.getRegionHeight() / 5.0f;
        }

        // Don't draw weapon when holding item or dead
        if (!hasItem() && isAlive()) {
            canvas.draw(handheld, tint,ox,0,getX() * drawScale.x + originX, getY() * drawScale.y + originY,
                    getAngle() + angleOffset,actualScale.x,actualScale.y);
        }
    }
}