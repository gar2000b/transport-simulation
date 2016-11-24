package com.mygdx.simulation.travellers.factory;

import com.mygdx.simulation.TransportSimulation;
import com.mygdx.simulation.travellers.Traveller;
import com.mygdx.simulation.travellers.TravellerA;
import com.mygdx.simulation.travellers.TravellerB;
import com.mygdx.simulation.travellers.TravellerC;

/**
 * Created by Digilogue on 24/11/2016.
 */
public class TravellerFactory {

    private static final String TRAVELLER_A = "TravellerA";
    private static final String TRAVELLER_B = "TravellerB";
    private static final String TRAVELLER_C = "TravellerC";

    public static Traveller getTraveller(String travellerType, TransportSimulation transportSimulation) {
        if (travellerType.equals(TRAVELLER_A))
            return new TravellerA(transportSimulation);
        if (travellerType.equals(TRAVELLER_B))
            return new TravellerB(transportSimulation);
        if (travellerType.equals(TRAVELLER_C))
            return new TravellerC(transportSimulation);

        return null;
    }
}
