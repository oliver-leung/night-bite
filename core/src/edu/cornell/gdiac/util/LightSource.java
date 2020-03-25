package edu.cornell.gdiac.util;

/*
 * PointSource.java
 *
 * This is an alternate interface for PositionalLight from Box2DLights.  It is necessary
 * because Box2DLights is an extremely poorly designed API.  It is a very tight coupling
 * of model/controller/view that makes it hostile for decoupled data-driven design.
 * This interface does not break this coupling.  However, it does expose more of the
 * inner workings of lights, making it more appropriate to treat the lights as models.
 *
 * In fact, you will notice that most of the methods are get/set/attach.  This allows
 * us to consider lights as models.  However, we do have to have immediate access to
 * the RayHandler when we construct a light, so this limits some of our decoupling.
 *
 * Author: Walker M. White
 * Initial version, 3/12/2016
 */

import box2dLight.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Interface representing a single light source.
 *
 * The light source is attached to a rayhandler on creation and cannot reassigned.  The
 * light source should be removed (which has an implicit dispose) when it is no longer
 * needed.
 */
public interface LightSource {
    /** The minimum number of rays to function properly */
    public static final int MIN_RAYS = 3;
    /** The default color for all lights */
    public static final Color DEFAULT_COLOR = new Color(0.75f, 0.75f, 0.5f, 0.75f);

    /**
     * Returns true if this light is active
     *
     * An inactive light is not added to the render pass of the rayhandler.
     *
     * @return true if this light is active
     */
    public boolean isActive();

    /**
     * Sets whether this light is active
     *
     * An inactive light is not added to the render pass of the rayhandler.
     *
     * @param active whether this light is active
     */
    public void setActive(boolean active);

    /**
     * Returns the current color of this light
     *
     * Note that you can also use colorless light with shadows, e.g. (0,0,0,1).
     *
     * @return current color of this light
     */
    public Color getColor();

    /**
     * Sets the current color of this light
     *
     * Note that you can also use colorless light with shadows, e.g. (0,0,0,1).
     *
     * @param newColor	the color of this light
     */
    public void setColor(Color newColor) ;

    /**
     * Sets the current color of this light
     *
     * Note that you can also use colorless light with shadows, e.g. (0,0,0,1).
     *
     * @param r	 lights color red component
     * @param g  lights color green component
     * @param b  lights color blue component
     * @param a  lights shadow intensity
     */
    public void setColor(float r, float g, float b, float a);

    /**
     * Returns the ray distance of this light (without gamma correction)
     *
     * The minimum value is capped at 0.1f meter.
     *
     * @return the ray distance of this light (without gamma correction)
     */
    public float getDistance();

    /**
     * Sets the ray distance of this light (without gamma correction)
     *
     * The minimum value is capped at 0.1f meter.
     *
     * @param dist	the ray distance of this light (without gamma correction)
     */

    public void setDistance(float dist);

    /**
     * Returns the direction of this light in degrees
     *
     * The angle is measured from the right horizontal, as normal.  If the light
     * does not have a direction, this value is 0.
     *
     * @return the direction of this light in degrees
     */
    public float getDirection();

    /**
     * Sets the direction of this light in degrees
     *
     * The angle is measured from the right horizontal, as normal.  If the light
     * does not have a direction, this value is 0.
     *
     * @param directionDegree	the direction of this light in degrees
     */
    public void setDirection(float directionDegree);

    /**
     * Returns the starting position of light in world coordinates
     *
     * This is a copy of the position vector and not a reference.  Changing the
     * contents of this vector does nothing.
     *
     * @return the starting position of light in world coordinates
     */
    public Vector2 getPosition();

    /**
     * Returns the horizontal starting position of light in world coordinates
     *
     * @return the horizontal starting position of light in world coordinates
     */
    public float getX();

    /**
     * Returns the vertical starting position of light in world coordinates
     *
     * @return the vertical starting position of light in world coordinates
     */
    public float getY();

    /**
     * Sets the light starting position
     *
     * @param x the initial horizontal position
     * @param y the initial vertical position
     */
    public void setPosition(float x, float y);

    /**
     * Sets the light starting position
     *
     * This method does not retain a reference to the parameter.
     *
     * @param position the initial starting position
     */
    public void setPosition(Vector2 position);

    /**
     * Returns true if the light beams go through obstacles
     *
     * Allowing a light to penetrate obstacles reduces the CPU burden of light
     * about 70%.
     *
     * @return true if the light beams go through obstacles
     */
    public boolean isXray();

    /**
     * Sets whether the light beams go through obstacles
     *
     * Allowing a light to penetrate obstacles reduces the CPU burden of light
     * about 70%.
     *
     * @param xray whether the light beams go through obstacles
     */
    public void setXray(boolean xray);

    /**
     * Returns true if this light has static behavior
     *
     * Static lights do not get any automatic updates but setting any
     * parameters will update it. Static lights are useful for lights that you
     * want to collide with static geometry but ignore all the dynamic objects.
     *
     * This can reduce the CPU burden of light about 90%
     *
     * @return true if this light has static behavior
     */
    public boolean isStaticLight();

    /**
     * Sets whether this light has static behavior
     *
     * Static lights do not get any automatic updates but setting any
     * parameters will update it. Static lights are useful for lights that you
     * want to collide with static geometry but ignore all the dynamic objects.
     *
     * This can reduce the CPU burden of light about 90%
     *
     * @param staticLight whether this light has static behavior
     */
    public void setStaticLight(boolean staticLight);

    /**
     * Returns true if the tips of the light beams are soft.
     *
     * Soft light beams allow the player to see through thin obstacles, making this
     * type of shadow less desirable for many applications.  It does look pretty,
     * however.
     *
     * @return true if the tips of this light beams are soft
     */
    public boolean isSoft();

    /**
     * Sets whether the tips of the light beams are soft.
     *
     * Soft light beams allow the player to see through thin obstacles, making this
     * type of shadow less desirable for many applications.  It does look pretty,
     * however.
     *
     * @return soft whether tips of this light beams are soft
     */
    public void setSoft(boolean soft);

    /**
     * Returns the softness value for the beam tips.
     *
     * By default, this value is 2.5f
     *
     * @return the softness value for the beam tips.
     */
    public float getSoftShadowLength();

    /**
     * Sets the softness value for the beam tips.
     *
     * By default, this value is 2.5f
     *
     * @param softShadowLength the softness value for the beam tips.
     */
    public void setSoftnessLength(float softShadowLength);

    /**
     * Returns the number of rays set for this light
     *
     * The number of rays determines how realistic the light looks.  More rays will
     * decrease performance.  The number of rays cannot be less than MIN_RAYS.
     *
     * @return the number of rays set for this light
     */
    public int getRayNum();

    /// MEMORY MANAGEMENT
    /**
     * Adds a light to specified RayHandler
     *
     * It is only safe to attach a rayhandler if (1) there is no rayhandler
     * currently attached and (2) this light has not been disposed.
     *
     * @param rayHandler the RayHandler
     */
    public void add(RayHandler rayHandler);

    /**
     * Removes the light from the active RayHandler and disposes it
     *
     * A disposed light may not be reused.
     */
    public void remove();

    /**
     * Removes the light from the active RayHandler and disposes it if requested
     *
     * A disposed light may not be reused.
     *
     * @param doDispose whether to dispose the light
     */
    public void remove(boolean doDispose);


    /// PHYSICS METHODS
    /**
     * Returns the body assigned to this light
     *
     * If the light has position, but no body, this method returns null.
     *
     * @return the body assigned to this light
     */
    public abstract Body getBody();

    /**
     * Attaches light to specified body
     *
     * The light will automatically follow the body.  Note that the body rotation angle
     * is taken into account for the light offset and direction calculations.
     *
     * @param  body  the body to assign this light
     */
    public void attachToBody(Body body);

    /**
     * Attaches light to specified body
     *
     * The light will automatically follow the body.  Note that the body rotation angle
     * is taken into account for the light offset and direction calculations.
     *
     * @param  body  the body to assign this light
     * @param  dx 	 horizontal position in body coordinates
     * @param  dy 	 vertical position in body coordinates
     */
    public void attachToBody(Body body, float dx, float dy);

    /**
     * Attaches light to specified body
     *
     * The light will automatically follow the body.  Note that the body rotation angle
     * is taken into account for the light offset and direction calculations.
     *
     * @param  body 	the body to assign this light
     * @param  dx 		horizontal position in body coordinates
     * @param  dy 		vertical position in body coordinates
     * @param degrees	directional relative offset in degrees
     */
    public void attachToBody(Body body, float offsetX, float offSetY, float degrees);

    /**
     * Returns true if the attached body fixtures should be ignored during raycasting
     *
     * If this value is true, all the fixtures of attached body will be ignored and
     * will not create any shadows for this light. By default this is false.
     *
     * @erturn true if the attached body fixtures should be ignored during raycasting
     */
    public boolean getIgnoreAttachedBody();

    /**
     * Sets whether the attached body fixtures should be ignored during raycasting
     *
     * If this value is true, all the fixtures of attached body will be ignored and
     * will not create any shadows for this light. By default this is false.
     *
     * @param flag	whether the attached body fixtures should be ignored during raycasting
     */
    public void setIgnoreAttachedBody(boolean flag);

    /**
     * Returns the current contact filter for this light
     *
     * The contact filter defines which obstacles block the light, and which are see
     * through.  As a general rule, sensor objects should not block light beams.
     *
     * @return the current contact filter for this light
     */
    public Filter getContactFilter();

    /**
     * Sets the current contact filter for this light
     *
     * The contact filter defines which obstacles block the light, and which are see
     * through.  As a general rule, sensor objects should not block light beams.
     *
     * @param filter the current contact filter for this light
     */
    public void setContactFilter(Filter filter);

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
    public void setContactFilter(short categoryBits, short groupIndex, short maskBits);

    /**
     * Returns true if given point is inside of this light area
     *
     * @param x	the horizontal position of point in world coordinates
     * @param y the vertical position of point in world coordinates
     *
     * @return true if given point is inside of this light area
     */
    public boolean contains(float x, float y);

}
