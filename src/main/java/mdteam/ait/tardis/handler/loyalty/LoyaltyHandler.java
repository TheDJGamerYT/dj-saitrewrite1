package mdteam.ait.tardis.handler.loyalty;

import mdteam.ait.tardis.AbstractTardisComponent;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.linkable.Linkable;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.HashMap;
import java.util.UUID;

public class LoyaltyHandler extends AbstractTardisComponent implements Linkable { // todo currently will be useless as will only be finished when all features have been added.
    private final HashMap<UUID, Loyalty> data;

    public LoyaltyHandler(Tardis tardis) {
        this(tardis, new HashMap<>());
    }

    public LoyaltyHandler(Tardis tardis, HashMap<UUID, Loyalty> data) {
        super(tardis);
        this.data = data;
    }

    public HashMap<UUID, Loyalty> data() {
        return this.data;
    }
    public void add(ServerPlayerEntity player) {
        this.add(player, Loyalty.NONE);
    }
    public void add(ServerPlayerEntity player, Loyalty loyalty) {
        this.data().put(player.getUuid(), loyalty);
    }
    public Loyalty get(ServerPlayerEntity player) {
        return this.data().get(player.getUuid());
    }

    @Override
    public void setTardis(Tardis tardis) {
        this.tardis = tardis;
    }
}
