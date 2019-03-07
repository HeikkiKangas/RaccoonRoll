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

/*
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
    private final float scale = 1f / 48f;
    private GlyphLayout glyphLayout;
    private BitmapFont buttonFont;
    private BitmapFont titleFont;
    private BitmapFont textFont;

	@Override
	public void create () {
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

    public Vector2 getTextSize(BitmapFont font, String text) {
        glyphLayout.setText(font, text);
        return new Vector2(glyphLayout.width, glyphLayout.height);
    }

    private void generateFonts() {
        String fontFilename = "fonts/joystix monospace.ttf";

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilename));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.color = Color.BLACK;

        // fontin koko muutettavissa vaihtamalla scaleFontille annettavaa parametriä
        fontParameter.size = scaleFont(20);
        textFont = fontGenerator.generateFont(fontParameter);

        fontParameter.size = scaleFont(100);
        buttonFont = fontGenerator.generateFont(fontParameter);

        fontParameter.size = scaleFont(180);
        titleFont = fontGenerator.generateFont(fontParameter);

        fontGenerator.dispose();
    }

    private int scaleFont(int fullHDfontSize) {
        float textScale = Gdx.graphics.getHeight() / 1080f;
        return (int) (fullHDfontSize * textScale);
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

    public BitmapFont getButtonFont() {
        return buttonFont;
    }

    public BitmapFont getTitleFont() {
        return titleFont;
    }

    public BitmapFont getTextFont() {
        return textFont;
    }
}
