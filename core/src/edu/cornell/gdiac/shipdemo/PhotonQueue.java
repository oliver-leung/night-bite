/*
 * PhotonQueue.cs
 *
 * This class implements a "particle system" that manages the photons fired
 * by either ship in the game.  When a ship fires a photon, it adds it to this
 * particle system.  The particle system is responsible for moving (and drawing)
 * the photon particle.  It also keeps track of the age of the photon.  Photons
 * that are too old are deleted, so that they are not bouncing about the game
 * forever.
 *
 * The PhotonQueue is exactly what it sounds like: a queue. In this implementation
 * we use the circular array implementation of a queue (which you may have learned
 * in CS 2110).  Why do we do this when C# has perfectly good Collection classes?
 * Because in game programming it is considered bad form to have "new" statements
 * in an update or a graphics loop if you can easily avoid it.  Each "new" is
 * a potentially expensive memory allocation.  It is (often) much better to
 * allocate all the memory that you need at start-up, so that all you do is
 * assign variables during game time. If you notice, all the Photon objects
 * are declared and initialized in the constructor; we just reassign the fields
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * MonoGame version, 12/30/2013
 */
package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Model class representing an "particle system" of photons.
 * <p>
 * Note that the graphics resources in this class are static.  That
 * is because all photons share the same image file, and it would waste
 * memory to load the same image file for each photon.
 */
public class PhotonQueue {
    // Private constants to avoid use of "magic numbers"
    /**
     * Fixed velocity for a photon
     */
    private static final float PHOTON_VELOCITY = 5.0f;
    /**
     * Number of animation frames a photon lives before deleted
     */
    private static final int MAX_AGE = 60;
    /**
     * Maximum number of photons allowed on screen at a time.
     */
    private static final int MAX_PHOTONS = 512;

    /**
     * Graphic asset representing a single photon.
     */
    private static Texture texture;

    // QUEUE DATA STRUCTURES
    /**
     * Array implementation of a circular queue.
     */
    protected Photon[] queue;
    /**
     * Index of head element in the queue
     */
    protected int head;
    /**
     * Index of tail element in the queue
     */
    protected int tail;
    /**
     * Number of elements currently in the queue
     */
    protected int size;

    /**
     * Constructs a new (empty) PhotonQueue
     */
    public PhotonQueue() {
        // Construct the queue.
        queue = new Photon[MAX_PHOTONS];

        head = 0;
        tail = -1;
        size = 0;

        // "Predeclare" all the photons for efficiency
        for (int ii = 0; ii < MAX_PHOTONS; ii++) {
            queue[ii] = new Photon();
        }
    }

    /**
     * Returns the image for a single photon; reused by all photons.
     * <p>
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @return the image for a single photon; reused by all photons.
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Sets the image for a single photon; reused by all photons.
     * <p>
     * This value should be loaded by the GameMode and set there. However, we
     * have to be prepared for this to be null at all times
     *
     * @param value the image for a single photon; reused by all photons.
     */
    public void setTexture(Texture value) {
        texture = value;
    }

    /**
     * Adds a photon to the active queue.
     * <p>
     * When adding a photon, we assume that it is fired from a ship with the
     * given position, velocity, and facing (angle).  We could have a general
     * photon adding function, but this will make refactoring easier in
     * Exercise 9.
     * <p>
     * As all Photons are predeclared, this involves moving the head and the tail,
     * and reseting the values of the object in place.  This is a simple implementation
     * of a memory pool. It works because we delete objects in the same order that
     * we allocate them.
     *
     * @param position The velocity of the ship firing the photon
     * @param velocity The position of the ship firing the photon
     * @param angle    The facing of the ship firing the photon
     * @param color    The color of the ship
     */
    public void addPhoton(Vector2 position, Vector2 velocity, float angle, Color color) {
        // Determine direction and velocity of the photon.
        float fire_x = velocity.x + (float) Math.cos(Math.toRadians(angle)) * PHOTON_VELOCITY;
        float fire_y = velocity.y + (float) -Math.sin(Math.toRadians(angle)) * PHOTON_VELOCITY;

        // Check if any room in queue.
        // If maximum is reached, remove the oldest photon.
        if (size == MAX_PHOTONS) {
            head = ((head + 1) % MAX_PHOTONS);
            size--;
        }

        // Add a new photon at the end.
        // Already declared, so just initialize.
        tail = ((tail + 1) % MAX_PHOTONS);
        queue[tail].allocate(position.x, position.y, fire_x, fire_y, color);
        size++;
    }

    /**
     * Moves all the photons in the active queu.
     * <p>
     * Each photon is advanced according to its velocity. Photons out of bounds are
     * rebounded into view. Photons which are too old are deleted.
     */
    public void move(Rectangle bounds) {
        // First, delete all old photons.
        // INVARIANT: Photons are in queue in decending age order.
        // That means we just remove the head until the photons are young enough.
        while (size > 0 && queue[head].age > MAX_AGE) {
            // As photons are predeclared, all we have to do is move head forward.
            head = ((head + 1) % MAX_PHOTONS);
            size--;
        }

        // Now, step through each active photon in the queue.
        for (int ii = 0; ii < size; ii++) {
            // Find the position of this photon.
            int idx = ((head + ii) % MAX_PHOTONS);

            // Move the photon according to velocity.
            queue[idx].move(bounds);
        }
    }

    /**
     * Draws the photons to the drawing canvas.
     * <p>
     * This method uses additive blending, which is set before this method is
     * called (in GameMode).
     *
     * @param canvas The drawing canvas.
     */
    public void draw(GameCanvas canvas) {
        if (texture == null) {
            return;
        }

        // Get photon texture origin
        float ox = texture.getWidth() / 2.0f;
        float oy = texture.getHeight() / 2.0f;

        // Step through each active photon in the queue.m
        for (int ii = 0; ii < size; ii++) {
            // Find the position of this photon.
            int idx = ((head + ii) % MAX_PHOTONS);

            // How big to make the photon.  Decreases with age.
            float sizeScale = 1.25f - (float) queue[idx].age * 0.5f / (float) MAX_AGE;

            // Photon color, which changes continuously.
            Color tint = new Color(Color.WHITE);
            float colorScale = 1 - (float) queue[idx].age / (float) MAX_AGE;
            if (queue[idx].color.equals(new Color(1.0f, 0.25f, 0.25f, 1.0f))) {
                tint.mul(1, colorScale, colorScale, 1);
            } else {
                tint.mul(colorScale, colorScale, 1, 1);
            }

            // Use this information to draw.
            canvas.draw(texture, tint, ox, oy, queue[idx].x, queue[idx].y, 0, sizeScale, sizeScale);
        }
    }

    public enum PhotonType {
        RED, BLUE
    }

    /**
     * An inner class that represents a single Photon.
     * <p>
     * To count down on memory references, the photon is "flattened" so that
     * it contains no other objects.
     */
    public class Photon implements Entity {
        /**
         * X-coordinate of photon position
         */
        public float x;
        /**
         * Y-coordinate of photon position
         */
        public float y;
        /**
         * X-coordinate of photon velocity
         */
        public float vx;
        /**
         * X-coordinate of photon velocity
         */
        public float vy;
        /**
         * Age for the photon in frames (for decay)
         */
        public int age;
        /**
         * Color of the ship that fired the photon
         */
        public Color color;

        /**
         * Creates a new empty photon with age -1.
         * <p>
         * Photons created this way "do not exist".  This constructor is
         * solely for preallocation.  To actually use a photon, use the
         * allocate() method.
         */
        public Photon() {
            this.x = 0.0f;
            this.y = 0.0f;
            this.vx = 0.0f;
            this.vy = 0.0f;
            this.age = -1;
        }

        /**
         * Allocates a photon by setting its position and velocity.
         * <p>
         * A newly allocated photon starts with age 0.
         *
         * @param x     The x-coordinate of the position
         * @param y     The y-coordinate of the position
         * @param vx    The x-coordinate of the velocity
         * @param vy    The y-coordinate of the velocity
         * @param color The type of the photon (i.e. the color of the ship that shot it)
         */
        public void allocate(float x, float y, float vx, float vy, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.age = 0;
            this.color = color;
        }

        /**
         * Moves the photon within the given boundary.
         * <p>
         * If the photon goes outside of bounds, it wraps to the next edge.
         * This method also advances the age of the photon.
         */
        public void move(Rectangle bounds) {
            x += vx;
            y += vy;

            if (x <= bounds.x) {
                x += bounds.width;
            } else if (x >= bounds.width) {
                x -= bounds.width;
            }

            if (y <= bounds.y) {
                y += bounds.height;
            } else if (y >= bounds.height) {
                y -= bounds.height;
            }

            // Finally, advance the age of the photon.
            age++;
        }

        @Override
        public Vector2 getPosition() {
            return new Vector2(x, y);
        }

        @Override
        public Vector2 getVelocity() {
            return new Vector2(vx, vy);
        }

        @Override
        public float getDiameter() {
            return 30;
        }

        @Override
        public float getMass() {
            return 0.1f;
        }

        @Override
        public Color getColor() {
            return color;
        }
    }
}