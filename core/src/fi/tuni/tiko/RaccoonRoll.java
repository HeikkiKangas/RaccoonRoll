package fi.tuni.tiko;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;

import java.util.Locale;

/*
Tile width: 16
Resolution: 1920
Show tiles: 30
16px -> 64px on FHD scale = 1/48f
 */
public class RaccoonRoll extends Game {
    private final boolean DEBUGGING = false;
    private SpriteBatch batch;
    private final float WORLD_WIDTH = 10f;
    private float WORLD_HEIGHT;
    private OrthographicCamera worldCamera;
    private OrthographicCamera textCamera;
    private final float scale = 1f / 96f;
    // 16px tile scaling: private final float scale = 1f / 48f;
    private GlyphLayout glyphLayout;
    private BitmapFont textFont;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont hudFont;
    private Locale locale;

	@Override
	public void create () {
        //for testing purposes
        // Locale could be moved to RaccoonRoll class to save a bit of memory
        locale = new Locale("fi", "FI");
        //locale = Locale.getDefault();

		batch = new SpriteBatch();

        worldCamera = new OrthographicCamera();
        textCamera = new OrthographicCamera();
        glyphLayout = new GlyphLayout();

		updateWorldDimensions();
		setupCameras();
        generateFonts();

		setScreen(new MenuScreen(this));
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
        // Gdx.app.log("RaccoonRoll", "Resize happened");
    }

    public void updateWorldDimensions() {
        float aspectRatio = 1.0f * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        WORLD_HEIGHT = WORLD_WIDTH * aspectRatio;
        setupCameras();
    }

    public Vector2 getTextDimensions(BitmapFont font, String text) {
        glyphLayout.setText(font, text);
        return new Vector2(glyphLayout.width, glyphLayout.height);
    }

    private void generateFonts() {
        Color fontColor = Color.BLACK;
        String fontFilename = "fonts/boorsok.ttf";

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilename));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = scaleTextFromFHD(55);
        fontParameter.color = fontColor;
        textFont = fontGenerator.generateFont(fontParameter);
        fontParameter.size = scaleTextFromFHD(85);
        titleFont = fontGenerator.generateFont(fontParameter);
        fontParameter.size = scaleTextFromFHD(55);
        buttonFont = fontGenerator.generateFont(fontParameter);
        fontParameter.size = scaleTextFromFHD(70);
        hudFont = fontGenerator.generateFont(fontParameter);
        fontGenerator.dispose();
    }

    public BitmapFont getHudFont() {
        return hudFont;
    }

    public BitmapFont getTextFont() {
        return textFont;
    }

    public BitmapFont getTitleFont() {
        return titleFont;
    }

    public BitmapFont getButtonFont() {
	    return buttonFont;
    }

    public float scaleFromFHD(float num) {
        float aspectRatio = Gdx.graphics.getHeight() / 1080f;
        return aspectRatio * num;
    }

    public int scaleTextFromFHD(int num) {
        float aspectRatio = Gdx.graphics.getHeight() / 1080f;
        return (int) (num * aspectRatio);
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
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean DEBUGGING() {
        return DEBUGGING;
    }
}
