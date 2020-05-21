package edu.cornell.gdiac.nightbite.entity;

import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.util.FilmStrip;

public class OilModel extends ImmovableModel {
    /* How fast we change frames (one frame per 16 calls to update */
    private static final float ANIMATION_SPEED = 0.0625f;
    /* The number of animation frames in our spill filmstrip */
    private static final float NUM_FRAMES_SPILL = 11;
    /* Current animation frame */
    private float frame;
    /* Expected timestep age of oil spilling */
    private int spillingAge = 170;

    private FilmStrip defaultTexture;

    public OilModel(float x, float y) {
        super(x, y, 0);
        setSensor(true);
        setTexture(Assets.getFilmStrip("item/oil_64_filmstrip.png"));
    }

    public void setTexture(FilmStrip texture) {
        if (defaultTexture == null) {
            defaultTexture = texture;
        }
        super.setTexture(texture);
    }

    // Ugly, and also being done in HoleModel so that it draws below everything else
    @Override
    public float getBottom() {
        return Float.POSITIVE_INFINITY - 1;
    }

    public boolean isSpilled() {
        return spillingAge == 0;
    }

    public void update(float delta) {
        super.update(delta);

        if (spillingAge == 0) {// Done with spilling animation
            setTexture(Assets.getFilmStrip("item/oiltile_64.png"));
        } else { // Animate spilling
            spillingAge--;
            frame += ANIMATION_SPEED;
            if (frame >= NUM_FRAMES_SPILL) { frame -= NUM_FRAMES_SPILL; }
            ((FilmStrip) texture).setFrame((int) frame);
        }
    }
}
