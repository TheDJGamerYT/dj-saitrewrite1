package mdteam.ait.core.entities.control.impl;

import mdteam.ait.core.entities.control.Control;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import mdteam.ait.tardis.Tardis;

public class DoorControl extends Control {
    public DoorControl() {
        super("door");
    }

    @Override
    public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world) {
        if(!player.isSneaking()) {
            tardis.getDoor().nextOrClosed(tardis.getExterior().getType().isDoubleDoor());
        } else {
            tardis.getDoor().setLocked(!tardis.getDoor().isLocked());
        }
        return true;
    }
}