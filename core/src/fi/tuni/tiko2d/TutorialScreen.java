package fi.tuni.tiko2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.ArrayList;

public class TutorialScreen implements Screen {
    private RaccoonRoll game;
    private SpriteBatch batch;
    private Player player;
    private OrthographicCamera worldCamera;
    private OrthographicCamera textCamera;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private ArrayList<Sound> wallHitSounds;
    private Music backgroundMusic;

    private I18NBundle tutorialBundle;
    private Options options;
    private BitmapFont textFont;

    float WORLD_WIDTH;
    float WORLD_HEIGHT;

    public TutorialScreen(RaccoonRoll game) {
        this.game = game;
        options = game.getOptions();
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        textFont = game.getTextFont();
// testi-muutos, katsotaan huomaako jenkins uuden pushin
        WORLD_HEIGHT = game.getWORLD_HEIGHT();
        WORLD_WIDTH = game.getWORLD_WIDTH();

        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        player = new Player(game);
        player.createPlayerBody(
                world,
                new Vector2(
                        WORLD_WIDTH / 2f,
                        WORLD_HEIGHT / 2f)
        );

        tutorialBundle = I18NBundle.createBundle(Gdx.files.internal("localization/TutorialBundle"), options.getLocale());
        loadSounds();
        loadBackgroundMusic("tutorial");

        createWalls();
    }

    /**
     * Loads levels background music, sets looping and volume and starts playing the music.
     *
     * @param levelName name of the level which music we want to load
     */
    private void loadBackgroundMusic(String levelName) {
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/backgroundMusic/" + levelName + ".mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(options.getMusicVolume());
        backgroundMusic.play();
    }

    /**
     * Loads sound effects
     */
    private void loadSounds() {
        wallHitSounds = new ArrayList<Sound>();
        for (int i = 1; i < 6; i++) {
            String filePath = String.format("sounds/wallHit/WALL_HIT_0%d.mp3", i);
            wallHitSounds.add(Gdx.audio.newSound(Gdx.files.internal(filePath)));
        }
    }

    /**
     * Add contact listener for playing sounds when player hits a wall
     */
    private void addContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                wallHitSounds.get(MathUtils.random(wallHitSounds.size() - 1)).play(options.getEffectsVolume());
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    private void createWalls() {
        for (int i = 0; i < 4; i++) {
            world.createBody(getGroundBodyDef(i)).createFixture(getGroundShape(i), 0.0f);
        }
    }

    private BodyDef getGroundBodyDef(int side) {

        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyDef.BodyType.StaticBody;
        switch (side) {
            case 0:
                groundBodyDef.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT - 0.1f);
                break;
            case 1:
                groundBodyDef.position.set(WORLD_WIDTH - 0.1f, WORLD_HEIGHT / 2);
                break;
            case 2:
                groundBodyDef.position.set(WORLD_WIDTH / 2, 0.1f);
                break;
            case 3:
                groundBodyDef.position.set(0.1f, WORLD_HEIGHT / 2);
                break;
        }

        return groundBodyDef;
    }

    private PolygonShape getGroundShape(int side) {
        PolygonShape groundShape = new PolygonShape();
        switch (side) {
            case 0:
                groundShape.setAsBox(WORLD_WIDTH / 2, 0.1f);
                break;
            case 1:
                groundShape.setAsBox(0.1f, WORLD_HEIGHT / 2);
                break;
            case 2:
                groundShape.setAsBox(WORLD_WIDTH / 2, 0.1f);
                break;
            case 3:
                groundShape.setAsBox(0.1f, WORLD_HEIGHT / 2);
                break;
        }

        return groundShape;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        clearScreen();
        player.movePlayer(delta);
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        player.draw(batch, delta);
        batch.setProjectionMatrix(textCamera.combined);
        //drawTexts();
        batch.end();
        if (true) {
            debugRenderer.render(world, worldCamera.combined);
        }
        world.step(1f / 60f, 6, 2);
    }

    /**
     * Clears the screen with black color
     */
    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "TutorialScreen");
        }
    }
}
