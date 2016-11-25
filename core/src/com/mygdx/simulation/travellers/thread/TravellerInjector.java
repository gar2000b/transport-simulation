package com.mygdx.simulation.travellers.thread;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.mygdx.simulation.TransportSimulation;
import com.mygdx.simulation.World;
import com.mygdx.simulation.travellers.Traveller;
import com.mygdx.simulation.travellers.factory.TravellerFactory;

/**
 * Created by Digilogue on 24/11/2016.
 */
public class TravellerInjector extends Thread {

    private static final int INTERVAL = 500;

    private Array<Traveller> travellers;
    private Array<Rectangle> grounds;
    private Array<String> travellerTypes;
    private TransportSimulation transportSimulation;

    private int travellerIndex = 0;
    private World world;

    public TravellerInjector(Array<Rectangle> grounds, Array<Traveller> travellers, TransportSimulation
            transportSimulation, World world) {
        // grounds.shuffle();
        this.grounds = grounds;
        this.travellers = travellers;
        this.transportSimulation = transportSimulation;
        this.world = world;

        travellerTypes = new Array<String>();

        travellerTypes.add("TravellerA");
        travellerTypes.add("TravellerB");
        travellerTypes.add("TravellerC");
    }

    @Override
    public void run() {
        try {
            int totalCount = 0;
            for (int i = 0; i < 1; i++) {
                totalCount++;
                if (i == grounds.size - 1)
                    i = 0;
                Traveller traveller = TravellerFactory.getTraveller(getNextTravellerType(), transportSimulation, world);
                traveller.setTravellerOnGround(grounds.get(i));
                traveller.setMode(Traveller.Mode.WALK);
                travellers.add(traveller);
                if (totalCount == grounds.size * 3)
                    break;
                Thread.sleep(INTERVAL);
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getNextTravellerType() {
        String tt;
        if (travellerIndex < travellerTypes.size - 1) {
            tt = travellerTypes.get(travellerIndex);
            travellerIndex++;
        } else {
            tt = travellerTypes.get(travellerIndex);
            travellerIndex = 0;
        }
        return tt;
    }
}
