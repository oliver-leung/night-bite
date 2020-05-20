package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Input.Keys;

/** Honestly please get rid of this class when we have json/toml whatever support
 *  like actually this is kind of disgusting **/
public class KeyboardMap {
    public static boolean mouse = true;

    public static class Player {
        public final int UP;
        public final int DOWN;
        public final int LEFT;
        public final int RIGHT;
        public final int DASH;
        public final int GRAB;
        public final int DEBUG;
        public final int RESET;
        public final int PAUSE;
        public final int WHACK;

        public Player(int up, int down, int left, int right, int dash, int grab,
                      int debug, int reset, int pause, int whack) {
            UP = up;
            DOWN = down;
            LEFT = left;
            RIGHT = right;
            DASH = dash;
            GRAB = grab;
            DEBUG = debug;
            RESET = reset;
            PAUSE = pause;
            WHACK = whack;
        }
    }

    private static Player PLAYER0 = new Player(
            Keys.W,
            Keys.S,
            Keys.A,
            Keys.D,
            Keys.UNKNOWN,
            Keys.SPACE,
            Keys.BACKSPACE,
            Keys.R,
            Keys.ESCAPE,
            Keys.ENTER);

    public static Player[] players = {PLAYER0};
}
