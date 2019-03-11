package fi.tuni.tiko;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    private Body playerBody;
    private float playerRotation;
    private float playerRadius;
    private Animation<TextureRegion> playerAnimation;
    private float statetime;
    private RaccoonRoll game;
    private TextureAtlas atlas;
    private float debuffTimeLeft;

    public Player(RaccoonRoll game) {
        this.game = game;
        atlas = new TextureAtlas(Gdx.files.internal("graphics/player/roll_animation/racc_roll.txt"));
        playerAnimation = new Animation<TextureRegion>(
                1 / 30f,
                atlas.findRegions("racc_roll")
        );
        playerRotation = 0;
        // 16px tileset scaling: playerRadius = 12 * game.getScale();
        playerRadius = 48 * game.getScale();
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

    public void applyDebuff() {
        if (debuffTimeLeft > 0) {
            debuffTimeLeft += 10f;
        } else {
            debuffTimeLeft = 10f;
        }
    }

    public void movePlayer(float deltatime) {
        float x = 0;
        float y = 0;

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                x = 30f * deltatime;
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                x = -30f * deltatime;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                y = 30f * deltatime;
            } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                y = -30f * deltatime;
            }
        } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
            x = MathUtils.clamp(Gdx.input.getAccelerometerY() * 15, -100f, 100f) * deltatime;
            y = MathUtils.clamp(Gdx.input.getAccelerometerX() * 15, -100f, 100f) * deltatime;
            if (game.DEBUGGING()) {
                Gdx.app.log("Accelerometer", "X: " + x / deltatime + " Y: " + y / deltatime);
            }
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

        playerBody.applyForceToCenter(
                new Vector2(x, y),
                true);

        debuffTimeLeft -= deltatime;

        if (debuffTimeLeft > 0) {
            Vector2 playerVelocity = playerBody.getLinearVelocity();
            playerVelocity.x = MathUtils.clamp(playerVelocity.x, -3f, 3f);
            playerVelocity.y = MathUtils.clamp(playerVelocity.y, -3f, 3f);
            playerBody.setLinearVelocity(playerVelocity);

            if (game.DEBUGGING()) {
                Gdx.app.log("Debuff", "Time left: " + debuffTimeLeft);
            }
        }

        //playerBody.setLinearVelocity(x, y);
        Vector2 playerVelocity = playerBody.getLinearVelocity();
        if (game.DEBUGGING()) {
            Gdx.app.log("Current velocity", "" + playerVelocity);
        }
        playerVelocity.x = -(playerVelocity.x * 2 * deltatime);
        playerVelocity.y = -(playerVelocity.y * 2 * deltatime);
        playerBody.applyForceToCenter(playerVelocity, true);
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
        playerCircle.setRadius(playerRadius * 0.9f);

        playerFixtureDef.shape = playerCircle;

        return playerFixtureDef;
    }

    public void dispose() {
        atlas.dispose();
    }

    public void setGoalReached() {
        playerBody.setType(BodyDef.BodyType.StaticBody);
    }
}
