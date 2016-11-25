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
import com.mygdx.simulation.dto.TransportationHubs;
import com.mygdx.simulation.travellers.Traveller;
import com.mygdx.simulation.travellers.thread.TravellerInjector;
import com.mygdx.simulation.vehicles.Taxi;

/**
 * Created by Digilogue on 19/11/2016.
 */
public class World extends ScreenAdapter {

    private enum SimulationMode {PAN, TAXI}

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
    private Array<Traveller> travellers = new Array<Traveller>();
    private Array<Rectangle> platforms = new Array<Rectangle>();
    private Array<Rectangle> grounds = new Array<Rectangle>();
    private Array<Rectangle> podStops = new Array<Rectangle>();
    private Array<MapLayer> mapLayers = new Array<MapLayer>();
    private Array<Boolean> mapLayersOnOff = new Array<Boolean>();
    private SimulationMode simulationMode = SimulationMode.TAXI;

    private boolean displayAllLayersFlag = true;
    private boolean pauseFlag = false;

    public World(TransportSimulation transportSimulation) {
        this.transportSimulation = transportSimulation;
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

        buildPlatforms();
        buildGrounds();
        buildPodStops();

        createTravellers();

        System.out.println("count of platforms: " + platforms.size);
        System.out.println("count of grounds: " + grounds.size);
        System.out.println("count of pod stops: " + podStops.size);

        batch.setProjectionMatrix(camera.combined);

        createMapLayerArray();
    }

    private void createTravellers() {

        TravellerInjector travellerInjector = new TravellerInjector(grounds, travellers, transportSimulation, this);
        travellerInjector.start();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update(delta);
        clearScreen();
        draw();
    }

    /**
     * Simulation logic updates for all in-simulation objects (Taxis, Travellers, Pods...)
     *
     * @param delta
     */
    private void update(float delta) {
        if (!pauseFlag) {
            updateTaxi(delta);
            updateTravellers(delta);
        }
        camera.update();
        orthogonalTiledMapRenderer.setView(camera);
        updateCamera();
        toggleLayers();
        toggleMode();
        togglePause();
    }

    private void updateTaxi(float delta) {
        if (simulationMode == SimulationMode.TAXI)
            taxi.update(delta, platforms, WORLD_WIDTH, WORLD_HEIGHT);
    }

    private void updateTravellers(float delta) {
        for (int i = 0; i < travellers.size; i++) {
            travellers.get(i).update(delta);
        }
    }


    private void draw() {
        orthogonalTiledMapRenderer.render();

        batch.begin();
        taxi.draw(batch);
        // System.out.println("travellers size is: " + travellers.size);
        for (int i = 0; i < travellers.size; i++) {
            travellers.get(i).draw(batch);
        }
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

    private void buildGrounds() {
        MapObjects objects = tiledMap.getLayers().get("Ground").getObjects();

        for (MapObject object : objects) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            grounds.add(rectangle);
        }
    }

    private void buildPodStops() {
        MapObjects objects = tiledMap.getLayers().get("Pod-Stops").getObjects();

        for (MapObject object : objects) {
            Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
            podStops.add(rectangle);
        }
    }

    private Rectangle getStreetLevelGround() {
        MapObjects objects = tiledMap.getLayers().get("Ground").getObjects();
        return ((RectangleMapObject) objects.get("Street-Level-Ground")).getRectangle();
    }

    private Rectangle getTestGround() {
        MapObjects objects = tiledMap.getLayers().get("Ground").getObjects();
        return ((RectangleMapObject) objects.get("Test-Ground")).getRectangle();
    }


    /**
     * This method will update the zoom depth and position of the camera relative to the mode that is set by the
     * keyboard. If it's in taxi mode, it will trail / lead the taxi (controlled by the user) as appropriate. If it's
     * in pan mode, the user can control the position of the camera with the cursor keys.
     */
    private void updateCamera() {

        if (simulationMode == SimulationMode.TAXI)
            updateCameraTaxi();

        if (simulationMode == SimulationMode.PAN)
            updateCameraPan();

        cameraZoom();
    }

    /**
     * Camera position is updated relative to Taxi's position following it when it reaches the trailing distances
     * of 30% of the screen width either side of the world.
     * 30% || 40% || 30% - in other words, the camera won't trail / lead Taxi in either the middle 40% of the screen or
     * the remaining 30% of the screen width at either of the boundaries of the world. It will trail / lead Taxi at
     * 30% of the screen until the remaining 30% at the boundaries.
     */
    private void updateCameraTaxi() {
        updateCameraTrailingTaxiTravellingRight();
        updateCameraLeadingTaxiTravellingLeft();
        updateCameraTrailingTaxiTravellingUp();
        updateCameraLeadingTaxiTravellingDown();
    }

    /**
     *
     */
    private void updateCameraPan() {
        Input input = Gdx.input;
        if (input.isKeyPressed(Input.Keys.LEFT))
            camera.position.x = camera.position.x - 10;
        if (input.isKeyPressed(Input.Keys.RIGHT))
            camera.position.x = camera.position.x + 10;
        if (input.isKeyPressed(Input.Keys.UP))
            camera.position.y = camera.position.y + 10;
        if (input.isKeyPressed(Input.Keys.DOWN))
            camera.position.y = camera.position.y - 10;
    }

    /**
     * Algorithm:
     * <p>
     * This will allow the camera to follow Taxi as it travels left:
     * If taxis position is less than 30% of the screen width from the world width &
     * Taxis position is greater than 30% of the screen width &
     * Taxis direction is facing left &
     * Taxis position is less than 20% of the screen width from the current camera position
     * Then update the new camera position to lead +20% of the screen width from Taxis position.
     */
    private void updateCameraLeadingTaxiTravellingLeft() {
        if ((taxi.getX() < WORLD_WIDTH - (SCREEN_WIDTH * 0.30f)) && (taxi.getX() > (SCREEN_WIDTH * 0.30f)) && taxi
                .getTravellingLeftRightDirection() == Taxi.Direction.LEFT && taxi.getX() < (camera.position.x) -
                (SCREEN_WIDTH * 0.20f)) {
            camera.position.set(taxi.getX() + (SCREEN_WIDTH * 0.20f), camera.position.y, camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }
    }

    /**
     * Algorithm:
     * <p>
     * This will allow the camera to follow Taxi as it travels right:
     * If Taxis position is greater than 70% of the screen width &
     * Taxis position is less than the last remaining 30% of the screen width of the world &
     * Taxis direction is facing right &
     * Taxis position is greater than 20% of the screen width from the current camera position
     * Then update the new camera position to trail -20% of the screen width from Taxis position.
     */
    private void updateCameraTrailingTaxiTravellingRight() {
        if ((taxi.getX() > SCREEN_WIDTH * 0.70f) && (taxi.getX() < WORLD_WIDTH - (SCREEN_WIDTH * 0.30f)) && taxi
                .getTravellingLeftRightDirection() == Taxi.Direction.RIGHT && taxi.getX() >
                (camera.position.x) +
                        (SCREEN_WIDTH * 0.20f)) {
            camera.position.set(taxi.getX() - (SCREEN_WIDTH * 0.20f), camera.position.y, camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }
    }

    /**
     * TODO - base it on updateCameraLeadingTaxiTravellingLeft()
     */
    private void updateCameraLeadingTaxiTravellingDown() {
        if ((taxi.getY() < WORLD_HEIGHT - (SCREEN_HEIGHT * 0.30f)) && (taxi.getY() > (SCREEN_HEIGHT * 0.30f)) && taxi
                .getTravellingUpDownDirection() == Taxi.Direction.DOWN && taxi.getY() < (camera.position.y) -
                (SCREEN_HEIGHT * 0.20f)) {
            camera.position.set(camera.position.x, taxi.getY() + (SCREEN_HEIGHT * 0.20f), camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }
    }

    /**
     * TODO - base it on updateCameraTrailingTaxiTravellingRight()
     */
    private void updateCameraTrailingTaxiTravellingUp() {
        if ((taxi.getY() > SCREEN_HEIGHT * 0.70f) && (taxi.getY() < WORLD_HEIGHT - (SCREEN_HEIGHT * 0.30f)) && taxi
                .getTravellingUpDownDirection() == Taxi.Direction.UP && taxi.getY() >
                (camera.position.y) +
                        (SCREEN_HEIGHT * 0.20f)) {
            camera.position.set(camera.position.x, taxi.getY() - (SCREEN_HEIGHT * 0.20f), camera.position.z);
            camera.update();
            orthogonalTiledMapRenderer.setView(camera);
        }
    }

    private void cameraZoom() {
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

    private void toggleMode() {
        Input input = Gdx.input;
        if (input.isKeyJustPressed(Input.Keys.M)) {
            if (simulationMode == SimulationMode.TAXI) {
                simulationMode = SimulationMode.PAN;
                return;
            }
            if (simulationMode == SimulationMode.PAN)
                simulationMode = SimulationMode.TAXI;
            return;
        }
    }

    private void togglePause() {
        Input input = Gdx.input;
        if (input.isKeyJustPressed(Input.Keys.P)) {
            pauseFlag = !pauseFlag;
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

    public TransportationHubs getTransportationHubs(Rectangle ground) {

        Array<Rectangle> podStopsForTransfer = new Array<Rectangle>();
        Array<Rectangle> platformsForTransfer = new Array<Rectangle>();

        // First of all, check all Pod Stops that lie directly on top of this ground.
        for (Rectangle podStop : podStops) {
            if (ground.getY() == (podStop.getY() - podStop.getHeight()) && podStop.getX() > ground.getX() && (podStop
                    .getX() + podStop.getWidth()) < (ground.getX() + ground.getWidth())) {
                podStopsForTransfer.add(podStop);
            }
        }

        System.out.println();
        System.out.println("# ground y: " + ground.getY());
        // Next, check all Platforms that lie directly on top of this ground.
        for (Rectangle platform : platforms) {
            System.out.println("# platform y: " + platform.getY());
            System.out.println("# platform height: " + platform.getHeight());
            System.out.println();
            if (ground.getY() == (platform.getY() - 1) && platform.getX() > ground.getX() && (platform
                    .getX() + platform.getWidth()) < (ground.getX() + ground.getWidth())) {
                System.out.println("# platform should be getting added now.");
                platformsForTransfer.add(platform);
            }
        }

        TransportationHubs transportationHubs = new TransportationHubs();
        transportationHubs.setPodStops(podStopsForTransfer);
        transportationHubs.setPlatforms(platformsForTransfer);

        return transportationHubs;
    }

}
