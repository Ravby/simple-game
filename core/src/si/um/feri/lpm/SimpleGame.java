package si.um.feri.lpm;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class SimpleGame extends ApplicationAdapter {

    private SpriteBatch batch;

    // textures
    private Texture playerTexture;
    private Texture collectibleTexture;
    private Texture obstacleTexture;
    private Texture background;

    // sounds
    private Sound collectSound;
    private Sound hitSound;

    private BitmapFont font;

    private Rectangle player;
    private Array<Rectangle> collectibles;
    private Array<Rectangle> obstacles;

    private float collectibleSpawnTime;    // in seconds
    private int collectiblesCollected;

    private float obstacleSpawnTime;    // in seconds
    private int health;

    private static final float PLAYER_SPEED = 250f;
    private static final float COLLECTIBLE_SPEED = 100f;
    private static final float COLLECTIBLE_SPAWN_TIME = 4f;    // in seconds
    private static final float OBSTACLE_SPEED = 150f;
    private static final int OBSTACLE_DAMAGE = 25;
    private static final float OBSTACLE_SPAWN_TIME = 2f;    // in seconds

    @Override
    public void create() {
        batch = new SpriteBatch();

        playerTexture = new Texture("images/piggy-bank/piggy-bank.png");
        collectibleTexture = new Texture("images/piggy-bank/coin.png");
        obstacleTexture = new Texture("images/piggy-bank/hammer.png");
        background = new Texture("images/piggy-bank/background.jpg");

        collectSound = Gdx.audio.newSound(Gdx.files.internal("sounds/piggy-bank/coin-collect.wav"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("sounds/piggy-bank/wreee.wav"));

        font = new BitmapFont();

        player = new Rectangle();
        player.x = Gdx.graphics.getWidth() / 2f - playerTexture.getWidth() / 2f;
        player.y = 0f;
        player.width = playerTexture.getWidth();
        player.height = playerTexture.getHeight();

        collectibles = new Array<>();
        collectiblesCollected = 0;
        spawnCollectible();

        obstacles = new Array<>();
        health = 100;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        if (health > 0) {
            handleInput(Gdx.graphics.getDeltaTime());
            update(Gdx.graphics.getDeltaTime());
        }
        
        batch.begin();

        draw();

        batch.end();
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveLeft(delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveRight(delta);
        }
    }

    private void moveLeft(float delta) {
        player.x -= PLAYER_SPEED * delta;
    }

    private void moveRight(float delta) {
        player.x += PLAYER_SPEED * delta;
    }

    private void moveUp(float delta) {
        player.y += PLAYER_SPEED * delta;
    }

    private void moveDown(float delta) {
        player.y -= PLAYER_SPEED * delta;
    }

    private void update(float delta) {
        float elapsedTime = (TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f);
        if (elapsedTime - collectibleSpawnTime > COLLECTIBLE_SPAWN_TIME) spawnCollectible();
        if (elapsedTime - obstacleSpawnTime > OBSTACLE_SPAWN_TIME) spawnObstacle();

        for (Iterator<Rectangle> it = collectibles.iterator(); it.hasNext(); ) {
            Rectangle collectible = it.next();
            collectible.y -= COLLECTIBLE_SPEED * delta;
            if (collectible.y + collectibleTexture.getHeight() < 0) {
                it.remove();
            }
            if (collectible.overlaps(player)) {
                collectiblesCollected++;
                collectSound.play();
                it.remove();
            }
        }

        for (Iterator<Rectangle> it = obstacles.iterator(); it.hasNext(); ) {
            Rectangle obstacle = it.next();
            obstacle.y -= OBSTACLE_SPEED * delta;
            if (obstacle.y + obstacleTexture.getHeight() < 0) {
                it.remove();
            }
            if (obstacle.overlaps(player)) {
                health -= OBSTACLE_DAMAGE;
                hitSound.play();
                it.remove();
            }
        }
    }

    private void draw() {
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (health <= 0) {
            font.setColor(Color.RED);
            font.draw(batch,
                    "GAME OVER",
                    20f, Gdx.graphics.getHeight() - 20f
            );
            return;
        }
        for (Rectangle coin : collectibles) {
            batch.draw(collectibleTexture, coin.x, coin.y);
        }
        for (Rectangle hammer : obstacles) {
            batch.draw(obstacleTexture, hammer.x, hammer.y);
        }
        batch.draw(playerTexture, player.x, player.y);

        font.setColor(Color.RED);
        font.draw(batch,
                "HEALTH: " + health,
                20f, Gdx.graphics.getHeight() - 20f
        );

        font.setColor(Color.YELLOW);
        font.draw(batch,
                "SCORE: " + collectiblesCollected,
                20f, Gdx.graphics.getHeight() - 60f
        );
    }

    private void spawnCollectible() {
        Rectangle collectible = new Rectangle();
        collectible.x = MathUtils.random(0f, Gdx.graphics.getWidth() - collectibleTexture.getWidth());
        collectible.y = Gdx.graphics.getHeight();
        collectible.width = collectibleTexture.getWidth();
        collectible.height = collectibleTexture.getHeight();
        collectibles.add(collectible);
        collectibleSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;  // 1 second = 1000 milliseconds
    }

    private void spawnObstacle() {
        Rectangle obstacle = new Rectangle();
        obstacle.x = MathUtils.random(0f, Gdx.graphics.getWidth() - obstacleTexture.getWidth());
        obstacle.y = Gdx.graphics.getHeight();
        obstacle.width = collectibleTexture.getWidth();
        obstacle.height = collectibleTexture.getHeight();
        obstacles.add(obstacle);
        obstacleSpawnTime = TimeUtils.nanosToMillis(TimeUtils.nanoTime()) / 1000f;  // 1 second = 1000 milliseconds
    }


    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        collectibleTexture.dispose();
        obstacleTexture.dispose();
        background.dispose();
        collectSound.dispose();
        hitSound.dispose();
    }
}
