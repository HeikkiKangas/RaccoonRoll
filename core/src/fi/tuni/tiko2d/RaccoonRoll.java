package fi.tuni.tiko2d;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

/**
 * Main game class.
 * Creates variables used in several classes and starts main menu
 *
 * @author Heikki Kangas
 */
public class RaccoonRoll extends Game {
    private final boolean DEBUGGING = false;

    private SpriteBatch batch;
    private final float WORLD_WIDTH = 10f;
    private float WORLD_HEIGHT;
    private OrthographicCamera worldCamera;
    private OrthographicCamera textCamera;
    private final float scale = 1f / 128f;
    //private final float scale = 1f / 96f;
    // 16px tile scaling: private final float scale = 1f / 48f;
    private GlyphLayout glyphLayout;

    private boolean scaleHorizontal;

    private Options options;

    /**
     * Creates variables used in most of the classes.
     * Updates world height according to screen's aspect ratio
     */
    @Override
    public void create () {
        options = new Options();

        if ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth() <= 0.5625f) {
            scaleHorizontal = true;
        }

        if (DEBUGGING) {
            Gdx.app.log("Window width", "" + Gdx.graphics.getWidth());
            Gdx.app.log("Window height", "" + Gdx.graphics.getHeight());
            Gdx.app.log("Horizontal scaling", "" + scaleHorizontal);
            Gdx.app.log("Aspect ratio", "" + ((float) Gdx.graphics.getHeight() / Gdx.graphics.getWidth()));
        }

        batch = new SpriteBatch();

        worldCamera = new OrthographicCamera();
        textCamera = new OrthographicCamera();
        glyphLayout = new GlyphLayout();

        updateWorldDimensions();
        setupCameras();
        //generateFonts();

        MemoryDebug.maxMemory();
        Gdx.app.log("MaxTextureSize", "" + GL20.GL_MAX_TEXTURE_SIZE);
        //3379
        Gdx.app.log("MaxTextureUnits", "" + GL20.GL_MAX_TEXTURE_IMAGE_UNITS);
        //34930
        setScreen(new MenuScreen(this));
    }

    /**
     * Renders the active screen.
     */
    @Override
    public void render () {
        super.render();
    }

    /**
     * Disposes used assets
     */
    @Override
    public void dispose () {
        if (DEBUGGING) {
            Gdx.app.log("Disposed", "RaccoonRoll");
        }
        batch.dispose();
        /*
        buttonFont.dispose();
        hudFont.dispose();
        textFont.dispose();
        titleFont.dispose();
        */
    }

    /**
     * Called when window resizes, calls {@link #updateWorldDimensions()}
     *
     * @param width  not used
     * @param height not used
     */
    @Override
    public void resize(int width, int height) {
        updateWorldDimensions();
        // Gdx.app.log("RaccoonRoll", "Resize happened");
    }

    /**
     * Updates world height according to window's aspect ratio
     */
    public void updateWorldDimensions() {
        float aspectRatio = 1.0f * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        WORLD_HEIGHT = WORLD_WIDTH * aspectRatio;
        setupCameras();
    }

    /**
     * Return's given text's dimensions when drawn with the given font
     * @param font the font to use
     * @param text the text which dimensions will be returned
     * @return Vector with text width as x and text height as y
     */
    public Vector2 getTextDimensions(BitmapFont font, String text) {
        glyphLayout.setText(font, text);
        return new Vector2(glyphLayout.width, glyphLayout.height);
    }

    /**
     * Generates the fonts used in the game
     */
    /*
    private void generateFonts() {
        String fontFilename = "fonts/boorsok.ttf";

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilename));

        FreeTypeFontGenerator.FreeTypeFontParameter tutorialFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        tutorialFontParameter.size = scaleTextFromFHD(75);
        tutorialFontParameter.borderWidth = 2f;
        tutorialFontParameter.borderColor = Color.BLACK;
        tutorialFontParameter.color = Color.WHITE;
        tutorialFont = fontGenerator.generateFont(tutorialFontParameter);

        tutorialFontParameter.size = scaleTextFromFHD(60);
        tutorialSmallFont = fontGenerator.generateFont(tutorialFontParameter);

        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        fontParameter.color = Color.BLACK;
        fontParameter.size = scaleTextFromFHD(55);
        textFont = fontGenerator.generateFont(fontParameter);

        fontParameter.size = scaleTextFromFHD(85);
        titleFont = fontGenerator.generateFont(fontParameter);

        fontParameter.size = scaleTextFromFHD(55);
        buttonFont = fontGenerator.generateFont(fontParameter);

        fontParameter.size = scaleTextFromFHD(70);
        hudFont = fontGenerator.generateFont(fontParameter);

        fontGenerator.dispose();
    }
    */

    /**
     * Getter for font used in TutorialScreen instructions
     *
     * @return font for TutorialScreen instructions
     */
    public BitmapFont getTutorialSmallFont() {
        String fontFilename = "fonts/boorsok.ttf";
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilename));

        FreeTypeFontGenerator.FreeTypeFontParameter tutorialFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        tutorialFontParameter.borderWidth = 2f;
        tutorialFontParameter.borderColor = Color.BLACK;
        tutorialFontParameter.color = Color.WHITE;
        tutorialFontParameter.size = scaleTextFromFHD(60);

        BitmapFont tutorialSmallFont = fontGenerator.generateFont(tutorialFontParameter);

        fontGenerator.dispose();

        return tutorialSmallFont;
    }

    /**
     * Getter for font used in TutorialScreen instructions
     *
     * @return font for TutorialScreen instructions
     */
    public BitmapFont getTutorialFont() {
        String fontFilename = "fonts/boorsok.ttf";
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilename));

        FreeTypeFontGenerator.FreeTypeFontParameter tutorialFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        tutorialFontParameter.size = scaleTextFromFHD(75);
        tutorialFontParameter.borderWidth = 2f;
        tutorialFontParameter.borderColor = Color.BLACK;
        tutorialFontParameter.color = Color.WHITE;
        BitmapFont tutorialFont = fontGenerator.generateFont(tutorialFontParameter);

        fontGenerator.dispose();

        return tutorialFont;
    }

    /**
     * Getter for font used in MazeScreen HUD
     * @return font for MazeScreen HUD
     */
    public BitmapFont getHudFont() {
        String fontFilename = "fonts/boorsok.ttf";
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilename));

        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        //fontParameter.color = Color.BLACK;
        fontParameter.borderWidth = 2f;
        fontParameter.borderColor = Color.BLACK;
        fontParameter.color = Color.WHITE;
        fontParameter.size = scaleTextFromFHD(70);
        BitmapFont hudFont = fontGenerator.generateFont(fontParameter);

        fontGenerator.dispose();

        return hudFont;
    }

    /**
     * Getter for font used in AboutScreen texts
     * @return font for AboutScreen texts
     */
    public BitmapFont getTextFont() {
        String fontFilename = "fonts/boorsok.ttf";
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilename));

        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.color = Color.BLACK;
        fontParameter.size = scaleTextFromFHD(55);
        BitmapFont textFont = fontGenerator.generateFont(fontParameter);

        fontGenerator.dispose();

        return textFont;
    }

    /**
     * Getter for font used in AboutScreen title text
     * @return font for AboutScreen title
     */
    public BitmapFont getTitleFont() {
        String fontFilename = "fonts/boorsok.ttf";
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilename));

        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.color = Color.BLACK;
        fontParameter.size = scaleTextFromFHD(85);
        BitmapFont titleFont = fontGenerator.generateFont(fontParameter);

        fontGenerator.dispose();

        return titleFont;
    }

    /**
     * Getter for font used in menu buttons
     * @return font for menu buttons
     */
    public BitmapFont getButtonFont() {
        String fontFilename = "fonts/boorsok.ttf";
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilename));

        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.color = Color.BLACK;
        fontParameter.size = scaleTextFromFHD(55);
        BitmapFont buttonFont = fontGenerator.generateFont(fontParameter);

        fontGenerator.dispose();

        return buttonFont;
    }

    /**
     * Scales numbers to smaller screens
     * @param num float to scale
     * @return scaled float
     */
    public float scaleFromFHD(float num) {
        float aspectRatio;
        if (scaleHorizontal) {
            aspectRatio = Gdx.graphics.getWidth() / 1920f;
        } else {
            aspectRatio = Gdx.graphics.getHeight() / 1080f;
        }
        return aspectRatio * num;
    }

    /**
     * Scales numbers to smaller screens
     *
     * @param num float to scale
     * @return scaled float
     */
    public float scaleHorizontal(float num) {
        float aspectRatio = Gdx.graphics.getWidth() / 1920f;
        return aspectRatio * num;
    }

    /**
     * Scales numbers to smaller screens
     *
     * @param num float to scale
     * @return scaled float
     */
    public float scaleVertical(float num) {
        float aspectRatio = Gdx.graphics.getHeight() / 1080f;
        return aspectRatio * num;
    }

    /**
     * Scales numbers for smaller screens
     * @param num int to scale
     * @return scaled int
     */
    public int scaleTextFromFHD(int num) {
        float aspectRatio;
        if (scaleHorizontal) {
            aspectRatio = Gdx.graphics.getWidth() / 1920f;
        } else {
            aspectRatio = Gdx.graphics.getHeight() / 1080f;
        }
        return (int) (num * aspectRatio);
    }

    /**
     * Getter for camera using world dimensions
     * @return the camera
     */
    public OrthographicCamera getWorldCamera() {
        return worldCamera;
    }

    /**
     * Getter for camera using pixels
     * @return the camera
     */
    public OrthographicCamera getTextCamera() {
        return textCamera;
    }

    /**
     * Getter for SpriteBatch used in all classes
     * @return the SpriteBatch
     */
    public SpriteBatch getBatch() {
        return batch;
    }

    /**
     * Getter for world width in meters
     * @return world width in meters
     */
    public float getWORLD_WIDTH() {
        return WORLD_WIDTH;
    }

    /**
     * Getter for world height in meters
     * @return world height in meters
     */
    public float getWORLD_HEIGHT() {
        return WORLD_HEIGHT;
    }

    /**
     * Getter for scaling of game objects to meters
     * @return scaling of game objects to meters
     */
    public float getScale() {
        return scale;
    }

    /**
     * Sets up cameras with world dimensions and pixels
     */
    public void setupCameras() {
        worldCamera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        textCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public Options getOptions() {
        return options;
    }

    /**
     * Should debugging messages and debug renderers be used
     * @return true if debugging is enabled
     */
    public boolean DEBUGGING() {
        return DEBUGGING;
    }
}
