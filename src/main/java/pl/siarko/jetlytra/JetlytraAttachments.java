package pl.siarko.jetlytra;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import pl.siarko.jetlytra.flight.FlightState;

import java.util.function.Supplier;

public class JetlytraAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Jetlytra.MODID);

    public static final Supplier<AttachmentType<FlightState>> FLIGHT_STATE = ATTACHMENTS.register(
            "flight_state",
            () -> AttachmentType.builder(() -> FlightState.JETPACK).build()
    );

    public static final Supplier<AttachmentType<Boolean>> THRUST_ACTIVE = ATTACHMENTS.register(
            "thrust_active",
            () -> AttachmentType.builder(() -> false).build()
    );

    public static final Supplier<AttachmentType<Integer>> FUEL_TICK = ATTACHMENTS.register(
            "fuel_tick",
            () -> AttachmentType.builder(() -> 0).build()
    );
}
