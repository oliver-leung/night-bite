package edu.cornell.gdiac.nightbite.ball;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.CapsuleObstacle;


public class BallModel extends CapsuleObstacle{

    /**
     * The density of this ball
     */
    private static final float DEFAULT_DENSITY = 1.0f;
    private TextureRegion defaultTexture;

    public void resetTexture() {
        texture = defaultTexture;
    }

    public enum MoveState {
        WALK,
        RUN,
        STATIC
    }

    /**
     * The friction of this ball
     */
    private static final float DEFAULT_FRICTION = 0.1f;
    /**
     * The restitution of this ball
     */
    private static final float DEFAULT_RESTITUTION = 0.4f;
    /**
     * The thrust factor to convert player input into thrust
     */
    private static final float DEFAULT_THRUST = 10.0f;
    /**
     * The impulse for the character boost
     */
    private static final float BOOST_IMP = 100.0f;
    /**
     * The amount to slow the character down
     */
    private static final float MOTION_DAMPING = 25f;

    private static final int BOOST_FRAMES = 20;

    private static final int COOLDOWN_FRAMES = 60;

    public MoveState state;
    private int boosting;
    private int cooldown;

    /**
     * Cache object for transforming the force according the object angle
     */
    public Affine2 affineCache = new Affine2();
    private boolean isAlive = true;
    private int spawnCooldown;

    /**
     * The force to apply to this rocket
     */
    private Vector2 impulse;
    private Vector2 boost;

    private String team;

    @Override
    public void setTexture(TextureRegion value) {
        if (defaultTexture == null) {
            defaultTexture = value;
        }
        super.setTexture(value);
    }

    private Vector2 homeLoc;

    public BallModel(float x, float y, float width, float height, String team) {
        super(x, y, width, height);
        homeLoc = new Vector2(x, y);
        impulse = new Vector2();
        boost = new Vector2();
        cooldown = 0;
        boosting = 0;
        setDensity(DEFAULT_DENSITY);
        setFriction(DEFAULT_FRICTION);
        setRestitution(DEFAULT_RESTITUTION);
        setName("ball");
        setOrientation(Orientation.VERTICAL);
        setBullet(true);

        this.team = team;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public Vector2 getHomeLoc() {
        return homeLoc;
    }

    public boolean item;

    public void setHomeLoc(Vector2 homeLoc) {
        this.homeLoc = homeLoc;
    }

    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setLinearDamping(getDamping());
        body.setFixedRotation(true);
        return true;
    }

    public void applyImpulse() {
        if (!isActive()) {
            return;
        }

        body.applyLinearImpulse(impulse.nor().scl(getThrust()).add(boost.nor().scl(BOOST_IMP)), getPosition(), true);
        boost.setZero();
    }

    public Vector2 getImpulse() { return impulse; }

    public void setIX(float value) {
        impulse.x = value;
    }

    public void setIY(float value) {
        impulse.y = value;
    }

    public float getThrust() {
        return DEFAULT_THRUST;
    }

    public float getDamping() {
        return MOTION_DAMPING;
    }

    public void setBoostImpulse(float hori, float vert) {
        if (cooldown > 0 || item) { return; }
        state = MoveState.RUN;
        boosting = BOOST_FRAMES;
        cooldown = COOLDOWN_FRAMES;
        boost.x = hori;
        boost.y = vert;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
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
        item = false;
        resetTexture();

        setLinearVelocity(Vector2.Zero);
    }

    public void setWalk() {
        if (boosting > 0) { return; }
        state = MoveState.WALK;

    }

    public void setStatic() {
        if (boosting > 0) { return; }
        state = MoveState.STATIC;
    }

    public void cooldown() {
        cooldown = Math.max(0, cooldown - 1);
        boosting = Math.max(0, boosting - 1);
    }

    public void resetBoosting() {
        boosting = 0;
    }
}

