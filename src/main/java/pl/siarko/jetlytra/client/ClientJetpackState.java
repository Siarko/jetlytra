package pl.siarko.jetlytra.client;

import pl.siarko.jetlytra.flight.FlightState;

// Holds the local client's view of jetpack state, updated by S2CSyncStatePacket
public class ClientJetpackState {
    private static FlightState previous = FlightState.JETPACK;
    private static FlightState state = FlightState.JETPACK;
    private static boolean thrustActive = false;

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

    public static boolean isThrustActive() {
        return thrustActive;
    }

    public static void setThrustActive(boolean active) {
        thrustActive = active;
    }

}
