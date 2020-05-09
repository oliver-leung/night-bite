package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import edu.cornell.gdiac.util.XBox360Controller;

public class InputController extends MechanicController {
    // TODO: Configurable controls
    // TODO: throwing not finalized
    // TODO: exit not implemented
    private static final float DEADZONE = 0.3f;

    private XBox360Controller xbox;
    private int keyboard;

    private boolean prevDash;
    private boolean prevThrow;

    private boolean prevDebug;
    private boolean prevReset;

    public InputController(int xbox, int keyboard, boolean debug) {
        sudo = debug;
        this.xbox = new XBox360Controller(xbox);
        this.keyboard = keyboard;
    }

    public InputController(int xbox, int keyboard) {
        this(xbox, keyboard,false);
    }

    private boolean notDeadZoned(float vert, float hori) {
        return Math.abs(vert) > DEADZONE || Math.abs(hori) > DEADZONE;
    }

    public void pollController() {
        if (! xbox.isConnected()) {
            velX = 0;
            velY = 0;
            isDashing = false;
            isThrowing = false;
            isDebug = false;
            isReset = false;
            return;
        }

        float hori = xbox.getLeftX();
        float vert = xbox.getLeftY();

        velX = notDeadZoned(vert, hori) ? hori : 0f;
        velY = notDeadZoned(vert, hori) ? vert : 0f;

        boolean temp = xbox.getB();
        isDashing = !prevDash && temp;
        temp = xbox.getA();
        isThrowing = !prevThrow && temp;

        if (!sudo) { return; }

        temp = xbox.getY();
        isDebug = !prevDebug && temp;
        temp = xbox.getStart();
        isReset = !prevReset && temp;
    }

    public void pollKeyboard() {
        if (keyboard < 0 || keyboard >= KeyboardMap.players.length) {
            return;
        }

        KeyboardMap.Player keybinds = KeyboardMap.players[keyboard];

        boolean temp1 = isKeyPressed(keybinds.LEFT);
        boolean temp2 = isKeyPressed(keybinds.RIGHT);

        if (!temp1 || !temp2) {
            velX = temp1 ? -1.0f : velX;
            velX = temp2 ? 1.0f : velX;
        }

        temp1 = isKeyPressed(keybinds.UP);
        temp2 = isKeyPressed(keybinds.DOWN);
        if (!temp1 || !temp2) {
            velY = temp1 ? 1.0f : velY;
            velY = temp2 ? -1.0f : velY;
        }

        temp1 = isKeyPressed(keybinds.DASH);
        isDashing = isDashing || (!prevDash && temp1);

        temp1 = isKeyPressed(keybinds.GRAB);
        isThrowing = isThrowing || (!prevThrow && temp1);

        if (!sudo) {
            return;
        }

        temp1 = Gdx.input.isKeyJustPressed(keybinds.DEBUG);
        isDebug = isDebug || (!prevDebug && temp1);

        temp1 = isKeyPressed(keybinds.RESET);
        isReset = isReset || (!prevReset && temp1);

        // Music
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            Music music = Assets.getMusic();
            if (music.getVolume() > 0) {
                Assets.EFFECT_VOLUME = 0;
                music.setVolume(Assets.EFFECT_VOLUME);
            } else {
                Assets.EFFECT_VOLUME = 0.1f;
                music.setVolume(Assets.EFFECT_VOLUME);
            }
        }
    }

    private boolean isKeyPressed(int key) {
        return Gdx.input.isKeyPressed(key);
    }

    private void resetPrev() {
        prevDash = isDashing;
        prevThrow = isThrowing;
        prevDebug = isDebug;
        prevReset = isReset;
    }

    public void poll() {
        pollController();
        pollKeyboard();
    }

}
