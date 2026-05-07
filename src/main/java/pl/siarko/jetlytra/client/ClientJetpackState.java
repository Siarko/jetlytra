package pl.siarko.jetlytra.client;

import pl.siarko.jetlytra.flight.FlightState;

// Holds the local client's view of jetpack state, updated by S2CSyncStatePacket
public class ClientJetpackState {
    private static FlightState previous = FlightState.OFF;
    private static FlightState state = FlightState.OFF;

    public static FlightState getState() {
        return state;
    }

    public static FlightState getPrevious() {
        return previous;
    }

    public static void setState(FlightState newState) {
        previous = state;
        state = newState;
    }

}
