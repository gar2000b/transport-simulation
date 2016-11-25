package com.mygdx.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by Digilogue on 19/11/2016.
 */
public class LoadingScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 1600;
    private static final float WORLD_HEIGHT = 900;
    private static final float PROGRESS_BAR_WIDTH = 100;
    private static final float PROGRESS_BAR_HEIGHT = 25;

    private Viewport viewport;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    private float progress = 0;
    private final TransportSimulation transportSimulation;

    public LoadingScreen(TransportSimulation transportSimulation) {
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
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();

        transportSimulation.getAssetManager().load("Taxi-Left.png", Texture.class);
        transportSimulation.getAssetManager().load("Taxi-Right.png", Texture.class);
        transportSimulation.getAssetManager().load("Taxi-Left-Gear.png", Texture.class);
        transportSimulation.getAssetManager().load("Taxi-Right-Gear.png", Texture.class);
        transportSimulation.getAssetManager().load("simulation.tmx", TiledMap.class);
        transportSimulation.getAssetManager().load("thruster-up.png", Texture.class);
        transportSimulation.getAssetManager().load("thruster-down.png", Texture.class);
        transportSimulation.getAssetManager().load("thruster-left.png", Texture.class);
        transportSimulation.getAssetManager().load("thruster-right.png", Texture.class);
        transportSimulation.getAssetManager().load("traveller-a.png", Texture.class);
        transportSimulation.getAssetManager().load("traveller-b.png", Texture.class);
        transportSimulation.getAssetManager().load("traveller-c.png", Texture.class);
        transportSimulation.getAssetManager().load("thought-bubble.png", Texture.class);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update();
        clearScreen();
        draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
    }

    private void update() {
        if (transportSimulation.getAssetManager().update()) {
            transportSimulation.setScreen(new World(transportSimulation));
        } else {
            progress = transportSimulation.getAssetManager().getProgress();
        }
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw() {
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect((WORLD_WIDTH - PROGRESS_BAR_WIDTH) / 2, WORLD_HEIGHT / 2 - PROGRESS_BAR_HEIGHT / 2,
                progress * PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        shapeRenderer.end();
    }
}