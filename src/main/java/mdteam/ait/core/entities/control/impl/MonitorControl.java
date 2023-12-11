package mdteam.ait.core.entities.control.impl;

import mdteam.ait.client.AITModClient;
import mdteam.ait.core.entities.control.Control;
import mdteam.ait.tardis.Tardis;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class MonitorControl extends Control {
    public MonitorControl() {
        super("monitor");
    }

    @Override
    public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world) {
        if(tardis != null) AITModClient.openScreen(player, 0, tardis.getUuid());
        return true;
    }
}
