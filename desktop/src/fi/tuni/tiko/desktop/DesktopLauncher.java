package fi.tuni.tiko.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import fi.tuni.tiko.RaccoonRoll;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 1080 / 2;
		config.width = 1920 / 2;
		new LwjglApplication(new RaccoonRoll(), config);
	}
}
