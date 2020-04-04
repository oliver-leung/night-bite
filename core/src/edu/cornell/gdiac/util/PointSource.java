package edu.cornell.gdiac.util;

/*
 * PointSource.java
 *
 * This is an subclass of PointLight from Box2D.  As with PointLight, it is a light
 * that travels in all directions to a given radius.
 *
 * This class is an implementation of LightSource, and so it exposes certain internals
 * hidden in PointLight (e.g. protecteds that need direct access).  This helps with the
 * design as we can now treat this light as a proper model, as opposed to a poorly
 * designed controller.
 *
 * Author: Walker M. White
 * B2Lights version, 3/12/2016
 */

import box2dLight.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

/**
 * A light shaped as a circle with a given radius
 */
public class PointSource extends PointLight implements LightSource {
    /** The default distance for a point source light */
    public static float DEFAULT_DISTANCE = 15.0f;

    /** Copy of the collision filter.  Necessary because the original version is private */
    protected Filter collisions;

    /**
     * Creates light shaped as a circle with default radius, color and position.
     *
     * The default radius is DEFAULT_DISTANCE, while the default color is DEFAULT_COLOR
     * in LightSource.  The default position is the origin.
     *
     * RayHandler is NOT allowed to be null.  This is the source of many design problems.
     *
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     *
     * The soft shadow length is set to distance * 0.1f.  This is why it ignores thin
     * walls, and is not particularly useful.
     *
     * @param rayHandler	a non-null instance of RayHandler
     * @param rays			the number of rays
     */
    public PointSource(RayHandler rayHandler, int rays) {
        super(rayHandler, rays, DEFAULT_COLOR, DEFAULT_DISTANCE, 0f, 0f);
    }


    /**
     * Creates light shaped as a circle with the given radius, color and position.
     *
     * RayHandler is NOT allowed to be null.  This is the source of many design problems.
     *
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     *
     * The soft shadow length is set to distance * 0.1f.  This is why it ignores thin
     * walls, and is not particularly useful.
     *
     * @param rayHandler	a non-null instance of RayHandler
     * @param rays			the number of rays
     * @param color			the light color, or null for default
     * @param distance		the light radius
     * @param x				the horizontal position in world coordinates
     * @param y				the vertical position in world coordinates
     */
    public PointSource(RayHandler rayHandler, int rays, Color color, float distance, float x, float y) {
        super(rayHandler, rays, color, distance, x, y);
    }

    @Override
    /**
     * Returns the direction of this light in degrees
     *
     * The angle is measured from the right horizontal, as normal.  If the light
     * does not have a direction, this value is 0.
     *
     * @return the direction of this light in degrees
     */
    public float getDirection() {
        return direction;
    }

    @Override
    public Filter getContactFilter() {
        return collisions;
    }

    @Override
    /**
     * Sets the current contact filter for this light
     *
     * The contact filter defines which obstacles block the light, and which are see
     * through.  As a general rule, sensor objects should not block light beams.
     *
     * @param filter the current contact filter for this light
     */
    public void setContactFilter(Filter filter) {
        collisions = filter;
        super.setContactFilter(filter);
    }

    /**
     * Creates a new contact filter for this light with given parameters
     *
     * The contact filter defines which obstacles block the light, and which are see
     * through.  As a general rule, sensor objects should not block light beams.
     * See Filter for a complete description of these parameters.
     *
     * @param categoryBits	the category of this light (to allow objects to exclude this light)
     * @param groupIndex    the group index of the light, for coarse-grain filtering
     * @param maskBits      the mask of this light (to allow the light to exclude objects)
     */
    public void setContactFilter(short categoryBits, short groupIndex, short maskBits) {
        collisions = new Filter();
        collisions.categoryBits = categoryBits;
        collisions.groupIndex = groupIndex;
        collisions.maskBits = maskBits;
        super.setContactFilter(collisions);
    }
}