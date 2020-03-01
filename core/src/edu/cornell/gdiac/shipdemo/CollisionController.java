/*
 * CollisionController.java
 *
 * Unless you are making a point-and-click adventure game, every single
 * game is going to need some sort of collision detection.  In a later
 * lab, we will see how to do this with a physics engine. For now, we use
 * custom physics.
 *
 * This class is an example of subcontroller.  A lot of this functionality
 * could go into GameMode (which is the primary controller).  However, we
 * have factored it out into a separate class because it makes sense as a
 * self-contained subsystem.  Note that this class needs to be aware of
 * of all the models, but it does not store anything as fields.  Everything
 * it needs is passed to it by the parent controller.
 *
 * This class is also an excellent example of the perils of heap allocation.
 * Because there is a lot of vector mathematics, we want to make heavy use
 * of the Vector2 class.  However, every time you create a new Vector2
 * object, you must allocate to the heap.  Therefore, we determine the
 * minimum number of objects that we need and pre-allocate them in the
 * constructor.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Controller implementing simple game physics.
 * <p>
 * This is the simplest of physics engines.  In later labs, we
 * will see how to work with more interesting engines.
 */
public class CollisionController {

    /**
     * Impulse for giving collisions a slight bounce.
     */
    public static final float COLLISION_COEFF = 0.1f;

    /**
     * Caching object for computing normal
     */
    private Vector2 normal;

    /**
     * Caching object for computing net velocity
     */
    private Vector2 velocity;

    /**
     * Caching object for intermediate calculations
     */
    private Vector2 temp;

    /**
     * Contruct a new controller.
     * <p>
     * This constructor initializes all the caching objects so that
     * there is no heap allocation during collision detection.
     */
    public CollisionController() {
        velocity = new Vector2();
        normal = new Vector2();
        temp = new Vector2();
    }

    /**
     * Handles collisions between ships or photons, causing them to bounce off one another.
     * <p>
     * This method updates the velocities of both ships: the collider and the
     * collidee. Therefore, you should only call this method for one of the
     * ships, not both. Otherwise, you are processing the same collisions twice.
     *
     * @param entity1 First entity in candidate collision
     * @param entity2 Second entity in candidate collision
     */
    public void checkForCollision(Entity entity1, Entity entity2) {
        // Calculate the normal of the (possible) point of collision
        normal.set(entity1.getPosition()).sub(entity2.getPosition());
        float distance = normal.len();
        float impactDistance = (entity1.getDiameter() + entity2.getDiameter()) / 2f;
        normal.nor();

        // If this normal is too small, there was a collision
        if (distance < impactDistance && !entity1.getColor().equals(entity2.getColor())) {
            // "Roll back" time so that the ships are barely touching (e.g. point of impact).
            // We need to use temp, as the method scl would change the contents of normal!
            temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d1 - dist)/2
            entity1.getPosition().add(temp);

            temp.set(normal).scl((impactDistance - distance) / 2);  // normal * (d2 - dist)/2
            entity2.getPosition().sub(temp);

            // Now it is time for Newton's Law of Impact.
            // Convert the two velocities into a single reference frame
            velocity.set(entity1.getVelocity()).sub(entity2.getVelocity()); // v1-v2

            // Compute the impulse (see Essential Math for Game Programmers)
            float impulse = (-(1 + COLLISION_COEFF) * normal.dot(velocity)) /
                    (normal.dot(normal) * (1 / entity1.getMass() + 1 / entity2.getMass()));

            // Change velocity of the two ships using this impulse
            temp.set(normal).scl(impulse / entity1.getMass());
            entity1.getVelocity().add(temp);

            temp.set(normal).scl(impulse / entity2.getMass());
            entity2.getVelocity().sub(temp);
        }
    }

    /**
     * Wrap the ship's position around the bounds of the window.
     *
     * @param player They player's ship which may have collided
     * @param bounds The rectangular bounds of the playing field
     */
    public void wrapAroundBounds(Player player, Rectangle bounds) {
        //Ensure the ship doesn't go out of view. Wrap around walls.
        Vector2 pos = player.getPosition();

        if (player.getPosition().x <= bounds.x) {
            pos.x += bounds.width;
        } else if (player.getPosition().x >= bounds.width) {
            pos.x -= bounds.width;
        }

        if (player.getPosition().y <= bounds.y) {
            pos.y += bounds.height;
        } else if (player.getPosition().y >= bounds.height) {
            pos.y -= bounds.height;
        }

        player.setPosition(pos);
    }
}