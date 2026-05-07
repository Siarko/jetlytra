package pl.siarko.jetlytra.flight;

public enum FlightState {
    OFF,
    JETPACK,
    HOVERING,
    ELYTRA;

    public boolean isActive() {
        return this != OFF;
    }
}
