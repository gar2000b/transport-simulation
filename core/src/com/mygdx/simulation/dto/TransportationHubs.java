package com.mygdx.simulation.dto;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Digilogue on 24/11/2016.
 */
public class TransportationHubs {

    private Array<Rectangle> platforms = new Array<Rectangle>();
    private Array<Rectangle> podStops = new Array<Rectangle>();

    public Array<Rectangle> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Array<Rectangle> platforms) {
        this.platforms = platforms;
    }

    public Array<Rectangle> getPodStops() {
        return podStops;
    }

    public void setPodStops(Array<Rectangle> podStops) {
        this.podStops = podStops;
    }
}
