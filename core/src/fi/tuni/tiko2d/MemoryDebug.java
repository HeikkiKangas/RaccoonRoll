package fi.tuni.tiko2d;

import com.badlogic.gdx.Gdx;

/**
 * Debugger class for observing memory usage to identify memory leaks
 */
public class MemoryDebug {
    private static float timeDelta;

    /**
     * Empty private constructor to hide the public one
     */
    private MemoryDebug() {
    }

    /**
     * Logs the system max memory
     */
    public static void maxMemory() {
        Gdx.app.log("MaxMemory", "" + Runtime.getRuntime().maxMemory() / 1000000f);
    }

    /**
     * Logs memory usage
     *
     * @param delta time since last frame
     */
    public static void memoryUsed(float delta) {
        Runtime run = Runtime.getRuntime();
        int mb = 1024 * 1024;
        timeDelta += delta;
        if (timeDelta >= 3) {
            timeDelta = 0;
            //System.gc();
            Gdx.app.log("*****", "*****");
            Gdx.app.log("JavaHeap", "" + Gdx.app.getJavaHeap() / mb);
            Gdx.app.log("NativeHeap", "" + Gdx.app.getNativeHeap() / mb);
            Gdx.app.log("Total Memory", "" + run.totalMemory() / mb);
            Gdx.app.log("Free Memory", "" + run.freeMemory() / mb);
            Gdx.app.log("Used Memory", "" + (run.totalMemory() - run.freeMemory()) / mb);
            Gdx.app.log("Max Memory", "" + run.maxMemory() / mb);
            Gdx.app.log("*****", "*****");
        }
    }
}
