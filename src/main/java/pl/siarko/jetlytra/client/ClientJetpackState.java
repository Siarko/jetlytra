package pl.siarko.jetlytra.client;

import pl.siarko.jetlytra.flight.FlightState;

public class ClientJetpackState {
    private static FlightState state = FlightState.JETPACK;
    private static boolean thrustActive = false;

    public static FlightState getState() { return state; }
    public static boolean isThrustActive() { return thrustActive; }
    public static void setState(FlightState s) { state = s; }
    public static void setThrustActive(boolean b) { thrustActive = b; }
}
