package com.mygdx.simulation.travellers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.simulation.TransportSimulation;

/**
 * Created by Digilogue on 21/11/2016.
 */
public class TravellerA extends Traveller {
    /**
     * Constructor basically builds up the travellers animation sequence from it's sprite sheet / image.
     *
     * @param transportSimulation
     */
    public TravellerA(TransportSimulation transportSimulation) {

        tileWidth = 82;
        tileHeight = 95;

        TextureRegion traveller = new TextureRegion(transportSimulation.getAssetManager().get("traveller-a.png",
                Texture.class));
        travellerTextures = new TextureRegion(traveller).split(tileWidth, tileHeight);

        travellerWalkingAnimation = new Animation(FRAME_DURATION, travellerTextures[5][7], travellerTextures[5][8],
                travellerTextures[5][9], travellerTextures[6][0], travellerTextures[6][1], travellerTextures[6][2],
                travellerTextures[6][3], travellerTextures[6][4]);
        travellerWalkingAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }
}
