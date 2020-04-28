package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.obstacle.SimpleObstacle;
import edu.cornell.gdiac.util.FilmStrip;

public class HumanoidModel extends SimpleObstacle {

    private static float SEAM_EPSILON = 0.01f;
    protected Vector2 dimension;
    private Vector2 cache;
    private PolygonShape hitBoxCore;
    private CircleShape[] hitBoxEdge;
    private PolygonShape feet;
    private float[] vertices;
    private Fixture[] capsuleFixtures;
    private Fixture feetFixture;
    private Rectangle coreBounds;
    private Rectangle feetBounds;

    /** Whether this humanoid is alive */
    protected boolean isAlive;

    /** Time until humanoid respawn */
    private int respawnCooldown;

    /* How fast we change frames (one frame per 8 calls to update */
    private static final float ANIMATION_SPEED = 0.125f;
    /* The number of animation frames in our falling filmstrip */
    private static final float NUM_FRAMES_FALL = 6;
    /* Keeps track of the falling animation frame */
    private float fallFrame;

    /** Steps between switching walk animation frames */
    public int walkCounter;
    /** Steps between switching falling animation frames */
    public int fallCounter;

    /** The previous horizontal direction of the humanoid */
    private float prevHoriDir;

    /** Textures */
    public FilmStrip defaultTexture;
    public FilmStrip fallTexture;

    /** Gets whether this humanoid is alive */
    public boolean isAlive() { return isAlive; }
    /** Set the liveness state of this humanoid */
    public void setAlive(boolean alive) { isAlive = alive; }

    /** Kill this humanoid and set its texture to falling */
    public void setDead() {
        setCurrentTexture(fallTexture);
        setAlive(false);
    }

    /** Gets previous horizontal direction */
    public float getPrevHoriDir() { return prevHoriDir; }
    /** Sets previous horizontal direction */
    public void setPrevHoriDir(float dir) { prevHoriDir = dir; }

    /**
     * Sets the current texture of this humanoid
     */
    public void setCurrentTexture(FilmStrip texture) {
        if (defaultTexture == null) { defaultTexture = texture; }
        setTexture(texture);
    }

    /**
     * Sets the current texture to walking. Changes frames every 10 steps.
     */
    public void setWalkTexture() {
        if (walkCounter % 20 == 0) {
            ((FilmStrip) texture).setFrame(1);
            if (prevHoriDir == 1) {
                texture.flip(true, false);
            }
        } else if (walkCounter % 20 == 10) {
            ((FilmStrip) texture).setFrame(0);
            if (prevHoriDir == 1) {
                texture.flip(true, false);
            }
        }
        walkCounter++;
    }

    /**
     * If the humanoid is not moving, set the texture to one frame of the walk texture.
     */
    public void setStaticTexture() {
        walkCounter = 0;
        ((FilmStrip) texture).setFrame(0);
        if (prevHoriDir == 1) {
            texture.flip(true, false);
        }
    }

    /**
     * If the humanoid is dead, set the texture to falling
     */
    public void setFallingTexture() {
        fallCounter++;
        fallFrame += ANIMATION_SPEED;
        if (fallFrame >= NUM_FRAMES_FALL) { fallFrame -= NUM_FRAMES_FALL; }
        ((FilmStrip) texture).setFrame((int) fallFrame);
        if (prevHoriDir == 1) {
            texture.flip(true, false);
        }
    }

    public HumanoidModel(float x, float y, float width, float height, FilmStrip texture, FilmStrip fallTexture) {
        super(x, y);

        this.texture = texture;
        this.fallTexture = fallTexture;
        setCurrentTexture(texture);

        isAlive = true;

        walkCounter = 0;
        fallCounter = 0;
        fallFrame = 0f;
        prevHoriDir = -1f;

        // TODO: FIX DIMENSION
        dimension = new Vector2(width, height);
        cache = new Vector2();
        hitBoxCore = new PolygonShape() ;
        hitBoxEdge = new CircleShape[] {
                new CircleShape(),
                new CircleShape()
        };
        feet = new PolygonShape();
        vertices = new float[8];
        capsuleFixtures = new Fixture[3];
        coreBounds = new Rectangle();
        feetBounds = new Rectangle();

        resize(width, height);
    }

    /**
     * Resets the current texture to the default (walking) texture
     */
    public void resetTexture() {
        setTexture(defaultTexture);
    }

    /**
     * Flips the current texture horizontally
     */
    public void flipTexture() {
        texture.flip(true, false);
    }

    /**
     * Respawn this humanoid at the specified position after 60 steps
     */
    public void respawn(Vector2 pos) {
        if (respawnCooldown == 0) {
            respawnCooldown = 60;
        }
        respawnCooldown--;
        if (respawnCooldown == 0) {
            setPosition(pos);
            setAlive(true);
            resetTexture();
            draw = true;
        }

        setLinearVelocity(Vector2.Zero);
    }

    private void resize(float width, float height) {
        float radius = width / 2;
        float x = -width / 2 + SEAM_EPSILON;
        float y = -height / 2 + radius;
        float capHeight = height - width;
        float capWidth = width - 2 * SEAM_EPSILON;

        vertices[0] = x;
        vertices[1] = y;
        vertices[2] = x;
        vertices[3] = y + capHeight;
        vertices[4] = x + capWidth;
        vertices[5] = y + capHeight;
        vertices[6] = x + capWidth;
        vertices[7] = y;


        hitBoxCore.set(vertices);

        hitBoxEdge[0].setRadius(radius);
        cache.set(0, y+capHeight);
        hitBoxEdge[0].setPosition(cache);
        cache.set(0, y);
        hitBoxEdge[1].setRadius(radius);
        hitBoxEdge[1].setPosition(cache);

        coreBounds.set(x, y, capWidth, capHeight);

        // Feet has half the width of the capsule and is the
        // lowest .2 of the sprite

        float feetWidth = width * 0.5f;
        float feetHeight = height * 0.2f;
        x = -feetWidth/2;
        y = - height/2;

        vertices[0] = x;
        vertices[1] = y;
        vertices[2] = x;
        vertices[3] = y + feetHeight;
        vertices[4] = x + feetWidth;
        vertices[5] = y + feetHeight;
        vertices[6] = x + feetWidth;
        vertices[7] = y;

        feet.set(vertices);

        feetBounds.set(x, y, feetWidth, feetHeight);

        markDirty(true);

    }

    @Override
    protected void createFixtures() {
        if (body == null) {
            return;
        }

        releaseFixtures();

        float defaultDensity = fixture.density;

        // TODO: Capsule Fixtures should have collision filters

        fixture.shape = hitBoxEdge[0];
        fixture.density /= 2;
        // GROUP INDEX MEANS CAPSULE WILL NOT COLLIDE WITH IMMOVABLE OBSTACLES
        fixture.filter.groupIndex = -1;
        capsuleFixtures[0] = body.createFixture(fixture);

        fixture.shape = hitBoxCore;
        fixture.density = defaultDensity;
        capsuleFixtures[1] = body.createFixture(fixture);

        fixture.shape = hitBoxEdge[1];
        fixture.density /= 2;
        capsuleFixtures[2] = body.createFixture(fixture);

        for (Fixture f : capsuleFixtures) {
            f.setUserData(HitArea.HITBOX);
        }

        fixture.shape = feet;
        fixture.density = defaultDensity;
        fixture.filter.groupIndex = 0;
        feetFixture = body.createFixture(fixture);
        feetFixture.setUserData(HitArea.WALKBOX);

        markDirty(false);
    }

    @Override
    protected void releaseFixtures() {
        // Free capsule
        for (Fixture f : capsuleFixtures) {
            if (f != null) {
                body.destroyFixture(f);
            }
        }

        // Free feet
        if (feetFixture != null) {
            body.destroyFixture(feetFixture);
        }
    }

    @Override
    public void drawDebug(GameCanvas canvas) {
        // ASSUMES NO ROTATION

        canvas.drawPhysics(hitBoxCore, Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
        if (hitBoxEdge[0] != null) {
            canvas.drawPhysics(hitBoxEdge[0], Color.YELLOW, getX(), getY() + coreBounds.height / 2, drawScale.x, drawScale.y);
        }
        if (hitBoxEdge[1] != null) {
            canvas.drawPhysics(hitBoxEdge[1], Color.YELLOW, getX(), getY() - coreBounds.height / 2, drawScale.x, drawScale.y);
        }

        canvas.drawPhysics(feet, Color.WHITE, getX(), getY() - dimension.y / 2 - feetBounds.y, getAngle(), drawScale.x, drawScale.y);
    }

    public enum HitArea {
        HITBOX,
        WALKBOX
    }

}
