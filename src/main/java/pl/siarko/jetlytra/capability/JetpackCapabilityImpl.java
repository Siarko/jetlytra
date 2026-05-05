package pl.siarko.jetlytra.capability;

import net.minecraft.nbt.CompoundTag;
import pl.siarko.jetlytra.flight.FlightState;

public class JetpackCapabilityImpl {

    private FlightState state = FlightState.OFF;
    private boolean thrustActive = false;

    public FlightState getState() {
        return state;
    }

    public void setState(FlightState state) {
        this.state = state;
    }

    public boolean isThrustActive() {
        return thrustActive;
    }

    public void setThrustActive(boolean thrustActive) {
        this.thrustActive = thrustActive;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("state", state.name());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        try {
            state = FlightState.valueOf(tag.getString("state"));
        } catch (IllegalArgumentException e) {
            state = FlightState.OFF;
        }
        thrustActive = false;
    }
}
