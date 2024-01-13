package mdteam.ait.tardis.handler;

import mdteam.ait.core.item.KeyItem;
import mdteam.ait.network.ServerAITNetworkManager;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.UUID;

public class CloakHandler extends TardisLink {

    // @TODO its been a minute since ive had to server to client logic bullshit so duzo you do it while i do components
    /*private float alphaBasedOnDistance = 1.0F;*/

    public CloakHandler(UUID tardisId) {
        super(tardisId);
    }

    public void enable() {
        PropertiesHandler.setBool(getTardis().getHandlers().getProperties(), PropertiesHandler.IS_CLOAKED, true);
        ServerAITNetworkManager.sendTardisCloakedUpdate(getTardis(), true);
        getTardis().markDirty();
    }

    public void disable() {
        PropertiesHandler.setBool(getTardis().getHandlers().getProperties(), PropertiesHandler.IS_CLOAKED, false);
        ServerAITNetworkManager.sendTardisCloakedUpdate(getTardis(), false);
        getTardis().markDirty();
    }

    public boolean isEnabled() {
        return PropertiesHandler.getBool(getTardis().getHandlers().getProperties(), PropertiesHandler.IS_CLOAKED);
    }

    public void toggle() {
        if (isEnabled()) disable();
        else enable();
    }

    /*public float getAlphaBasedOnDistance() {
        return alphaBasedOnDistance;
    }

    public void setAlphaBasedOnDistance(float alphaBasedOnDistance) {
        this.alphaBasedOnDistance = alphaBasedOnDistance;
    }*/

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);

        if (this.isEnabled() && !getTardis().hasPower()) {
            this.disable();
            return;
        }

        if(this.getTardis().getExterior().getExteriorPos() == null) return;
        List<PlayerEntity> players = this.getTardis().getTravel().getPosition().getWorld().getEntitiesByClass(PlayerEntity.class,
                new Box(getTardis().getExterior().getExteriorPos()).expand(3), EntityPredicates.EXCEPT_SPECTATOR);
        for (PlayerEntity player : players) {
            ItemStack stack = KeyItem.getFirstKeyStackInInventory(player);
            if (stack != null && stack.getItem() instanceof KeyItem) {
                NbtCompound tag = stack.getOrCreateNbt();
                if (!tag.contains("tardis")) {
                    return;
                }
                if(UUID.fromString(tag.getString("tardis")).equals(this.getTardis().getUuid())) {
                    //this.setAlphaBasedOnDistance(0.45f);
                    return;
                }/* else {
                    this.setAlphaBasedOnDistance(0.105f);
                }*/
            }
        }

        if (!this.isEnabled()) return;

        this.getTardis().removeFuel(2); // idle drain of 2 fuel per tick
    }
}
