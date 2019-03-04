package fi.tuni.tiko;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/*
Hours:
TileMappeja ~2h

26.02   Start:  20:20
        End:    23:50   3h 30min

27.02   Start:  15:15
        End:    16:15   1h

01.03   Start:  10:20
        End:    12:20   2h

03.03   Start:  21:30
        End:    00:00   2h 30min

Tile width: 16
Resolution: 1920
Show tiles: 30
16px -> 64px on FHD scale = 1/48f
 */
public class RaccoonRoll extends Game {
    private SpriteBatch batch;
    private final float WORLD_WIDTH = 10f;
    private float WORLD_HEIGHT;
    private OrthographicCamera worldCamera;
    private OrthographicCamera textCamera;
    //private final float scale = 1/48f;
    private final float scale = 0.5f / 48f;

	@Override
	public void create () {
		batch = new SpriteBatch();

        worldCamera = new OrthographicCamera();
        textCamera = new OrthographicCamera();

		updateWorldDimensions();
		setupCameras();

		setScreen(new Level1Screen(this));
	}

	@Override
	public void render () {
        super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}

    @Override
    public void resize(int width, int height) {
        updateWorldDimensions();
    }

    public void updateWorldDimensions() {
        float aspectRatio = 1.0f * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        WORLD_HEIGHT = WORLD_WIDTH * aspectRatio;
        /*
        Gdx.app.log("World Width", "" + WORLD_WIDTH);
        Gdx.app.log("World Height", "" + WORLD_HEIGHT);
        Gdx.app.log("Pixels Width", "" + Gdx.graphics.getWidth());
        Gdx.app.log("Pixels Height", "" + Gdx.graphics.getHeight());
        */
        setupCameras();
    }

    public OrthographicCamera getWorldCamera() {
        return worldCamera;
    }

    public OrthographicCamera getTextCamera() {
        return textCamera;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public float getWORLD_WIDTH() {
        return WORLD_WIDTH;
    }

    public float getWORLD_HEIGHT() {
        return WORLD_HEIGHT;
    }

    public float getScale() {
        return scale;
    }

    public void setupCameras() {
        worldCamera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        textCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.app.log("WorldCameraWidth", "" + worldCamera.viewportWidth);
        Gdx.app.log("WorldCameraHeight", "" + worldCamera.viewportHeight);
    }
}
