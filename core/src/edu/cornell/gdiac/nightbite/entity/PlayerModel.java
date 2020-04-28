package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;

import static edu.cornell.gdiac.nightbite.entity.MovableModel.*;

public class PlayerModel extends HumanoidModel {

    // TODO
    private int NUM_ITEMS = 2;

    /**
     * player movement params
     */
    private static final float DEFAULT_THRUST = 10.0f;
    private static final float BOOST_IMP = 100.0f;
    private static final float MOTION_DAMPING = 25f;

    private static final int BOOST_FRAMES = 20;
    private static final int COOLDOWN_FRAMES = 70;

    public enum MoveState {
        WALK,
        RUN,
        STATIC
    }

    public enum DirectionState {
        NORTH,
        NORTHEAST,
        EAST,
        SOUTHEAST,
        SOUTH,
        SOUTHWEST,
        WEST,
        NORTHWEST,
    }

    public MoveState state;
    private int boosting;
    private int cooldown;

    private float prevHoriDir;
    private int playerWalkCounter;

    private Vector2 impulse;
    private Vector2 boost;

    /** player respawn */
    private boolean isAlive;
    private int spawnCooldown;

    /** cooldown for grabbing and throwing items */
    private int grabCooldown;
    private static int GRAB_COOLDOWN_PERIOD = 15;

    /** player identification */
    private String team;

    private Vector2 homeLoc;

    /** player-item */
    private ArrayList<ItemModel> item;
    private ArrayList<Boolean> overlapItem;

    /** player texture */
    private FilmStrip defaultTexture;
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
    private int REFLECT_DIST = 8;
    private float REFLECT_RANGE = 1.5f;

    public PlayerModel(float x, float y, float width, float height, World world, FilmStrip texture, FilmStrip holdTexture, TextureRegion wokTexture, TextureRegion shadowTexture, TextureRegion arrowTexture, String playerTeam) {
        super(x, y, width, height);
        setName("ball");

        this.texture = texture;
        setTexture(texture);

        impulse = new Vector2();
        boost = new Vector2();

        prevHoriDir = -1;
        playerWalkCounter = 0;

        cooldown = 0;
        boosting = 0;

        isAlive = true;
        item = new ArrayList<>();
        overlapItem = new ArrayList<>();
        for (int i = 0; i < NUM_ITEMS; i++) {
            overlapItem.add(false);
        }
        // TODO Set this later on in a better way
        homeLoc = new Vector2(x - 0.5f, y - 0.5f);
        team = playerTeam;
        setDensity(MOVABLE_OBJ_DENSITY);
        setFriction(MOVABLE_OBJ_FRICTION);
        setRestitution(MOVABLE_OBJ_RESTITUTION);

        handheld = wokTexture;
        defaultHandheld = wokTexture;
        flipHandheld = false;
        angleOffset = 0;
        swinging = false;
        shadow = shadowTexture;
        arrow = arrowTexture;
        swingCooldown = 0;
        alternateShadow = false;

        this.holdTexture = holdTexture;
        this.world = world;
    }

    public void resetTexture() {
        texture = defaultTexture;
        handheld = defaultHandheld;
    }

    public void flipTexture() {
        texture.flip(true, false);
        handheld.flip(true, false);
        flipHandheld = !flipHandheld;
    }

    public void setTexture(FilmStrip value) {
        if (defaultTexture == null) {
            defaultTexture = value;
        }
        super.setTexture(value);
    }

    /** player identification */
    public String getTeam() {
        return team;
    }

    public Vector2 getHomeLoc() {
        return homeLoc;
    }

    /** physics */
    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setLinearDamping(MOTION_DAMPING);
        body.setFixedRotation(true);
        return true;
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
        body.applyLinearImpulse(impulse.nor().scl(DEFAULT_THRUST).add(boost.nor().scl(BOOST_IMP)), getPosition(), true);
        boost.setZero();
    }

    public boolean isBoostCooldown() {
        return cooldown > 0;
    }

    /** movement state */

    public float getPrevHoriDir() {
        return prevHoriDir;
    }

    public void setPrevHoriDir(float dir) {
        prevHoriDir = dir;
    }

    public void setWalk() {
        if (boosting > 0) { return; }
        state = MoveState.WALK;

        if (playerWalkCounter % 20 == 0) {
            ((FilmStrip) texture).setFrame(1);
            if (prevHoriDir == 1) {
                texture.flip(true, false);
            }
        } else if (playerWalkCounter % 20 == 10) {
            ((FilmStrip) texture).setFrame(0);
            if (prevHoriDir == 1) {
                texture.flip(true, false);
            }
        }
        playerWalkCounter++;
    }

    public void setStatic() {
        if (boosting > 0) { return; }
        state = MoveState.STATIC;

        playerWalkCounter = 0;
        ((FilmStrip) texture).setFrame(0);
        if (prevHoriDir == 1) {
            texture.flip(true, false);
        }
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
    }

    public void resetBoosting() {
        boosting = 0;
    }

    /** player respawn */
    public boolean isAlive() {
        return isAlive;
    }

    public void setDead() {
        isAlive = false;
        draw = false;
    }

    public void respawn() {
        if (spawnCooldown == 0) {
            spawnCooldown = 60;
        }
        spawnCooldown--;
        if (spawnCooldown == 0) {
            setPosition(homeLoc);
            isAlive = true;
            draw = true;
        }
        resetTexture();

        setLinearVelocity(Vector2.Zero);
    }

    /** player-item */
    public void setOverlapItem(int id, boolean b) {
        overlapItem.set(id, b);
    }

    public boolean getOverlapItem(int id) {
        return overlapItem.get(id);
    }

    public boolean hasItem() {
        return item.size() > 0;
    }

    public ArrayList<ItemModel> getItems() {
        return item;
    }

    public void clearInventory() {
        item.clear();
        texture = defaultTexture;
        handheld = defaultHandheld;
    }

    public void holdItem(ItemModel i) {
        item.add(i);
        texture = holdTexture;
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
    public void swingWok(Vector2 clickPos, PooledList<FirecrackerModel> firecrackers) {
        Vector2 clickVector = new Vector2(clickPos.x, clickPos.y);
        clickVector.sub(getPosition());

        // animation
        if (swingCooldown == 0) {
            startSwing(clickVector.angleRad());
        }

        // hit things // TODO
//        clickVector.nor();
        for (FirecrackerModel firecracker: firecrackers) {
            Vector2 firecrackerVector = firecracker.getPosition();
            firecrackerVector.sub(getPosition());
            if (firecrackerVector.angleRad(clickVector) < SWING_RADIUS && firecrackerVector.angleRad(clickVector) > -SWING_RADIUS && firecrackerVector.len() < REFLECT_RANGE) {
                System.out.println("called");
                Vector2 reflectDirection = new Vector2(firecrackerVector.nor().scl(REFLECT_DIST));
                firecracker.throwItem(reflectDirection);
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
    public void updateDirectionState(float vert, float hori) {
        arrowAngle = -1 * ((new Vector2(vert, hori).angleRad()) - (float) Math.PI/2);
        if (hori == -1) {
            arrowXOffset = -1 * texture.getRegionWidth()/2;
        } else if (hori == 1) {
            arrowXOffset = texture.getRegionWidth()/2;
        } else {
            arrowXOffset = 0;
        }
        if (vert == -1) {
            arrowYOffset = -1 * texture.getRegionHeight() * 3/4;
        } else if (vert == 1){
            arrowYOffset = -1 * texture.getRegionHeight()/5;
        } else {
            arrowYOffset = -1 * texture.getRegionHeight()/2;
        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (!isBoostCooldown() || alternateShadow) {
            canvas.draw(shadow, Color.WHITE,origin.x-texture.getRegionWidth()/4,origin.y+texture.getRegionHeight()/15,getX() * drawScale.x, getY() * drawScale.y,
                    getAngle(),actualScale.x,actualScale.y);
        }

        canvas.draw(arrow, Color.WHITE,arrow.getRegionWidth()/2,arrow.getRegionHeight()/2,getX() * drawScale.x + arrowXOffset, getY() * drawScale.y + arrowYOffset,
                arrowAngle,actualScale.x,actualScale.y);

        super.draw(canvas);

        float originX;
        float originY;
        float ox;
        if (flipHandheld) {
            originX = -texture.getRegionWidth()/5;
            ox = handheld.getRegionWidth();
        } else {
            originX = texture.getRegionWidth()/5;
            ox = 0;
        }

        if (((FilmStrip) texture).getFrame() == 1) {
            originY = -texture.getRegionHeight()/3;
        } else {
            originY = -texture.getRegionHeight()/5;
        }

        if (!hasItem()) {
            canvas.draw(handheld, Color.WHITE,ox,0,getX() * drawScale.x + originX, getY() * drawScale.y + originY,
                    getAngle() + angleOffset,actualScale.x,actualScale.y);
        }
    }
}