package fi.tuni.tiko2d.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import fi.tuni.tiko2d.RaccoonRoll;



public class DesktopLauncher {
    /*
	    Aspect ratios:
       -1 = FullScreen / native aspect ratio
	    0 = 16:10
	    1 = 16:9
	    2 = 18.5:9
	    3 = 19.5:9
    */
    final static int aspectRatio = 1;

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		// Sets window width
		int width = 1000;

        if (aspectRatio == -1) {
            Graphics.DisplayMode displayMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
            config.setFromDisplayMode(displayMode);
        } else {
            config.width = width;
            config.height = getHeight(aspectRatio, width);
        }

		new LwjglApplication(new RaccoonRoll(), config);
	}

    /**
     * Returns window height for given aspect ratio and window width
     *
     * @param aspectRatio chosen aspect ratio mode
     * @param width window width
     * @return window height
     */
    private static int getHeight(int aspectRatio, int width) {
        float aspectRatioFloat = 0f;
        switch (aspectRatio) {

            // 16 : 10
            case 0:
                aspectRatioFloat = 0.625f;
                break;

            // 16 : 9
            case 1:
                aspectRatioFloat = 0.5625f;
                break;

            // 18.5 : 9
            case 2:
                aspectRatioFloat = 0.486f;
                break;

            // 19.5 : 9
            case 3:
                aspectRatioFloat = 0.462f;
                break;
        }
        return (int) (width * aspectRatioFloat);
    }
}
