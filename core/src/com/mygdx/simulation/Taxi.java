package com.mygdx.simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Digilogue on 19/11/2016.
 */
public class Taxi {

    private static final float DIVE_ACCEL = 0.075F;
    private static final float THRUST_UP_ACCEL = 0.20F;
    private static final float LANDING_PYLON_LEFT_X = 5;
    private static final float LANDING_PYLON_LEFT_Y = 0;
    private static final float LANDING_PYLON_RIGHT_X = 80;
    private static final float LANDING_PYLON_RIGHT_Y = 0;
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

    public Taxi(TransportSimulation transportSimulation) {

        System.out.println("* constructor called");

        taxiLeft = transportSimulation.getAssetManager().get("Taxi-Left.png", Texture.class);
        taxiRight = transportSimulation.getAssetManager().get("Taxi-Right.png", Texture.class);
        taxiLeftGear = transportSimulation.getAssetManager().get("Taxi-Left-Gear.png", Texture.class);
        taxiRightGear = transportSimulation.getAssetManager().get("Taxi-Right-Gear.png", Texture.class);
    }

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

    public void update(float delta, Array<Rectangle> platforms, float worldWidth, float worldHeight) {

        Input input = Gdx.input;
        if (input.isKeyPressed(Input.Keys.RIGHT)) {
            direction = Direction.RIGHT;
            leftRightFlag = Direction.RIGHT;
            xSpeed += DIVE_ACCEL;
        }
        if (input.isKeyPressed(Input.Keys.LEFT)) {
            direction = Direction.LEFT;
            leftRightFlag = Direction.LEFT;
            xSpeed -= DIVE_ACCEL;
        }
        if (input.isKeyPressed(Input.Keys.UP)) {
            direction = Direction.UP;
            ySpeed += THRUST_UP_ACCEL;
            landed = false;
        }
        if (input.isKeyPressed(Input.Keys.DOWN)) {
            direction = Direction.DOWN;
        }
        if (input.isKeyJustPressed(Input.Keys.SPACE)) {
            landingGear = !landingGear;
        }

        detectCollisionWithPlatforms(platforms);

        workOutTravellingDirection();

        // If taxi collides with any of the world boundaries then stop it.
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

        if (!landed)
            ySpeed -= DIVE_ACCEL;

        setPosition(x + xSpeed, y + ySpeed);
    }

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

    private void detectCollisionWithPlatforms(Array<Rectangle> platforms) {

        if (landingGear) {
            for (Rectangle rectangle : platforms) {

                if (((rectangle.getY() + rectangle.getHeight() <= previousY && rectangle.getY() + rectangle.getHeight() >= y))) {
                    float landingPylonOffsetLeftX = LANDING_PYLON_LEFT_X + x;
                    float landingPylonOffsetRightX = LANDING_PYLON_RIGHT_X + x;
                    float platformRightEdge = rectangle.getX() + rectangle.getWidth();

                    if ((rectangle.getX() <= landingPylonOffsetLeftX) && (platformRightEdge >= landingPylonOffsetRightX)) {
                        System.out.println("Houston, we have landed!!!");
                        xSpeed = 0;
                        ySpeed = 0;
                        landed = true;
                    }
                    // System.out.println("Collision occurred at: " + x + ", " + y);
                }
            }
        }
    }

    public void update2(float delta) {
        // System.out.println(delta);
        Input input = Gdx.input;
        if (input.isKeyPressed(Input.Keys.RIGHT)) {
            direction = Direction.RIGHT;
            leftRightFlag = Direction.RIGHT;
            xSpeed = 2;
        }
        if (input.isKeyPressed(Input.Keys.LEFT)) {
            direction = Direction.LEFT;
            leftRightFlag = Direction.LEFT;
            xSpeed = -2;
        }
        if (input.isKeyPressed(Input.Keys.UP)) {
            direction = Direction.UP;
            ySpeed = 2;
        }
        if (input.isKeyPressed(Input.Keys.DOWN)) {
            direction = Direction.DOWN;
            ySpeed = -2;
        }
        if (input.isKeyJustPressed(Input.Keys.SPACE)) {
            landingGear = !landingGear;
        }

        setPosition(x + xSpeed, y + ySpeed);
    }

    public void setPosition(float x, float y) {
        this.previousX = this.x;
        this.previousY = this.y;

        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Direction getTravellingLeftRightDirection() {
        return travellingLeftRightDirection;
    }

    public Direction getTravellingUpDownDirection() {
        return travellingUpDownDirection;
    }
}