package pl.siarko.jetlytra.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.util.GeckoLibUtil;
import pl.siarko.jetlytra.flight.FuelData;

public class JetpackBlockEntity extends JetpackBlockEntityBase implements GeoBlockEntity {

    private static final String VARIABLE_FUEL_GAUGE_SCALE = "v.fuel_gauge_scale";

    private static final RawAnimation INIT_STATE = RawAnimation.begin().then("init_state", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation FUEL_BAR = RawAnimation.begin().then("fuel_bar", Animation.LoopType.LOOP);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public JetpackBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "wing_controller",
                0,
                state -> state.setAndContinue(INIT_STATE))
        );

        controllers.add(new AnimationController<>(
                this,
                "fuel_gauge_controller",
                0,
                state -> {
                    FuelData fuelData = getFuelData();
                    double scale = fuelData != null ? fuelData.count() / (double) FuelData.MAX_COUNT * 6.4 : 0.0;
                    MathParser.setVariable(VARIABLE_FUEL_GAUGE_SCALE, () -> scale);
                    return state.setAndContinue(FUEL_BAR);
                }
            )
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
