package pl.siarko.jetlytra.client;

import pl.siarko.jetlytra.flight.FlightState;

public class ClientJetpackState {
    private static FlightState state = FlightState.JETPACK;

    public static FlightState getState() { return state; }
    public static void setState(FlightState s) { state = s; }
}
