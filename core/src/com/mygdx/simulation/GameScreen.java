package com.mygdx.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Digilogue on 19/11/2016.
 */
public class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 8640;
    private static final float WORLD_HEIGHT = 6480;

    private static final float SCREEN_WIDTH = 1024;
    private static final float SCREEN_HEIGHT = 768;

    private final TransportSimulation transportSimulation;
    private Viewport viewport;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;
    private Taxi taxi;
    private Array<Rectangle> platforms = new Array<Rectangle>();

    public GameScreen(TransportSimulation transportSimulation) {
        this.transportSimulation = transportSimulation;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void show() {
        super.show();
        camera = new OrthographicCamera();

        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
        viewport.apply(false);

        batch = new SpriteBatch();

        tiledMap = transportSimulation.getAssetManager().get("simulation.tmx");
        orthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);
        // orthogonalTiledMapRenderer.setView(camera);

        camera.position.set(512, 3240, camera.position.z);
        camera.update();
        orthogonalTiledMapRenderer.setView(camera);

        taxi = new Taxi(transportSimulation);

        buildPlatforms();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update(delta);
        clearScreen();
        draw(delta);
    }

    private void draw(float delta) {
        // camera.update();
        batch.setProjectionMatrix(camera.combined);
        // batch.setTransformMatrix(camera.view);
//        orthogonalTiledMapRenderer.render();

        batch.begin();

        // update(delta);
        taxi.draw(batch);

        batch.end();
    }

    private void update(float delta) {
        taxi.update(delta, platforms, WORLD_WIDTH, WORLD_HEIGHT);
        camera.update();
        orthogonalTiledMapRenderer.setView(camera);
        updateCamera();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        orthogonalTiledMapRenderer.render();
    }

    private void buildPlatforms() {
        MapObjects objects = tiledMap.getLayers().get("Platforms").getObjects();

        for (MapObject object : objects) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            platforms.add(rectangle);
            System.out.println();
            System.out.println("name:" + object.getName());
            System.out.println("x: " + rectangle.getX());
            System.out.println("y: " + rectangle.getY());
            System.out.println("width: " + rectangle.getWidth());
            System.out.println("height: " + rectangle.getHeight());
        }
    }

    private void updateCamera() {

        TiledMapTileLayer tiledMapTileLayer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
        float levelWidth = tiledMapTileLayer.getWidth() * tiledMapTileLayer.getTileWidth();
        float levelHeight = tiledMapTileLayer.getHeight() * tiledMapTileLayer.getTileHeight();

        if ((taxi.getX() > SCREEN_WIDTH * 0.70f) && (taxi.getX() < levelWidth - (SCREEN_WIDTH * 0.30f)) && taxi
                .getTravellingLeftRightDirection() == Taxi.Direction.RIGHT && taxi.getX() >
                (camera.position.x) +
                        (SCREEN_WIDTH * 0.20f)) {
            camera.position.set(taxi.getX() - (SCREEN_WIDTH * 0.20f), camera.position.y, camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }

        if ((taxi.getX() < levelWidth - (SCREEN_WIDTH * 0.30f)) && (taxi.getX() > (SCREEN_WIDTH * 0.30f)) && taxi
                .getTravellingLeftRightDirection() == Taxi.Direction.LEFT && taxi.getX() < (camera.position.x) -
                (SCREEN_WIDTH * 0.20f)) {
            camera.position.set(taxi.getX() + (SCREEN_WIDTH * 0.20f), camera.position.y, camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }

        if ((taxi.getY() > SCREEN_HEIGHT * 0.70f) && (taxi.getY() < levelHeight - (SCREEN_HEIGHT * 0.30f)) && taxi
                .getTravellingUpDownDirection() == Taxi.Direction.UP && taxi.getY() >
                (camera.position.y) +
                        (SCREEN_HEIGHT * 0.20f)) {
            camera.position.set(camera.position.x, taxi.getY() - (SCREEN_HEIGHT * 0.20f), camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }

        if ((taxi.getY() < levelHeight - (SCREEN_HEIGHT * 0.30f)) && (taxi.getY() > (SCREEN_HEIGHT * 0.30f)) && taxi
                .getTravellingUpDownDirection() == Taxi.Direction.DOWN && taxi.getY() < (camera.position.y) -
                (SCREEN_HEIGHT * 0.20f)) {
            camera.position.set(camera.position.x, taxi.getY() + (SCREEN_HEIGHT * 0.20f), camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }
    }

}