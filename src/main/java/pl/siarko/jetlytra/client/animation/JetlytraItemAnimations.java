package pl.siarko.jetlytra.client.animation;

import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.MathParser;
import pl.siarko.jetlytra.flight.FlightState;
import pl.siarko.jetlytra.flight.FuelData;
import pl.siarko.jetlytra.item.JetlytraItem;
import pl.siarko.jetlytra.item.JetlytraItems;

import java.util.Objects;

public class JetlytraItemAnimations {

    private static final RawAnimation INIT_STATE = RawAnimation.begin().then("init_state", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation FUEL_BAR = RawAnimation.begin().then("fuel_bar", Animation.LoopType.LOOP);
    private static final RawAnimation WINGS_OPEN_STATE = RawAnimation.begin().then("wings_open_state", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation WINGS_OUT = RawAnimation.begin().then("wings_out", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation WINGS_IN = RawAnimation.begin().then("wings_in", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation BOOST = RawAnimation.begin()
            .then("boost_activate", Animation.LoopType.PLAY_ONCE)
            .then("boost_active", Animation.LoopType.LOOP);

    public static AnimationController<JetlytraItem> wings(JetlytraItem item) {
        return new AnimationController<>(item, "wing_controller", 0, state -> {
            RawAnimation current = state.getController().getCurrentRawAnimation();
            boolean elytraActive = Objects.requireNonNull(state.getData(DataTickets.ITEMSTACK)).getOrDefault(
                    JetlytraItems.FLIGHT_STATE_COMPONENT,
                    FlightState.JETPACK
            ) == FlightState.ELYTRA;

            if (current == null) {
                return state.setAndContinue(elytraActive ? WINGS_OPEN_STATE : INIT_STATE);
            }
            if (elytraActive) {
                return state.setAndContinue(WINGS_OUT);
            }
            if (current == WINGS_OUT) {
                return state.setAndContinue(WINGS_IN);
            }
            return PlayState.CONTINUE;
        });
    }

    public static AnimationController<JetlytraItem> boost(JetlytraItem item) {
        return new AnimationController<>(item, "boost_controller", 0, state -> {
            boolean thrustActive = Objects.requireNonNull(state.getData(DataTickets.ITEMSTACK))
                    .getOrDefault(JetlytraItems.THRUST_ACTIVE_COMPONENT, false);
            return thrustActive ? state.setAndContinue(BOOST) : PlayState.STOP;
        });
    }

    public static AnimationController<JetlytraItem> fuelGauge(JetlytraItem item) {
        return new AnimationController<>(item, "fuel_gauge_controller", 0, state -> {
            FuelData fuel = state.getData(DataTickets.ITEMSTACK).get(JetlytraItems.FUEL_DATA);
            double scale = fuel != null ? fuel.count() / (double) FuelData.MAX_COUNT * 6.4 : 0.0;
            MathParser.setVariable("v.fuel_gauge_scale", () -> scale);
            return state.setAndContinue(FUEL_BAR);
        });
    }
}
