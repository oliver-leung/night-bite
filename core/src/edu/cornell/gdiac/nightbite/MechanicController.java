package edu.cornell.gdiac.nightbite;

import edu.cornell.gdiac.util.XBox360Controller;

public abstract class MechanicController {
    protected float velX;
    protected float velY;

    protected boolean isDashing;
    protected boolean isThrowing;

    protected boolean sudo;

    protected boolean isDebug;
    protected boolean isReset;
    protected boolean isExit;

    public float getVelX() {
        return velX;
    }

    public float getVelY() {
        return velY;
    }

    public boolean isThrowing() {
        return isThrowing;
    }

    public boolean isDashing() {
        return isDashing;
    }

    public boolean isDebug() {
        return isDebug && sudo;
    }

    public boolean isReset() {
        return isReset && sudo;
    }

    public boolean isExit() {
        return isExit && sudo;
    }

    public abstract void poll();
}
