package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.WorldModel;
import edu.cornell.gdiac.nightbite.WorldModel;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.SoundController;

public class HomeModel extends ImmovableModel {

    private static String FX_DELIVER_FILE = "audio/delivered.wav";
    private final WorldModel worldModel;
    private String team;
    private int score;

    private FilmStrip flagFilmstrip = Assets.getFilmStrip("environment/flag_filmstrip_64.png", 44, 64);
    private int flagTicks = 0;

    /**
     * @param x          X position of the home
     * @param y          Y position of the home
     * @param team       Team that the home belongs to
     * @param worldModel WorldModel that the home belongs to
     */
    public HomeModel(float x, float y, String team, String stallTexture, WorldModel worldModel) {
        super(x, y, 0);
        this.texture = Assets.getFilmStrip(stallTexture, 128);
        setTexture(this.texture);

        this.team = team;
        score = 0;
        setWidth(2);
        setHeight(2);
        // TODO: Fix once the JSON's are fixed
        setX(getX() + 0.5f);
        setY(getY() - 0.5f);
        setName(team);
        this.worldModel = worldModel;
    }

    public int getScore() {
        return score;
    }

    /**
     * Increments the score and updates the home texture accordingly
     * @param increase Amount to increase score by
     */
    public void incrementScore(int increase) {
        score = score + increase;
        SoundController.getInstance().play(FX_DELIVER_FILE, FX_DELIVER_FILE, false, Assets.VOLUME);

        // TODO need to safely set the texture
        if (score < 4) ((FilmStrip) texture).setFrame(score);
    }

    public String getTeam() {
        return team;
    }

    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        setSensor(true);
        return true;
    }

    @Override
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        canvas.draw(
                flagFilmstrip,
                (int) (getPosition().x + 1) * worldModel.getScale().x,
                (int) getPosition().y * worldModel.getScale().y);
        flagTicks++;
        flagFilmstrip.setFrame((flagTicks / 6) % flagFilmstrip.getSize());
    }
}
