package loqor.ait.core;

import loqor.ait.AITMod;
import net.minecraft.component.DataComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class AITDataComponents {

    public static final DataComponentType<Float> AU_LEVEL = register("au_level", (builder) -> builder.codec(Codecs.POSITIVE_FLOAT).packetCodec(PacketCodecs.FLOAT));
    public static final DataComponentType<String> UUID_KEY = register("uuid", (builder) -> builder.codec(Codecs.ESCAPED_STRING).packetCodec(PacketCodecs.STRING));


    private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return (DataComponentType<T>) Registry.register(Registries.DATA_COMPONENT_TYPE, new Identifier(AITMod.MOD_ID, id), ((DataComponentType.Builder)builderOperator.apply(DataComponentType.builder())).build());
    }

    public static void init() {

    }
}
