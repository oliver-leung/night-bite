package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.obstacle.SimpleObstacle;

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

    public HumanoidModel(float x, float y, float width, float height) {
        super(x, y);

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
