package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.LightSource;
import edu.cornell.gdiac.util.SoundController;

import java.util.ArrayList;

public class FirecrackerModel extends BoxObstacle {

    /* Expected timestep age of firecracker before becoming lit */
    private static final int AGE = 1;
    /* Expected timestep age of firecracker before detonating */
    private static final int LIT_AGE = 43;
    /* Expected timestep age of firecracker before becoming destroyed */
    private static final int DETONATING_AGE = 80;
    /* Expected timestep age of firecracker in which players will be pushed */
    private static final int DETONATING_TIME = 10;

    /* How far out will humanoid bodies be affected by the detonation */
    private static final float BLAST_RADIUS = 0.7f;

    /* Physics constants */
    private static final float MOVABLE_OBJECT_DENSITY = 1.0f;
    private static final float MOVABLE_OBJECT_FRICTION = 0.1f;
    private static final float MOVABLE_OBJECT_RESTITUTION = 0.4f;
    private static final float THROW_FORCE = 6f;
    private static final float MOTION_DAMPING = 30f;
    protected static final float KNOCKBACK_IMPULSE = 50f;

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

    /* Reference to the existing world but also like jesus this is bad practice */
    private World world;

    /** The light that appears when a firecracker is detonating */
    private LightSource light;
    /** Transparency of the explosion light */
    private float lightAlpha = 0.75f;

    /** Min velocity for firecracker to be considered 'not flying' */
    private final float stop_velocity = 1f;

    /** List of holes the firecracker is in contact with */
    private ArrayList<HoleModel> contactHoles;
    public void addContactHole(HoleModel hole) { contactHoles.add(hole); }
    public void removeContactHole(HoleModel hole) { contactHoles.remove(hole); }

    /** Indicators for fading out firecrackers when it lands in a hole */
    private boolean fadeOut = false;
    private int fadeOutFrame;
    private static final int NUM_FRAMES_FADE_OUT = 15;

    /** Texture tint */
    public Color tint = new Color(Color.WHITE);

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
    public FirecrackerModel(World world, float x, float y, float width, float height) {
        super(x, y, width, height);

        this.texture = Assets.getFilmStrip("item/firecracker_64.png");
        litTexture = Assets.getFilmStrip("item/firecracker_fuse_64_fs.png");
        detTexture = Assets.getFilmStrip("item/firecracker_detonating_64_fs.png");
        setTexture(this.texture);

        setDensity(MOVABLE_OBJECT_DENSITY);
        setFriction(MOVABLE_OBJECT_FRICTION);
        setRestitution(MOVABLE_OBJECT_RESTITUTION);

        setSensor(true);
        // TODO shouldn't need - firecracker should move relatively slowly
        setBullet(true);
        setName("firecracker");

        contactHoles = new ArrayList<HoleModel>();

        age = AGE;
        lit_age = LIT_AGE;
        detonating_age = DETONATING_AGE;

        lit = false;
        detonating = false;

        frame = 0f;

        categoryBits = 0x0020;
        maskBits = 0x0008;

        this.world = world;
    }

    /**
     * Called when the enemy AI chooses to throw the firecracker at the player.
     */
    public void throwItem(Vector2 impulse) {
        getBody().applyLinearImpulse(impulse.scl(THROW_FORCE), getPosition(), true);
    }

    /**
     * Callback for AABB query of nearby humanoids
     * For use with blastSurroundingBodies()
     */
    static class DetectionCallback implements QueryCallback {
        ArrayList<Body> foundBodies = new ArrayList<>();

        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixture.getUserData() == HumanoidModel.HitArea.HITBOX) {
                foundBodies.add(fixture.getBody());
            }
            return true;
        }
    }

    /**
     * Controls the push back of humanoid models within range of the blast.
     */
    public void blastSurroundingBodies(World world) {
        Vector2 pos = getPosition();
        DetectionCallback callback = new DetectionCallback();
        world.QueryAABB(callback, pos.x-BLAST_RADIUS, pos.y-BLAST_RADIUS, pos.x+BLAST_RADIUS, pos.y+BLAST_RADIUS);

        // Push back all humanoid bodies within radius
        for (Body b : callback.foundBodies) {

            // If the firecracker is detonating, player should be knocked back
            // TODO and stunned temporarily
            if (isDetonating()) {
                Vector2 blastDirection = b.getPosition().sub(this.getPosition()).nor();
                blastDirection = new Vector2(blastDirection.x, blastDirection.y);  // jank but will break without
                blastDirection.scl(KNOCKBACK_IMPULSE);
                b.applyLinearImpulse(blastDirection, b.getPosition(), true);
            }

            if (body.getUserData() instanceof EnemyModel) {
                EnemyModel e = ((EnemyModel) (body.getUserData()));
                e.forceReplan();
            }
        }
    }

    /** Sets the light for this body */
    public void setLight(LightSource light) {
        this.light = light;
    }

    /** Removes the light for this body */
    public void deactivateLight() {
        if (light != null) {
            light.setActive(false);
        }
    }

    /** To be called when fading out the detonation light */
    private void decrLightAlpha() {
        if (lightAlpha - 0.05 > 0) lightAlpha -= 0.05;
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

        if (fadeOut) { // In fade out mode
            if (fadeOutFrame == 0) { // When done fading out, remove firecracker & point light
               markRemoved(true);
               deactivateLight();
            }
            // Fade-out effect for firecracker & light
            decrLightAlpha();
            tint.sub(0,0,0, 0.03f);
            fadeOutFrame -= 1;
        } else if (!contactHoles.isEmpty() && Math.abs(getVX()) <= stop_velocity && Math.abs(getVY()) <= stop_velocity ) {
            // ^ Check that firecracker 'landed' and is in contact with holes
            Vector2 position = getPosition();
            for (HoleModel hole : contactHoles) {
                float holeOffsetX = hole.getDimension().cpy().x/2;
                float holeOffsetY = hole.getDimension().cpy().y/2;
                float holeLeft = hole.getX() - holeOffsetX;
                float holeRight = hole.getX() + holeOffsetX;
                float holeDown = hole.getY() - holeOffsetY;
                float holeUp = hole.getY() + holeOffsetY;

                // If center of firecracker is in a hole, remove firecracker
                if (position.x > holeLeft && position.x < holeRight && position.y > holeDown && position.y < holeUp) {
                    fadeOut = true;
                    fadeOutFrame = NUM_FRAMES_FADE_OUT;
                    break;
                }
            }
        }

        // Cold age countdown
        if (!lit && !detonating) {
            age--;
            if (age == 0) {
                lit = true;
                detonating = false;
                frame = 0f;
                setTexture(litTexture);
                String firecrackerSoundFile = "audio/firecracker.wav";
                SoundController.getInstance().play(firecrackerSoundFile, firecrackerSoundFile, false, Assets.VOLUME * 0.4f);
            }
        }

        // Lit age countdown
        else if (lit) {
            lit_age--;
            if (lit_age == 0) {
                lit = false;
                detonating = true;
                frame = 0f;
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
                deactivateLight();
            } else {
                frame += ANIMATION_SPEED;
                if (frame >= NUM_FRAMES_DET) {
                    frame -= NUM_FRAMES_DET;
                }
                ((FilmStrip) texture).setFrame((int) frame);
                if (light != null) {
                    light.setColor(0.15f, 0.05f, 0.0f, lightAlpha);
                    decrLightAlpha();
                    light.setDistance(3.0f);
                }
                // only push back during first moment of explosion
                if (detonating_age > DETONATING_AGE - DETONATING_TIME)
                    blastSurroundingBodies(world);
            }

        }
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.draw(texture,tint,origin.x,origin.y,getX() * drawScale.x, getY() * drawScale.y,
                    getAngle(),actualScale.x,actualScale.y);
        }
    }
}
