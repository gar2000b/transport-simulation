package com.mygdx.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.simulation.travellers.TravellerA;
import com.mygdx.simulation.vehicles.Taxi;

/**
 * Created by Digilogue on 19/11/2016.
 */
public class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 8640;
    private static final float WORLD_HEIGHT = 6480;

    private static final float SCREEN_WIDTH = 1600;
    private static final float SCREEN_HEIGHT = 900;

    private final TransportSimulation transportSimulation;
    private Viewport viewport;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;
    private Taxi taxi;
    private TravellerA travellerA;
    private Array<Rectangle> platforms = new Array<Rectangle>();
    private Array<MapLayer> mapLayers = new Array<MapLayer>();
    private Array<Boolean> mapLayersOnOff = new Array<Boolean>();

    private boolean displayAllLayersFlag = true;

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

        camera.position.set(SCREEN_WIDTH / 2, WORLD_HEIGHT / 2, camera.position.z);
        camera.update();
        orthogonalTiledMapRenderer.setView(camera);

        taxi = new Taxi(transportSimulation);
        travellerA = new TravellerA(transportSimulation);

        buildPlatforms();
        batch.setProjectionMatrix(camera.combined);

        createMapLayerArray();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update(delta);
        clearScreen();
        draw();
    }

    private void update(float delta) {
        taxi.update(delta, platforms, WORLD_WIDTH, WORLD_HEIGHT);
        travellerA.update(delta);
        camera.update();
        orthogonalTiledMapRenderer.setView(camera);
        updateCamera();
        toggleLayers();
    }


    private void draw() {
        orthogonalTiledMapRenderer.render();

        batch.begin();
        taxi.draw(batch);
        travellerA.draw(batch);
        batch.end();
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void buildPlatforms() {
        MapObjects objects = tiledMap.getLayers().get("Platforms").getObjects();

        for (MapObject object : objects) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            platforms.add(rectangle);
        }
    }

    private void updateCamera() {

//        TiledMapTileLayer tiledMapTileLayer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
//        float levelWidth = tiledMapTileLayer.getWidth() * tiledMapTileLayer.getTileWidth();
//        float levelHeight = tiledMapTileLayer.getHeight() * tiledMapTileLayer.getTileHeight();

        if ((taxi.getX() > SCREEN_WIDTH * 0.70f) && (taxi.getX() < WORLD_WIDTH - (SCREEN_WIDTH * 0.30f)) && taxi
                .getTravellingLeftRightDirection() == Taxi.Direction.RIGHT && taxi.getX() >
                (camera.position.x) +
                        (SCREEN_WIDTH * 0.20f)) {
            camera.position.set(taxi.getX() - (SCREEN_WIDTH * 0.20f), camera.position.y, camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }

        if ((taxi.getX() < WORLD_WIDTH - (SCREEN_WIDTH * 0.30f)) && (taxi.getX() > (SCREEN_WIDTH * 0.30f)) && taxi
                .getTravellingLeftRightDirection() == Taxi.Direction.LEFT && taxi.getX() < (camera.position.x) -
                (SCREEN_WIDTH * 0.20f)) {
            camera.position.set(taxi.getX() + (SCREEN_WIDTH * 0.20f), camera.position.y, camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }

        if ((taxi.getY() > SCREEN_HEIGHT * 0.70f) && (taxi.getY() < WORLD_HEIGHT - (SCREEN_HEIGHT * 0.30f)) && taxi
                .getTravellingUpDownDirection() == Taxi.Direction.UP && taxi.getY() >
                (camera.position.y) +
                        (SCREEN_HEIGHT * 0.20f)) {
            camera.position.set(camera.position.x, taxi.getY() - (SCREEN_HEIGHT * 0.20f), camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }

        if ((taxi.getY() < WORLD_HEIGHT - (SCREEN_HEIGHT * 0.30f)) && (taxi.getY() > (SCREEN_HEIGHT * 0.30f)) && taxi
                .getTravellingUpDownDirection() == Taxi.Direction.DOWN && taxi.getY() < (camera.position.y) -
                (SCREEN_HEIGHT * 0.20f)) {
            camera.position.set(camera.position.x, taxi.getY() + (SCREEN_HEIGHT * 0.20f), camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }

        Input input = Gdx.input;
        if (input.isKeyPressed(Input.Keys.S)) {
            camera.zoom += 0.02;
        }

        if (input.isKeyPressed(Input.Keys.A)) {
            camera.zoom -= 0.02;
        }

        if (input.isKeyPressed(Input.Keys.R)) {
            camera.zoom = 1;
        }
    }

    private void createMapLayerArray() {
        for (int i = 0; i < tiledMap.getLayers().getCount(); i++) {
            mapLayers.add(tiledMap.getLayers().get(i));
            mapLayersOnOff.add(true);
        }
    }

    private void toggleLayers() {
        Input input = Gdx.input;
        if (input.isKeyJustPressed(Input.Keys.NUM_1)) {
            toggleLayer(0);
            removeAllLayersFromTiledMap();
            addLayersToTiledMap();
        }

        if (input.isKeyJustPressed(Input.Keys.NUM_2)) {
            toggleLayer(1);
            removeAllLayersFromTiledMap();
            addLayersToTiledMap();
        }

        if (input.isKeyJustPressed(Input.Keys.NUM_0)) {
            toggleAllLayersOnOff();
            removeAllLayersFromTiledMap();
            addLayersToTiledMap();
        }
    }

    private void toggleAllLayersOnOff() {
        displayAllLayersFlag = !displayAllLayersFlag;
        for (int i = 0; i < mapLayersOnOff.size; i++)
            mapLayersOnOff.set(i, displayAllLayersFlag);
    }

    private void toggleLayer(int i) {
        if (mapLayersOnOff.get(i).equals(true))
            mapLayersOnOff.set(i, false);
        else
            mapLayersOnOff.set(i, true);
    }

    private void addLayersToTiledMap() {
        for (int i = 0; i < mapLayers.size; i++) {
            if (mapLayersOnOff.get(i).equals(true))
                tiledMap.getLayers().add(mapLayers.get(i));
        }
    }

    private void removeAllLayersFromTiledMap() {
        for (int i = 0; i < tiledMap.getLayers().getCount(); i++) {
            tiledMap.getLayers().remove(0);
        }
    }

}
