package pl.siarko.jetlytra;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import pl.siarko.jetlytra.flight.FlightState;

public class JetlytraAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Jetlytra.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FlightState>> FLIGHT_STATE =
            ATTACHMENT_TYPES.register("flight_state", () ->
                    AttachmentType.builder(() -> FlightState.OFF).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> THRUST_ACTIVE =
            ATTACHMENT_TYPES.register("thrust_active", () ->
                    AttachmentType.builder(() -> false).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> FUEL_TICK_COUNTER =
            ATTACHMENT_TYPES.register("fuel_tick_counter", () ->
                    AttachmentType.builder(() -> 0).build());
}
