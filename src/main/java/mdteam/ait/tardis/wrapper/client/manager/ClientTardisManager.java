package mdteam.ait.tardis.wrapper.client.manager;

import mdteam.ait.tardis.TardisDesktop;
import mdteam.ait.tardis.TardisDesktopSchema;
import mdteam.ait.tardis.wrapper.client.ClientTardis;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientTardisManager {

    private static final ClientTardisManager INSTANCE = new ClientTardisManager();

    public final HashMap<UUID, Supplier<ClientTardis>> LOOKUP = new HashMap<>();

    public ClientTardisManager() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            for (ClientTardis tardis : this.LOOKUP.values().stream().map(Supplier::get).toList()) {
                tardis.getLoadCache().reset();
            }
        });
    }

    public ClientTardis getClientTardisFromBlockPosition(BlockPos blockPos) {
        for (ClientTardis tardis : this.LOOKUP.values().stream().map(Supplier::get).toList()) {
            if (tardis.getExterior().getExteriorBlockPos().equals(blockPos)) {
                return tardis;
            }
        }
        return null;
    }

    public static ClientTardisManager getInstance() {
        return INSTANCE;
    }

    public void reset() {
        this.LOOKUP.clear();
    }

}
