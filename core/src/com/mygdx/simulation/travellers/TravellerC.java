package com.mygdx.simulation.travellers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.simulation.TransportSimulation;
import com.mygdx.simulation.World;

/**
 * Created by Digilogue on 23/11/2016.
 */
public class TravellerC extends Traveller {
    /**
     * Constructor basically builds up the travellers animation sequence from it's sprite sheet / image.
     *
     * @param transportSimulation
     */
    public TravellerC(TransportSimulation transportSimulation, World world) {

        super(transportSimulation, world);
        tileWidth = 60;
        tileHeight = 88;

        TextureRegion traveller = new TextureRegion(transportSimulation.getAssetManager().get("traveller-c.png",
                Texture.class));
        travellerTextures = new TextureRegion(traveller).split(tileWidth, tileHeight);

        travellerWalkingAnimation = new Animation(FRAME_DURATION, travellerTextures[0][0], travellerTextures[0][1],
                travellerTextures[0][2], travellerTextures[0][3], travellerTextures[0][4], travellerTextures[0][5],
                travellerTextures[0][6], travellerTextures[0][7]);
        travellerWalkingAnimation.setPlayMode(Animation.PlayMode.LOOP);
    }
}
