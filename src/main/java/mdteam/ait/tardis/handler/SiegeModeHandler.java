package mdteam.ait.tardis.handler;

import mdteam.ait.core.AITSounds;
import mdteam.ait.network.ServerAITNetworkManager;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import mdteam.ait.tardis.util.TardisUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

import java.util.UUID;

public class SiegeModeHandler extends TardisLink {
    public SiegeModeHandler(UUID tardisId) {
        super(tardisId);
    }

    public boolean isSiegeMode() {
        return PropertiesHandler.getBool(getTardis().getHandlers().getProperties(), PropertiesHandler.SIEGE_MODE);
    }
    public boolean isSiegeBeingHeld() {
        return PropertiesHandler.get(getTardis().getHandlers().getProperties(), PropertiesHandler.SIEGE_HELD) != null;
    }
    public UUID getHeldPlayerUUID() {
        if (!isSiegeBeingHeld()) return null;

        return (UUID) PropertiesHandler.get(getTardis().getHandlers().getProperties(), PropertiesHandler.SIEGE_HELD);
    }
    public ServerPlayerEntity getHeldPlayer() {
        if (isClient()) return null;

        return TardisUtil.getServer().getPlayerManager().getPlayer(getHeldPlayerUUID());
    }
    public void setSiegeBeingHeld(UUID playerId) {
        if (playerId != null) getTardis().getHandlers().getAlarms().enable();

        PropertiesHandler.set(getTardis().getHandlers().getProperties(), PropertiesHandler.SIEGE_HELD, playerId);
        getTardis().markDirty();
    }
    public int getTimeInSiegeMode() {
        return PropertiesHandler.getInt(getTardis().getHandlers().getProperties(), PropertiesHandler.SIEGE_TIME);
    }
    // todo this is getting bloateed, merge some if statements together
    public void setSiegeMode(boolean b) {
        if (getTardis().getFuel() <= (0.01 * FuelHandler.TARDIS_MAX_FUEL)) return; // The required amount of fuel to enable/disable siege mode
        if (b) getTardis().disablePower();
        if (!b) getTardis().getHandlers().getAlarms().disable();
        if (getTardis().isSiegeBeingHeld()) return;
        if (!b && getTardis().getExterior().findExteriorBlock().isEmpty())
            getTardis().getTravel().placeExterior();
        if (b) TardisUtil.giveEffectToInteriorPlayers(getTardis(), new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0 , false, false));
        if (b) TardisUtil.getTardisDimension().playSound(null, getTardis().getDesktop().getConsolePos(), AITSounds.SIEGE_ENABLE, SoundCategory.BLOCKS, 3f, 1f);
        if (!b) TardisUtil.getTardisDimension().playSound(null, getTardis().getDesktop().getConsolePos(), AITSounds.SIEGE_DISABLE, SoundCategory.BLOCKS, 3f, 1f);

        getTardis().removeFuel(0.01 * FuelHandler.TARDIS_MAX_FUEL);

        PropertiesHandler.setBool(getTardis().getHandlers().getProperties(), PropertiesHandler.SIEGE_MODE, b);
        // Loqor is stinky
        getTardis().markDirty();
        ServerAITNetworkManager.setSendTardisSiegeModeUpdate(getTardis(), b);
    }

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);
        if (getTardis().getExterior().findExteriorBlock().isPresent()) {
            getTardis().setSiegeBeingHeld(null);
        }

        int siegeTime = getTardis().getTimeInSiegeMode() + 1;
        PropertiesHandler.set(getTardis().getHandlers().getProperties(), PropertiesHandler.SIEGE_TIME, getTardis().isSiegeMode() ? siegeTime : 0);
        // this.markDirty(); // DO NOT UNCOMMENT THAT LAG GOES CRAZYYYY!!!

        // todo add more downsides the longer you are in siege mode as it is meant to fail systems and kill you and that
        // for example, this starts to freeze the player (like we see in the episode) after a minute (change the length if too short) and only if its on the ground, to stop people from just slaughtering lol
        if (getTardis().getTimeInSiegeMode() > (60 * 20) && !getTardis().isSiegeBeingHeld()) {
            for (PlayerEntity player : TardisUtil.getPlayersInInterior(getTardis())) {
                if (!player.isAlive()) continue;
                if (player.getFrozenTicks() < player.getMinFreezeDamageTicks()) player.setFrozenTicks(player.getMinFreezeDamageTicks());
                player.setFrozenTicks(player.getFrozenTicks() + 2);
            }
        } else {
            for (PlayerEntity player : TardisUtil.getPlayersInInterior(getTardis())) {
                // something tells meee this will cause laggg
                if (player.getFrozenTicks() > player.getMinFreezeDamageTicks())
                    player.setFrozenTicks(0);
            }
        }
    }
}
