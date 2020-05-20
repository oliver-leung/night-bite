package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
    private boolean prevPaused;
    private boolean prevReset;
    private boolean prevWhack;

    public InputController(int xbox, int keyboard, boolean debug) {
        sudo = debug;
        this.xbox = new XBox360Controller(xbox);
        this.keyboard = keyboard;
    }

//    public InputController(int xbox, int keyboard) {
//        this(xbox, keyboard,false);
//    }

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
            isPaused = false;
            isEnter = false;
            isWhack = false;
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

        isDashing = Gdx.input.isKeyJustPressed(keybinds.DASH);
        isThrowing = Gdx.input.isKeyJustPressed(keybinds.GRAB);
        isDebug = Gdx.input.isKeyJustPressed(keybinds.DEBUG);
        isPaused = Gdx.input.isKeyJustPressed(keybinds.PAUSE);
        isReset = Gdx.input.isKeyJustPressed(keybinds.RESET);
        isEnter = Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        isWhack = Gdx.input.isKeyJustPressed(keybinds.WHACK);

        // Music
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            Assets.changeMute();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) KeyboardMap.mouse = !KeyboardMap.mouse;

        // TODO we need so set some stuff around here re: prevPaused
    }

    private boolean isKeyPressed(int key) {
        return Gdx.input.isKeyPressed(key);
    }

    public void poll() {
        pollController();
        pollKeyboard();
    }

}
