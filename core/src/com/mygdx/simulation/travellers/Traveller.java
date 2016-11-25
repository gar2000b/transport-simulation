package com.mygdx.simulation.travellers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.simulation.TransportSimulation;

import java.util.Random;

/**
 * Created by Digilogue on 23/11/2016.
 */
public class Traveller {

    static final float FRAME_DURATION = 0.1F;

    TextureRegion[][] travellerTextures;
    Animation travellerWalkingAnimation;
    TextureRegion travellerToRender;
    Texture thoughtBubble;

    Rectangle currentGround;

    private float animationTimer = 0;
    private float timeElapsed = 0;
    private float x = 300;
    private float y = 3333;

    int tileWidth;
    int tileHeight;
    private int modeTime = 0;

    public enum Direction {LEFT, RIGHT}

    public enum Mode {WALK, THINK, WALK_TO_TAXI_PLATFORM, WALK_POD_STOP, STOP, CALL_TAXI, CALL_POD}

    private Direction travellingDirection = Direction.RIGHT;

    private Mode mode = Mode.WALK;

    public Traveller(TransportSimulation transportSimulation) {
        thoughtBubble = transportSimulation.getAssetManager().get("thought-bubble.png",
                Texture.class);
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

        setPosition(x, y);
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
                mode = Mode.WALK;
                modeTime = generateRandom(30);
//                mode = Mode.WALK_TO_TAXI_PLATFORM;
//                modeTime = -1;
                break;
            case 3:
                mode = Mode.WALK;
                modeTime = generateRandom(30);
//                mode = Mode.WALK_POD_STOP;
//                modeTime = -1;
                break;
            case 4:
                mode = Mode.STOP;
                modeTime = generateRandom(30);
                break;
            case 5:
                mode = Mode.WALK;
                modeTime = generateRandom(30);
//                mode = Mode.CALL_TAXI;
//                modeTime = -1;
                break;
            case 6:
                mode = Mode.WALK;
                modeTime = generateRandom(30);
//                mode = Mode.CALL_POD;
//                modeTime = -1;
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
        if (mode == Mode.WALK || mode == Mode.WALK_POD_STOP || mode == Mode.WALK_TO_TAXI_PLATFORM) {
            travellerToRender = travellerWalkingAnimation.getKeyFrame(animationTimer);
        }

        if (mode == Mode.THINK) {
            batch.draw(thoughtBubble, Math.round(x), Math.round(y) + tileHeight);
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

}
