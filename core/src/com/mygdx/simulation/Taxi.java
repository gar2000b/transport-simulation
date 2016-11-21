package com.mygdx.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Digilogue on 19/11/2016.
 *
 * Taxi Class - represents the in-simulation sprite for the Aerial Taxi vehicle. Maintains state, update logic and
 * rendering.
 *
 */
public class Taxi {

    private static final float DIVE_ACCEL = 0.075F;
    private static final float HORIZ_ACCEL = 0.15F;
    private static final float THRUST_UP_ACCEL = 0.2F;
    private static final float THRUST_DOWN_ACCEL = 0.05F;
    private static final float LANDING_PYLON_LEFT_X = 5;
    private static final float LANDING_PYLON_RIGHT_X = 80;
    private static final float TAXI_LENGTH = 85;
    private static final float TAXI_HEIGHT = 43;

    private final Texture taxiLeft;
    private final Texture taxiRight;
    private final Texture taxiLeftGear;
    private final Texture taxiRightGear;

    public enum Direction {UP, DOWN, LEFT, RIGHT}

    private Direction direction = Direction.RIGHT;
    private Direction leftRightFlag = Direction.RIGHT;
    private Direction travellingLeftRightDirection = Direction.RIGHT; // The current direction of travel.
    private Direction travellingUpDownDirection = Direction.DOWN; // The current direction of travel.

    private boolean landingGear = true;

    private float x = 80;
    private float y = 3333;

    private float previousX = 0;
    private float previousY = -7;

    private float ySpeed = 0;
    private float xSpeed = 0;

    private boolean landed = false;

    /**
     * Constructor initialising sprite textures (taxiLeft, taxiRight, taxiLeftGear, taxiRightGear)
     *
     * @param transportSimulation
     */
    public Taxi(TransportSimulation transportSimulation) {

        taxiLeft = transportSimulation.getAssetManager().get("Taxi-Left.png", Texture.class);
        taxiRight = transportSimulation.getAssetManager().get("Taxi-Right.png", Texture.class);
        taxiLeftGear = transportSimulation.getAssetManager().get("Taxi-Left-Gear.png", Texture.class);
        taxiRightGear = transportSimulation.getAssetManager().get("Taxi-Right-Gear.png", Texture.class);
    }

    /**
     * Taxi's draw method for rendering appropriate sprite textures (taxiLeft, taxiRight, taxiLeftGear, taxiRightGear)
     * at current position.
     *
     * @param batch
     */
    public void draw(Batch batch) {
        if (leftRightFlag == Direction.LEFT) {
            if (landingGear)
                batch.draw(taxiLeftGear, Math.round(x), Math.round(y));
            else
                batch.draw(taxiLeft, Math.round(x), Math.round(y));
        }
        if (leftRightFlag == Direction.RIGHT) {
            if (landingGear)
                batch.draw(taxiRightGear, Math.round(x), Math.round(y));
            else
                batch.draw(taxiRight, Math.round(x), Math.round(y));
        }
    }

    /**
     * Main update logic for the taxi. Processes user input, processes collisions with platforms, works out travel
     * direction, processes collisions with world boundaries, processes dive logic and finally sets the taxi's
     * position.
     *
     * @param delta
     * @param platforms
     * @param worldWidth
     * @param worldHeight
     */
    public void update(float delta, Array<Rectangle> platforms, float worldWidth, float worldHeight) {

        processInput();
        processCollisionWithPlatforms(platforms);
        workOutTravellingDirection();
        processCollisionWithWorldBoundaries(worldWidth, worldHeight);
        processDive();

        setPosition(x + xSpeed, y + ySpeed);
    }

    /**
     * If taxi is not landed then proceed with dive.
     */
    private void processDive() {
        if (!landed)
            ySpeed -= DIVE_ACCEL;
    }

    /**
     * Detects if taxi collides with any of the world boundaries then prevents that direction from advancing beyond
     * the boundary.
     *
     * @param worldWidth
     * @param worldHeight
     */
    private void processCollisionWithWorldBoundaries(float worldWidth, float worldHeight) {
        if (x < 0) {
            x = 0;
            xSpeed = 0;
        }

        if (x > worldWidth - TAXI_LENGTH) {
            x = worldWidth - TAXI_LENGTH;
            xSpeed = 0;
        }

        if (y < 0) {
            y = 0;
            ySpeed = 0;
            landed = true;
        }

        if (y > worldHeight - TAXI_HEIGHT) {
            y = worldHeight - TAXI_HEIGHT;
            ySpeed = 0;
        }
    }

    /**
     * Processes user input and updates taxi's x, y, speed and direction states accordingly.
     */
    private void processInput() {
        Input input = Gdx.input;
        if (input.isKeyPressed(Input.Keys.RIGHT)) {
            direction = Direction.RIGHT;
            leftRightFlag = Direction.RIGHT;
            xSpeed += HORIZ_ACCEL;
        }
        if (input.isKeyPressed(Input.Keys.LEFT)) {
            direction = Direction.LEFT;
            leftRightFlag = Direction.LEFT;
            xSpeed -= HORIZ_ACCEL;
        }
        if (input.isKeyPressed(Input.Keys.UP)) {
            direction = Direction.UP;
            ySpeed += THRUST_UP_ACCEL;
            landed = false;
        }
        if (input.isKeyPressed(Input.Keys.DOWN)) {
            direction = Direction.DOWN;
            ySpeed -= THRUST_DOWN_ACCEL;
        }
        if (input.isKeyJustPressed(Input.Keys.SPACE)) {
            landingGear = !landingGear;
        }
    }

    /**
     * Works out the travelling direction of taxi and sets appropriate state.
     */
    private void workOutTravellingDirection() {
        if (previousX > x)
            travellingLeftRightDirection = Direction.LEFT;
        if (previousX < x)
            travellingLeftRightDirection = Direction.RIGHT;

        if (previousY > y)
            travellingUpDownDirection = Direction.DOWN;
        if (previousY < y)
            travellingUpDownDirection = Direction.UP;

    }

    /**
     * Detects whether taxi has landed on a platform. If does this if both landing pylons land within the platform
     * boundaries. If so, stop taxi and set to landed.
     *
     * @param platforms
     */
    private void processCollisionWithPlatforms(Array<Rectangle> platforms) {

        if (landingGear) {
            for (Rectangle rectangle : platforms) {

                if (((rectangle.getY() + rectangle.getHeight() <= previousY &&
                        rectangle.getY() + rectangle.getHeight() >= y))) {
                    float landingPylonOffsetLeftX = LANDING_PYLON_LEFT_X + x;
                    float landingPylonOffsetRightX = LANDING_PYLON_RIGHT_X + x;
                    float platformRightEdge = rectangle.getX() + rectangle.getWidth();

                    if ((rectangle.getX() <= landingPylonOffsetLeftX) &&
                            (platformRightEdge >= landingPylonOffsetRightX)) {
                        xSpeed = 0;
                        ySpeed = 0;
                        landed = true;
                    }
                }
            }
        }
    }

    /**
     * Sets the position of the taxi and takes a note of it's previous position.
     *
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        this.previousX = this.x;
        this.previousY = this.y;

        this.x = x;
        this.y = y;
    }

    /**
     * Get x
     *
     * @return x
     */
    public float getX() {
        return x;
    }

    /**
     * Get y
     *
     * @return y
     */
    public float getY() {
        return y;
    }

    /**
     * Get travellingLeftRightDirection
     *
     * @return travellingLeftRightDirection
     */
    public Direction getTravellingLeftRightDirection() {
        return travellingLeftRightDirection;
    }

    /**
     * Get travellingUpDownDirection
     *
     * @return travellingUpDownDirection
     */
    public Direction getTravellingUpDownDirection() {
        return travellingUpDownDirection;
    }
}