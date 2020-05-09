package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.nightbite.obstacle.SimpleObstacle;
import edu.cornell.gdiac.util.FilmStrip;

public class HumanoidModel extends SimpleObstacle {
    /** Prevents humanoids from flying around */
    private static final float MOTION_DAMPING = 25f;
    /** How fast we change frames (one frame per 8 calls to update */
    private static final float ANIMATION_SPEED = 0.125f;
    /** The number of animation frames in our falling filmstrip */
    private static final float NUM_FRAMES_FALL = 6;
    /** Small distance between fixtures to avoid overlapping */
    private static float SEAM_EPSILON = 0.01f;
    private final float DENSITY = 10f;
    private final float FRICTION = 0.1f;
    private final float RESTITUTION = 0.4f;
    /** Steps between switching walk animation frames */
    public int walkCounter = 0;
    /** Steps between switching falling animation frames */
    public int fallCounter = 0;
    /** Textures */
    public FilmStrip defaultTexture;
    public FilmStrip fallTexture;
    /** Texture tint */
    public Color tint = new Color(Color.WHITE);
    protected Vector2 dimension;
    /** Whether this humanoid is alive */
    protected boolean isAlive = true;
    private Vector2 cache = new Vector2();
    private PolygonShape hitBoxCore = new PolygonShape();
    private CircleShape[] hitBoxEdge = new CircleShape[]{
            new CircleShape(),
            new CircleShape()
    };
    private PolygonShape feet = new PolygonShape();
    private float[] vertices = new float[8];
    private Fixture[] capsuleFixtures = new Fixture[3];
    private Fixture feetFixture;
    private Rectangle coreBounds = new Rectangle();
    private Rectangle feetBounds = new Rectangle();
    private Vector2 feetPositionCache = new Vector2();
    private Rectangle feetRectangleCache = new Rectangle();
    private Vector2 dimensionCache = new Vector2();
    /** Respawn position */
    private Vector2 homePosition;
    /** Time until humanoid respawn */
    private int respawnCooldown;
    private int DEFAULT_RESPAWN_COOLDOWN = 60;
    /* Keeps track of the falling animation frame */
    private float fallFrame = 0f;
    /** The previous horizontal direction of the humanoid */
    private float prevHoriDir = -1f;

    public HumanoidModel(float x, float y, float width, float height, FilmStrip texture, FilmStrip fallTexture) {
        super(x, y);
        setBullet(true);
        setDensity(DENSITY);
        setFriction(FRICTION);
        setRestitution(RESTITUTION);

        this.texture = texture;
        this.fallTexture = fallTexture;
        setCurrentTexture(texture);

        // TODO: FIX DIMENSION
        dimension = new Vector2(width, height);
        resize(width, height);
    }

    /** Gets whether this humanoid is alive */
    public boolean isAlive() {
        return isAlive;
    }

    /** Set the liveness state of this humanoid */
    public void setAlive(boolean alive) {
        isAlive = alive;
        if (isAlive) {
            tint.a = 1.0f;
        }
    }

    public void setRespawnCooldown(int cooldown) {
        DEFAULT_RESPAWN_COOLDOWN = cooldown;
    }

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
        defaultTexture.setFrame((walkCounter / 5) % defaultTexture.getSize());
        if (prevHoriDir == 1) {
            defaultTexture.flip(true, false);
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

        tint.sub(0,0,0, 0.02f); // Fade-out effect

        ((FilmStrip) texture).setFrame((int) fallFrame);
        if (prevHoriDir == 1) {
            texture.flip(true, false);
        }
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

    public Vector2 getHomePosition() {
        return homePosition;
    }

    public void setHomePosition(Vector2 position) {
        homePosition = position;
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
    public void respawn() {
        setLinearVelocity(Vector2.Zero);
        if (respawnCooldown == 0) {
            respawnCooldown = DEFAULT_RESPAWN_COOLDOWN;
        }
        respawnCooldown--;
        if (respawnCooldown == 0) {
            setPosition(homePosition);
            setAlive(true);
            resetTexture();
            draw = true;
        }
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

        float feetWidth = width * 0.2f;
        float feetHeight = height * 0.1f;
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
        fixture.filter.maskBits = 0x002 | 0x0020 | 0x0001;
        fixture.filter.categoryBits = Obstacle.HITBOX;
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
        fixture.filter.categoryBits = Obstacle.WALKBOX;
        fixture.filter.maskBits = 0x0004 | 0x0008;
        feetFixture = body.createFixture(fixture);
        feetFixture.setUserData(HitArea.WALKBOX);

//        System.out.println(feetFixture.getFilterData().maskBits);
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

        canvas.drawPhysics(feet, Color.WHITE, getX(), getY() - dimension.y/2 - feetBounds.y, getAngle(), drawScale.x, drawScale.y);
        canvas.drawPoint(getX() * drawScale.x,(getY() - dimension.y - feetBounds.y + feetBounds.width/2) * drawScale.y, Color.PINK);
    }

    public float getWidth() {
        return dimension.x;
    }

    public float getHeight() {
        return dimension.y;
    }

    public Vector2 getDimension() {
        dimensionCache.set(dimension);
        return dimensionCache;
    }

    public Vector2 getFeetPosition() {
        feetPositionCache.set(getX(), getY() - dimension.y - feetBounds.y + feetBounds.width/2);
        return feetPositionCache;
    }

    // TODO
    public Rectangle getFeetRectangle() {
        feetRectangleCache.set(getX() - feetBounds.width/2, getY() - dimension.y/2 - feetBounds.y - 0.5f,
                feetBounds.width, feetBounds.height);
        return feetRectangleCache;
    }

    @Override
    public void draw(GameCanvas canvas) {
        if (texture != null) {
            canvas.draw(texture, tint, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y,
                    getAngle(), actualScale.x, actualScale.y);
        }
    }

    public enum HitArea {
        HITBOX,
        WALKBOX
    }
}
