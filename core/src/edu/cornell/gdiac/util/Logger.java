package edu.cornell.gdiac.util;

import com.badlogic.gdx.ApplicationLogger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

public class Logger implements ApplicationLogger {
    private Writer writer;

    public Logger() {
        try {
            writer = new BufferedWriter(new FileWriter("log.txt", false));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(String tag, String message) {
        try {
            writer.write("[" + tag + "] " + message + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        try {
            writer.write("[" + tag + "] " + message + "\n");
            writer.write(Arrays.toString(exception.getStackTrace()));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void error(String tag, String message) {
        try {
            writer.write("[" + tag + "] " + message + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        try {
            writer.write("[" + tag + "] " + message + "\n");
            writer.write(Arrays.toString(exception.getStackTrace()));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void debug(String tag, String message) {
        try {
            writer.write("[" + tag + "] " + message + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        try {
            writer.write("[" + tag + "] " + message + "\n");
            writer.write(Arrays.toString(exception.getStackTrace()));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        writer.close();
    }
}
