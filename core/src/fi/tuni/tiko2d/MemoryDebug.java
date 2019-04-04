package fi.tuni.tiko2d;

import com.badlogic.gdx.Gdx;

public class MemoryDebug {
    private static float timeDelta;

    public static void maxMemory() {
        Gdx.app.log("MaxMemory", "" + Runtime.getRuntime().maxMemory() / 1000000f);
    }

    public static void memoryUsed(float delta) {
        Runtime run = Runtime.getRuntime();
        int mb = 1024 * 1024;
        timeDelta += delta;
        if (timeDelta >= 3) {
            timeDelta = 0;
            System.gc();
            //Gdx.app.log("JavaHeap", "" + Gdx.app.getJavaHeap() / 1048576f);
            //Gdx.app.log("NativeHeap", "" + Gdx.app.getNativeHeap() / 1048576f);
            Gdx.app.log("Memory", "Start");
            Gdx.app.log("JavaHeap", "" + Gdx.app.getJavaHeap() / mb);
            Gdx.app.log("NativeHeap", "" + Gdx.app.getNativeHeap() / mb);
            Gdx.app.log("Total Memory", "" + run.totalMemory() / mb);
            Gdx.app.log("Free Memory", "" + run.freeMemory() / mb);
            Gdx.app.log("Used Memory", "" + (run.totalMemory() - run.freeMemory()) / mb);
            Gdx.app.log("Max Memory", "" + run.maxMemory() / mb);
            Gdx.app.log("Memory", "End");
        }
    }
}
