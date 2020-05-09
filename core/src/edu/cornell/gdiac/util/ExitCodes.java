package edu.cornell.gdiac.util;

public class ExitCodes {
    public static final int QUIT = 0;     // quits the application
    public static final int TITLE = 1;    // title/loading screen
    public static final int SELECT = 2;   // head back to level select
    public static final int LEVEL = 3;    // starts a level
    public static final int PAUSE = 4;    // resumes a level

    public static final int LEVEL_PASS = 98;  // when a level is successfully completed
    public static final int LEVEL_FAIL = 99;  // when a level is failed

}
