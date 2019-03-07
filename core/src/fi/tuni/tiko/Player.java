package fi.tuni.tiko;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    Body playerBody;
    float playerRotation;
    float playerRadius;
    Animation<TextureRegion> playerAnimation;
    float statetime;
    RaccoonRoll game;

    public Player(RaccoonRoll game) {
        this.game = game;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("graphics/player/roll_animation/racc_roll.txt"));
        playerAnimation = new Animation<TextureRegion>(
                1 / 30f,
                atlas.findRegions("racc_roll")
        );
        playerRotation = 90;
        playerRadius = 12 * game.getScale();
    }

    public void draw(SpriteBatch batch, float delta) {
        Vector2 playerVelocity = playerBody.getLinearVelocity();
        if (playerVelocity.x != 0 || playerVelocity.y != 0) {
            playerRotation = playerBody.getLinearVelocity().angle();
            statetime += delta * Math.max(Math.abs(playerVelocity.x), Math.abs(playerVelocity.y));
        }

        batch.draw(
                playerAnimation.getKeyFrame(statetime, true),
                playerBody.getPosition().x - playerRadius,
                playerBody.getPosition().y - playerRadius,
                playerRadius,
                playerRadius,
                playerRadius * 2,
                playerRadius * 2,
                1.0f,
                1.0f,
                playerRotation - 90);
    }

    public void createPlayerBody(World world, Vector2 startPos) {
        playerBody = world.createBody(getPlayerBodyDef(startPos.x, startPos.y));
        playerBody.createFixture(getPlayerFixtureDef());
    }

    public Vector2 getPosition() {
        return playerBody.getPosition();
    }

    public float getBodyRadius() {
        return playerRadius;
    }

    public void movePlayer(float deltatime) {
        float x = 0;
        float y = 0;

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                x = 150f * deltatime;
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                x = -150f * deltatime;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                y = 150f * deltatime;
            } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                y = -150f * deltatime;
            }
        } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
            x = Gdx.input.getAccelerometerY() * 25 * deltatime;
            y = Gdx.input.getAccelerometerX() * 25 * deltatime;
            if (x < 0.2 && x > -0.2) {
                x = 0;
            }
            if (y < 0.2 && y > -0.2) {
                y = 0;
            }
            if (y > 0) {
                y = -y;
            } else {
                y = Math.abs(y);
            }
        }
        /*
        playerBody.applyForceToCenter(
                new Vector2(x, y),
                true);
        */
        playerBody.setLinearVelocity(x, y);
    }

    private BodyDef getPlayerBodyDef(float x, float y) {
        float scale = game.getScale();
        BodyDef playerBodyDef = new BodyDef();
        playerBodyDef.type = BodyDef.BodyType.DynamicBody;
        playerBodyDef.position.set(x * scale, y * scale);
        return playerBodyDef;
    }

    private FixtureDef getPlayerFixtureDef() {
        FixtureDef playerFixtureDef = new FixtureDef();
        playerFixtureDef.density = 0.1f;
        playerFixtureDef.restitution = 0.1f;
        playerFixtureDef.friction = 0.75f;

        CircleShape playerCircle = new CircleShape();
        playerCircle.setRadius(12 * game.getScale());

        playerFixtureDef.shape = playerCircle;

        return playerFixtureDef;
    }
}
