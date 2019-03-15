package fi.tuni.tiko2d.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import fi.tuni.tiko2d.RaccoonRoll;

/*
	Aspect ratio codes:
   -1 = FullScreen / native aspect ratio
	0 = 16:10
	1 = 16:9
	2 = 18.5:9
	3 = 19.5:9
*/

public class DesktopLauncher {
    final static int aspectRatio = 1;

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		/*
		config.height = 1080 / 2;
		config.width = 1920 / 2;
		*/


        if (aspectRatio == -1) {
            Graphics.DisplayMode displayMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
            config.setFromDisplayMode(displayMode);
        } else {
            config.width = 1000;
            config.height = getHeight(aspectRatio);
        }
		new LwjglApplication(new RaccoonRoll(), config);
	}

    private static int getHeight(int aspectRatio) {
        float aspectRatioDecimal = 0f;
        switch (aspectRatio) {
            case 0:
                aspectRatioDecimal = 0.625f;
                break;
            case 1:
                aspectRatioDecimal = 0.5625f;
                break;
            case 2:
                aspectRatioDecimal = 0.486f;
                break;
            case 3:
                aspectRatioDecimal = 0.462f;
                break;
        }
        return (int) (1000 * aspectRatioDecimal);
    }
}
