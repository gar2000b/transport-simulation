package com.mygdx.simulation.travellers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.simulation.TransportSimulation;

/**
 * Created by Digilogue on 21/11/2016.
 */
public class TravellerA {

    private static final int TILE_WIDTH = 82;
    private static final int TILE_HEIGHT = 95;
    private static final float FRAME_DURATION = 0.1F;

    TextureRegion[][] travellerTextures;
    Animation travellerWalkingAnimation;
    TextureRegion travellerToRender;

    private float animationTimer = 0;
    private float x = 300;
    private float y = 3333;

    public TravellerA(TransportSimulation transportSimulation) {

        TextureRegion traveller = new TextureRegion(transportSimulation.getAssetManager().get("traveller-a.png",
                Texture
                        .class));
        travellerTextures = new TextureRegion(traveller).split(TILE_WIDTH,
                TILE_HEIGHT);
        travellerWalkingAnimation = new Animation(FRAME_DURATION, travellerTextures[5][7], travellerTextures[5][8],
                travellerTextures[5][9], travellerTextures[6][0], travellerTextures[6][1], travellerTextures[6][2],
                travellerTextures[6][3], travellerTextures[6][4]);
        travellerWalkingAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }

    public void update(float delta) {
        updateAnimationTimers(delta);
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
}
