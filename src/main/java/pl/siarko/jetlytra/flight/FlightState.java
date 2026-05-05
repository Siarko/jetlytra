package pl.siarko.jetlytra.flight;

public enum FlightState {
    OFF(false, false, false),
    JETPACK(true, false, false),
    HOVERING(false, true, false),
    ELYTRA(false, false, true);

    public final boolean allowsThrust;
    public final boolean cancelsGravity;
    public final boolean usesFallFlying;

    FlightState(boolean allowsThrust, boolean cancelsGravity, boolean usesFallFlying) {
        this.allowsThrust = allowsThrust;
        this.cancelsGravity = cancelsGravity;
        this.usesFallFlying = usesFallFlying;
    }

    public boolean isActive() {
        return this != OFF;
    }
}
