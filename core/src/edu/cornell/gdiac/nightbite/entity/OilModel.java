package edu.cornell.gdiac.nightbite.entity;

import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.util.FilmStrip;

public class OilModel extends ImmovableModel {
    /* How fast we change frames (one frame per 16 calls to update */
    private static final float ANIMATION_SPEED = 0.0625f;
    /* The number of animation frames in our spill filmstrip */
    private static final float NUM_FRAMES_SPILL = 12;
    /* Current animation frame */
    private float frame;
    /* Expected timestep age of oil spilling */
    private int spillingAge = 170;

    private FilmStrip defaultTexture = Assets.OIL_SPILLING;

    public OilModel(float x, float y){
        super(x, y, 0);
        setSensor(true);
        setTexture(defaultTexture);
    }

    public void update(float delta) {
        super.update(delta);

        if (spillingAge == 0) {// Done with spilling animation
            setTexture(Assets.OIL_TILE);
        } else { // Animate spilling
            spillingAge--;
            frame += ANIMATION_SPEED;
            if (frame >= NUM_FRAMES_SPILL) { frame -= NUM_FRAMES_SPILL; }
            ((FilmStrip) texture).setFrame((int) frame);
        }
    }
}
