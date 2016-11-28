package com.mygdx.simulation.travellers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.simulation.TransportSimulation;
import com.mygdx.simulation.World;
import com.mygdx.simulation.dto.TransportationHubs;

import java.util.Random;

/**
 * Created by Digilogue on 23/11/2016.
 */
public class Traveller {

    static final float FRAME_DURATION = 0.1F;
    private final World world;

    TextureRegion[][] travellerTextures;
    Animation travellerWalkingAnimation;
    TextureRegion travellerToRender;
    Texture thoughtBubble;
    Texture taxiBubble;

    Rectangle currentGround;
    private Rectangle currentPlatform;

    private float animationTimer = 0;
    private float timeElapsed = 0;
    private float x = 300;
    private float y = 3333;

    int tileWidth;
    int tileHeight;

    private int modeTime = 0;
    private boolean walkingToTaxiPlatformFlag = false;

    public enum Direction {LEFT, RIGHT}

    public enum Mode {WALK, THINK, WALK_TO_TAXI_PLATFORM, WALK_POD_STOP, STOP, CALL_TAXI, CALL_POD, TAXI_ON_ITS_WAY}

    private Direction travellingDirection = Direction.RIGHT;

    private Mode mode = Mode.WALK;

    public Traveller(TransportSimulation transportSimulation, World world) {
        thoughtBubble = transportSimulation.getAssetManager().get("thought-bubble.png", Texture.class);
        taxiBubble = transportSimulation.getAssetManager().get("taxi-bubble.png", Texture.class);
        this.world = world;
    }

    public void update(float delta) {
        timeElapsed += delta;
        updateAnimationTimers(delta);

        if (getMode() == Mode.WALK)
            walking();

        if (getMode() == Mode.STOP)
            stopped();

        if (getMode() == Mode.THINK)
            thinking();

        if (getMode() == Mode.WALK_TO_TAXI_PLATFORM)
            walkToTaxiPlatform();

        setPosition(x, y);
    }

    private void walkToTaxiPlatform() {
        // Determine if taxi platform exists on this ground (by contacting world).
        // If so, choose one then proceed to walk until you reach sed taxi platform.
        // Else, generate new mode.

        if (!walkingToTaxiPlatformFlag) {
//            System.out.println("* walkToTaxiPlatform called.");
            TransportationHubs transportationHubs = world.getTransportationHubs(currentGround);
            if (transportationHubs.getPlatforms().size > 0) {
                System.out.println("*** YES, there are indeed taxi platforms on this ground");
                int platformIndex = generateRandom(transportationHubs.getPlatforms().size - 1);
                currentPlatform = transportationHubs.getPlatforms().get(platformIndex);
                walkingToTaxiPlatformFlag = true;
            }
        } else {
            // Take a step closer to taxi platform and determine if we have reached our destination.
            walk();
            // reset all flags / modes.
//            System.out.println("* x is: " + x);
//            System.out.println("* currentPlatform x is: " + currentPlatform.getX());
            if (x == currentPlatform.getX()) {
                mode = Mode.CALL_TAXI;
                modeTime = -1;
                walkingToTaxiPlatformFlag = false;
                System.out.println("!!! Hey dudes, I've reached the taxi platform stop.");
                return;
            }
        }

        if (!walkingToTaxiPlatformFlag)
            generateNewMode();

    }

    private void thinking() {
        if (timeElapsed > modeTime) {
            generateNewMode();
            return;
        }
    }

    private void stopped() {
        if (timeElapsed > modeTime) {
            generateNewMode();
            return;
        }
    }

    private void walking() {

        if (timeElapsed > modeTime) {
            generateNewMode();
            return;
        }

        walk();
    }

    private void walk() {
        if (travellingDirection == Direction.RIGHT) {
            if (x < currentGround.getX() + (currentGround.getWidth() - tileWidth)) {
                x++;
            } else {
                travellingDirection = Direction.LEFT;
                flipAnimationDirection();
            }
        } else if (travellingDirection == Direction.LEFT) {
            if (x > currentGround.getX()) {
                x--;
            } else {
                travellingDirection = Direction.RIGHT;
                flipAnimationDirection();
            }
        }
    }

    private void generateNewMode() {
        int choice = generateRandom(0, 6);

        switch (choice) {
            case 0:
                mode = Mode.WALK;
                modeTime = generateRandom(30);
                break;
            case 1:
                mode = Mode.THINK;
                modeTime = generateRandom(30);
                break;
            case 2:
                mode = Mode.WALK_TO_TAXI_PLATFORM;
                modeTime = -1;
                break;
            case 3:
                mode = Mode.WALK;
                modeTime = generateRandom(30);
                // TODO
//                mode = Mode.WALK_POD_STOP;
//                modeTime = -1;
                break;
            case 4:
                mode = Mode.STOP;
                modeTime = generateRandom(30);
                break;
        }

        timeElapsed = 0;
    }

    private void updateAnimationTimers(float delta) {
        animationTimer += delta;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Batch batch) {
        if (mode == Mode.WALK || mode == Mode.WALK_POD_STOP || mode == Mode.WALK_TO_TAXI_PLATFORM)
            travellerToRender = travellerWalkingAnimation.getKeyFrame(animationTimer);

        if (mode == Mode.THINK)
            batch.draw(thoughtBubble, Math.round(x), Math.round(y) + tileHeight);

        if (mode == Mode.CALL_TAXI || mode == Mode.TAXI_ON_ITS_WAY) {
            batch.draw(taxiBubble, Math.round(x), Math.round(y) + tileHeight);
        }
        batch.draw(travellerToRender, Math.round(x), Math.round(y));
    }

    public void setTravellerOnGround(Rectangle ground) {

        if (ground.getX() == 4576.00) {
            System.out.println("*** we found the special one. y: " + ground.getY());
        }
        this.currentGround = ground;
        int answer = generateRandom(0, 1);

        if (answer == 0) {
            travellingDirection = Direction.RIGHT;
            x = ground.getX();
            y = ground.getY();
        } else {
            flipAnimationDirection();
            travellingDirection = Direction.LEFT;
            x = ground.getX() + ground.getWidth() - tileWidth;
            y = ground.getY();
        }
    }

    private int generateRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    private int generateRandom(int max) {
        Random random = new Random();
        return random.nextInt(max - 1 + 1) + 1;
    }

    private void flipAnimationDirection() {
        TextureRegion[] keyFrames = travellerWalkingAnimation.getKeyFrames();
        for (int i = 0; i < keyFrames.length; i++) {
            keyFrames[i].flip(true, false);
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        modeTime = generateRandom(30);
    }

    public Rectangle getCurrentGround() {
        return currentGround;
    }

    public void setCurrentGround(Rectangle currentGround) {
        this.currentGround = currentGround;
    }

    public Rectangle getCurrentPlatform() {
        return currentPlatform;
    }

    public void setCurrentPlatform(Rectangle currentPlatform) {
        this.currentPlatform = currentPlatform;
    }
}
