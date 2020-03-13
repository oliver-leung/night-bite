package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;

public class MechanicManager {
    // TODO: exit doesn't work

    private static final int SUPPORTED_CONTROLLERS = 2;

    public static MechanicManager instance;

    public static PooledList<Vector2> objectList; // TODO feels whack

    public static MechanicManager getInstance(PooledList<Vector2> objects) {
        objectList = objects;
        if (instance == null) {
            instance = new MechanicManager();
        }
        return instance;
    }

    public MechanicManager() {
        // TODO: stop hardcoding stuff
        controllers = new MechanicController[SUPPORTED_CONTROLLERS];
        connectController(0, 0, true);
        connectController(1, 1, false);
//        controllers[connected] = new AIController();
//        connected++;
    }

    private int connected = 0;

    private MechanicController[] controllers;

    public void connectController(int xbox, int keyboard, boolean debug) {
        controllers[connected] = new InputController(xbox, keyboard, debug);
        connected ++;
    }

    public void fillAI() {

    }


    public void update() {
        for (int i = 0; i < controllers.length; i ++) {
            if (controllers[i] instanceof AIController) {
                ((AIController) controllers[i]).updateAI(objectList);
            }
            controllers[i].poll();
        }
    }

    public float getVelX(int controller) {
        return controllers[controller].getVelX();
    }

    public float getVelY(int controller) {
        return controllers[controller].getVelY();
    }

    public boolean isThrowing(int controller) {
        return controllers[controller].isThrowing();
    }

    public boolean isDashing(int controller) {
        return controllers[controller].isDashing();
    }

    public boolean isDebug(int controller) {
        return controllers[controller].isDebug();
    }

    public boolean isReset(int controller) {
        return controllers[controller].isReset();
    }

    public boolean didDebug() {
        for (int i = 0; i < controllers.length; i ++) {
            if (controllers[i].isDebug()) { return true; }
        }
        return false;
    }

    public boolean didReset() {
        for (int i = 0; i < controllers.length; i ++) {
            if (controllers[i].isReset()) { return true; }
        }
        return false;
    }

    public boolean didExit() {
        for (MechanicController c : controllers) {
            if (c.isExit()) { return true; }
        }
        return false;
    }
}
