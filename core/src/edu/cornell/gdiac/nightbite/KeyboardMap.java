package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Input.Keys;

/** Honestly please get rid of this class when we have json/toml whatever support
 *  like actually this is kind of disgusting **/
public class KeyboardMap {
    public static class Player {
        public final int UP;
        public final int DOWN;
        public final int LEFT;
        public final int RIGHT;
        public final int DASH;
        public final int GRAB;
        public final int DEBUG;
        public final int RESET;

        public Player(int up, int down, int left, int right, int dash, int grab, int debug, int reset) {
            UP = up;
            DOWN = down;
            LEFT = left;
            RIGHT = right;
            DASH = dash;
            GRAB = grab;
            DEBUG = debug;
            RESET = reset;
        }
    }

    private static Player PLAYER0 = new Player(
            Keys.UP,
            Keys.DOWN,
            Keys.LEFT,
            Keys.RIGHT,
            Keys.SLASH,
            Keys.PERIOD,
            Keys.Y,
            Keys.R);

    private static Player PLAYER1 = new Player(
            Keys.W,
            Keys.S,
            Keys.A,
            Keys.D,
            Keys.SHIFT_LEFT,
            Keys.E,
            Keys.UNKNOWN,
            Keys.UNKNOWN);

    public static Player[] players = {PLAYER0, PLAYER1};
}
