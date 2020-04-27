package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;
import edu.cornell.gdiac.util.FilmStrip;

public class FirecrackerModel extends BoxObstacle {

    /* Expected timestep age of firecracker before becoming lit */
    private static final int AGE = 128;
    /* Expected timestep age of firecracker before detonating */
    private static final int LIT_AGE = 112;
    /* Expected timestep age of firecracker before becoming destroyed */
    private static final int DETONATING_AGE = 80;

    /* Physics constants */
    private static final float MOVABLE_OBJECT_DENSITY = 1.0f;
    private static final float MOVABLE_OBJECT_FRICTION = 0.1f;
    private static final float MOVABLE_OBJECT_RESTITUTION = 0.4f;
    private static final float THROW_FORCE = 6f;
    private static final float MOTION_DAMPING = 30f;

    /* How fast we change frames (one frame per 16 calls to update */
    private static final float ANIMATION_SPEED = 0.0625f;
    /* The number of animation frames in our fuse filmstrip */
    private static final float NUM_FRAMES_LIT = 5;
    /* The number of animation frames in our detonating filmstrip */
    private static final float NUM_FRAMES_DET = 7;

    /* Age counter of firecracker while cold */
    private int age;
    /* Age counter of firecracker while lit */
    private int lit_age;
    /* Age counter of firecracker while detonating */
    private int detonating_age;

    /* Whether this firecracker's fuse is lit */
    private boolean lit;
    /* Whether this firecracker is detonating */
    private boolean detonating;

    /* Textures */
    private FilmStrip defaultTexture;
    private FilmStrip litTexture;
    private FilmStrip detTexture;

    /* Current animation frame */
    private float frame;


    /**
     * Get whether this firecracker is lit or not
     */
    public boolean isLit() { return this.lit; }

    /**
     * Get whether this firecracker is detonating or not
     */
    public boolean isDetonating() { return this.detonating; }

    /**
     * Set the texture of this firecracker
     */
    public void setTexture(FilmStrip texture) {
        if (defaultTexture == null) { defaultTexture = texture; }
        super.setTexture(texture);
    }

    /**
     * Construct a new firecracker
     */
    public FirecrackerModel(float x, float y, float width, float height) {
        super(x, y, width, height);

        this.texture = Assets.FIRECRACKER;
        litTexture = Assets.FIRECRACKER_LIT;
        detTexture = Assets.FIRECRACKER_DET;
        setTexture(this.texture);

        setDensity(MOVABLE_OBJECT_DENSITY);
        setFriction(MOVABLE_OBJECT_FRICTION);
        setRestitution(MOVABLE_OBJECT_RESTITUTION);

        setSensor(false);
        // TODO shouldn't need - firecracker should move relatively slowly
        setBullet(true);
        setName("firecracker");

        age = AGE;
        lit_age = LIT_AGE;
        detonating_age = DETONATING_AGE;

        lit = false;
        detonating = false;

        frame = 0f;
    }

    /**
     * Called when the enemy AI chooses to throw the firecracker at the player.
     */
    public void throwItem(Vector2 impulse) {
        getBody().applyLinearImpulse(impulse.scl(THROW_FORCE), getPosition(), true);
    }

    /**
     * Activate physics for this firecracker in the world
     */
    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setLinearDamping(MOTION_DAMPING);
        body.setFixedRotation(true);
        Filter f = geometry.getFilterData();
        f.groupIndex = 1;
        geometry.setFilterData(f);
        return true;
    }

    /**
     * Updates the state and age of this firecracker.
     * It progresses from cold -> lit -> detonating. Respective ages are updated accordingly.
     *
     * @param delta Number of seconds since last frame
     */
    public void update(float delta) {
        super.update(delta);

        // Cold age countdown
        if (!lit && !detonating) {
            age--;
            if (age == 0) {
                lit = true;
                detonating = false;
                frame = 0f;
                setTexture(litTexture);
            }
        }

        // Lit age countdown
        else if (lit) {
            lit_age--;
            if (lit_age == 0) {
                lit = false;
                detonating = true;
                setTexture(detTexture);
            } else {
                frame += ANIMATION_SPEED;
                if (frame >= NUM_FRAMES_LIT) { frame -= NUM_FRAMES_LIT; }
                ((FilmStrip) texture).setFrame((int) frame);
            }

        }

        // Detonating age countdown
        else {
            detonating_age--;
            if (detonating_age == 0) {
                detonating = false;
                markRemoved(true);
            } else {
                frame += ANIMATION_SPEED;
                if (frame >= NUM_FRAMES_DET) { frame -= NUM_FRAMES_DET; }
                ((FilmStrip) texture).setFrame((int) frame);
            }
        }
    }
}
