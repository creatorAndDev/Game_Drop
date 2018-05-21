package info.android.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import com.badlogic.gdx.math.Rectangle;
import java.util.Iterator;

public class GameScreen implements Screen {
	//fields items
	final Drop game;

	OrthographicCamera camera;

	SpriteBatch batch;

	Texture dropImage;
	Texture bucketImage;

	Sound dropSound;
	Music rainMusic;

	Rectangle bucket;

	Vector3 touchPos;

	//коллекции капель
	Array<Rectangle> raindrops;

	//время последнее появления капли
	long lastDropTime;

	//счетчик пойманых капель
	int dropsGatchered;

	public GameScreen (final Drop gam) {
		this.game = gam;
		//создаем камеру для визуализации пространства
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		//вызываем наши текстуры которые мы загрузили
		batch = new SpriteBatch();

		//трехмерный вектор
		touchPos = new Vector3();

		dropImage = new Texture("droplet.png");
		bucketImage = new Texture("bucket.png");

		dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));

		//запускаем музыку и задаем повторение музыки (true)
		rainMusic.setLooping(true);
		rainMusic.play();

		//
		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		//создаем экземпляр массива капель
		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

    // метод для капель
	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800 - 64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void render (float delta ) {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		game.batch.setProjectionMatrix(camera.combined);
		game.batch.begin();

		//оповещение о количестве собраных капель
		game.font.draw(game.batch, "Drops Collected: " + dropsGatchered, 0, 480);

		game.batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop: raindrops) {
			game.batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		game.batch.end();

		if (Gdx.input.isTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = (int) (touchPos.x - 64 / 2);
		}

		//для десктопа и браузера движение
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			bucket.x += 200 * Gdx.graphics.getDeltaTime();
		}

		//проверяем не заходил ли за пределы экрана
		if (bucket.x < 0) {
			bucket.x = 0;
		}
		if (bucket.x > 800 - 64) {
			bucket.x = 800 - 64;
		}

		//сколько времени прошло после первой капли и если необхоимо то создаем новую
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) {
			spawnRaindrop();
		}

		Iterator<Rectangle> iter = raindrops.iterator();

		//цикл обновления капли
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0) {
				iter.remove();
			}

			//столкновение капли с ведром
			if (raindrop.overlaps(bucket)) {
				//увеличиваем переменную счетчика капель
				dropsGatchered++;

				dropSound.play();
				iter.remove();
			}
		}
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

	//освобождаем программу нагружеными ресурсами при ее завершении
	@Override
	public void dispose () {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
	}

	@Override
	public void show() {
		rainMusic.play();
	}
}
