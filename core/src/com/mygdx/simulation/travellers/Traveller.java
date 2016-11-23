package com.mygdx.simulation.travellers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

/**
 * Created by Digilogue on 23/11/2016.
 */
public class Traveller {

    static final float FRAME_DURATION = 0.1F;

    TextureRegion[][] travellerTextures;
    Animation travellerWalkingAnimation;
    TextureRegion travellerToRender;

    Rectangle currentGround;

    private float animationTimer = 0;
    private float x = 300;
    private float y = 3333;

    int tileWidth;
    int tileHeight;

    private boolean walk = false;

    public enum Direction {LEFT, RIGHT}

    private TravellerA.Direction travellingDirection = TravellerA.Direction.RIGHT;

    public Traveller(){}

    public void update(float delta) {
        updateAnimationTimers(delta);

        if (isWalk()) {
            if (travellingDirection == TravellerA.Direction.RIGHT) {
                if (x < currentGround.getX() + (currentGround.getWidth() - tileWidth)) {
                    x++;
                } else {
                    setWalk(false);
                    travellerWalkingAnimation.setPlayMode(Animation.PlayMode.NORMAL);
                }
            } else if (travellingDirection == TravellerA.Direction.LEFT) {
                if (x > currentGround.getX()) {
                    x--;
                } else {
                    setWalk(false);
                    travellerWalkingAnimation.setPlayMode(Animation.PlayMode.NORMAL);
                }
            }
        }

        setPosition(x, y);
    }

    private void updateAnimationTimers(float delta) {
        animationTimer += delta;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Batch batch) {
        travellerToRender = travellerWalkingAnimation.getKeyFrame(animationTimer);
        batch.draw(travellerToRender, Math.round(x), Math.round(y));
    }

    public void setTravellerOnGround(Rectangle ground) {

        this.currentGround = ground;
        Random random = new Random();
        int answer = random.nextInt(1 - 0 + 1) + 0;
        answer = 0; // TODO - remove when done.

        if (answer == 0) {
            travellingDirection = TravellerA.Direction.RIGHT;
            x = ground.getX();
            y = ground.getY();
        } else {
            TextureRegion[] keyFrames = travellerWalkingAnimation.getKeyFrames();
            for (int i = 0; i < keyFrames.length; i++) {
                keyFrames[i].flip(true, false);
            }
            travellingDirection = TravellerA.Direction.LEFT;
            x = ground.getWidth() - tileWidth;
            y = ground.getY();
        }
    }

    public boolean isWalk() {
        return walk;
    }

    public void setWalk(boolean walk) {
        this.walk = walk;
    }
}
