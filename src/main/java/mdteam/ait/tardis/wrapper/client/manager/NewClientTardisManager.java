package mdteam.ait.tardis.wrapper.client.manager;

import mdteam.ait.tardis.wrapper.client.ClientTardis;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

public class NewClientTardisManager {

    private static final NewClientTardisManager INSTANCE = new NewClientTardisManager();

    public final HashMap<UUID, Supplier<ClientTardis>> LOOKUP = new HashMap<>();

    public NewClientTardisManager() {

    }

    public static NewClientTardisManager getInstance() {
        return INSTANCE;
    }

    public void reset() {
        this.LOOKUP.clear();
    }

}
