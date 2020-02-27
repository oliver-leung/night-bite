package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public interface Entity {
    Vector2 getPosition();

    Vector2 getVelocity();

    float getDiameter();

    float getMass();

    Color getColor();
}
