package pl.siarko.jetlytra.flight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import pl.siarko.jetlytra.Jetlytra;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class FuelTypeRegistry extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();

    private static volatile Map<ResourceLocation, FuelTypeDefinition> definitions = Map.of();

    public FuelTypeRegistry() {
        super(GSON, "fuel_types");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, FuelTypeDefinition> newDefs = new HashMap<>();
        DynamicOps<JsonElement> ops = makeConditionalOps();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            FuelTypeDefinition.CONDITIONAL_CODEC.parse(ops, entry.getValue())
                    .resultOrPartial(err -> Jetlytra.LOGGER.error("Failed to parse fuel type {}: {}", entry.getKey(), err))
                    .flatMap(Function.identity())
                    .ifPresent(wc -> newDefs.put(entry.getKey(), wc.carrier()));
        }
        definitions = Map.copyOf(newDefs);
    }

    public static void applyClientSync(Map<ResourceLocation, FuelTypeDefinition> map) {
        definitions = Map.copyOf(map);
    }

    public static Map<ResourceLocation, FuelTypeDefinition> getDefinitions() {
        return definitions;
    }

    public static Optional<FuelTypeDefinition> byId(ResourceLocation id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public static Optional<ResourceLocation> idFromItem(Item item) {
        return definitions.entrySet().stream()
                .filter(e -> e.getValue().item() == item)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public static Optional<FuelTypeDefinition> fromItem(Item item) {
        return definitions.values().stream()
                .filter(d -> d.item() == item)
                .findFirst();
    }
}
