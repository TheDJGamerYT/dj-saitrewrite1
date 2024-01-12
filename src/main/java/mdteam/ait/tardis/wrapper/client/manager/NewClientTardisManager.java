package mdteam.ait.tardis.wrapper.client.manager;

import mdteam.ait.tardis.wrapper.client.ClientTardis;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class NewClientTardisManager {

    private static final NewClientTardisManager INSTANCE = new NewClientTardisManager();

    public final HashMap<UUID, Supplier<ClientTardis>> LOOKUP = new HashMap<>();

    public NewClientTardisManager() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            for (ClientTardis tardis : this.LOOKUP.values().stream().map(Supplier::get).toList()) {
                tardis.getLoadCache().reset();
            }
        });
    }

    public static NewClientTardisManager getInstance() {
        return INSTANCE;
    }

    public void reset() {
        this.LOOKUP.clear();
    }

}
