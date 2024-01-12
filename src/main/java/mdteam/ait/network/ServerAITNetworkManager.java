package mdteam.ait.network;

import io.wispforest.owo.ops.WorldOps;
import mdteam.ait.AITMod;
import mdteam.ait.client.registry.exterior.ClientExteriorVariantSchema;
import mdteam.ait.core.AITSounds;
import mdteam.ait.core.item.TardisItemBuilder;
import mdteam.ait.registry.DesktopRegistry;
import mdteam.ait.registry.ExteriorRegistry;
import mdteam.ait.registry.ExteriorVariantRegistry;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisDesktopSchema;
import mdteam.ait.tardis.TardisExterior;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.handler.DoorHandler;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.util.Corners;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.tardis.variant.exterior.ExteriorVariantSchema;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class ServerAITNetworkManager {
    public static final Identifier SEND_EXTERIOR_ANIMATION_UPDATE_SETUP = new Identifier(AITMod.MOD_ID, "send_exterior_animation_update_setup");
    public static final Identifier SEND_INITIAL_TARDIS_SYNC = new Identifier(AITMod.MOD_ID, "send_initial_tardis_sync");
    public static final Identifier SEND_SYNC_NEW_TARDIS = new Identifier(AITMod.MOD_ID, "send_sync_new_tardis");
    public static final Identifier SEND_TARDIS_CORNERS = new Identifier(AITMod.MOD_ID, "send_tardis_corners");
    public static final Identifier SEND_TARDIS_CONSOLE_BLOCK_POS = new Identifier(AITMod.MOD_ID, "send_tardis_console_block_pos");
    public static final Identifier SEND_TARDIS_SIEGE_MODE_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_siege_mode_update");
    public static final Identifier SEND_TARDIS_TRAVEL_SPEED_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_travel_speed_update");
    public static final Identifier SEND_TARDIS_TRAVEL_STATE_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_travel_state_update");
    public static final Identifier SEND_TARDIS_POWERED_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_powered_update");
    public static final Identifier SEND_TARDIS_ALARMS_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_alarms_update");
    public static final Identifier SEND_TARDIS_EXTERIOR_DOOR_STATE_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_exterior_door_state_update");

    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            ServerTardisManager.getInstance().removePlayerFromAllTardis(handler.getPlayer());
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_REQUEST_ADD_TO_EXTERIOR_SUBSCRIBERS, ((server, player, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            if (player == null) return;
            ServerTardisManager.getInstance().addExteriorSubscriberToTardis(player, uuid);
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_REQUEST_ADD_TO_INTERIOR_SUBSCRIBERS, ((server, player, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            if (player == null) return;
            ServerTardisManager.getInstance().addInteriorSubscriberToTardis(player, uuid);
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_EXTERIOR_UNLOADED, ((server, player, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            if (player == null) return;
            ServerTardisManager.getInstance().removeExteriorSubscriberToTardis(player, uuid);
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_INTERIOR_UNLOADED, ((server, player, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            if (player == null) return;
            ServerTardisManager.getInstance().removeInteriorSubscriberToTardis(player, uuid);
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_REQUEST_EXTERIOR_CHANGE_FROM_MONITOR, ((server, player, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            Identifier exteriorIdentifier = Identifier.tryParse(buf.readString());
            Identifier variantIdentifier = Identifier.tryParse(buf.readString());
            boolean variantChanged = buf.readBoolean();
            Tardis tardis = ServerTardisManager.getInstance().getTardis(uuid);
            TardisExterior tardisExterior = tardis.getExterior();
            tardisExterior.setType(ExteriorRegistry.REGISTRY.get(exteriorIdentifier));
            if (variantChanged) {
                tardis.getExterior().setVariant(ExteriorVariantRegistry.REGISTRY.get(variantIdentifier));
            }
            WorldOps.updateIfOnServer(TardisUtil.getServer().getWorld(tardis.getTravel().getPosition().getWorld().getRegistryKey()), tardis.getDoor().getExteriorPos());
            WorldOps.updateIfOnServer(TardisUtil.getServer().getWorld(TardisUtil.getTardisDimension().getRegistryKey()), tardis.getDoor().getDoorPos());
            if (tardis.isGrowth()) {
                tardis.getHandlers().getInteriorChanger().queueInteriorChange(TardisItemBuilder.findRandomDesktop(tardis));
            }

        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_SNAP_TO_OPEN_DOORS, ((server, player, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            if (player == null) return;
            Tardis tardis = ServerTardisManager.getInstance().getTardis(uuid);
            if (tardis.getHandlers().getOvergrownHandler().isOvergrown()) return;
            player.getWorld().playSound(null, player.getBlockPos(), AITSounds.SNAP, SoundCategory.PLAYERS, 4f, 1f);
            if ((player.squaredDistanceTo(tardis.getDoor().getExteriorPos().getX(), tardis.getDoor().getExteriorPos().getY(), tardis.getDoor().getExteriorPos().getZ())) <= 200 || TardisUtil.inBox(tardis.getDesktop().getCorners().getBox(), player.getBlockPos())) {
                if (!player.isSneaking()) {
                    if(!tardis.getDoor().locked()) {
                        if (tardis.getDoor().isOpen()) tardis.getDoor().closeDoors();
                        else tardis.getDoor().openDoors();
                    }
                } else {
                    DoorHandler.toggleLock(tardis, player);
                }
            }
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_REQUEST_FIND_PLAYER_FROM_MONITOR, ((server, player, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            UUID playerUUID = buf.readUuid();
            Tardis tardis = ServerTardisManager.getInstance().getTardis(tardisUUID);
            ServerPlayerEntity serverPlayer = TardisUtil.getServer().getPlayerManager().getPlayer(playerUUID);
            if (tardis.getDesktop().getCorners() == null || serverPlayer == null) return;
            tardis.getTravel().setDestination(new AbsoluteBlockPos.Directed(
                    serverPlayer.getBlockX(),
                            serverPlayer.getBlockY(),
                            serverPlayer.getBlockZ(),
                            serverPlayer.getWorld(),
                            serverPlayer.getMovementDirection()),
                    PropertiesHandler.getBool(tardis.getHandlers().getProperties(), PropertiesHandler.AUTO_LAND));
            TardisUtil.getTardisDimension().playSound(null, tardis.getDesktop().getConsolePos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 3f, 1f);
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_REQUEST_INTERIOR_CHANGE_FROM_MONITOR, ((server, player, handler, buf, responseSender) -> {
            Tardis tardis = ServerTardisManager.getInstance().getTardis(buf.readUuid());
            TardisDesktopSchema desktop = DesktopRegistry.get(buf.readIdentifier());
            if (tardis == null || desktop == null) return;
            tardis.getHandlers().getInteriorChanger().queueInteriorChange(desktop);
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_REQUEST_INITIAL_TARDIS_SYNC, ((server, player, handler, buf, responseSender) -> {
            setSendInitialTardisSync(player);
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_REQUEST_TARDIS_CORNERS, ((server, player, handler, buf, responseSender) -> {
            Tardis tardis = ServerTardisManager.getInstance().getTardis(buf.readUuid());
            if (tardis == null) return;
            Corners corners = tardis.getDesktop().getCorners();
            setSendTardisCorners(tardis.getUuid(), player.getUuid(), corners);
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_REQUEST_TARDIS_CONSOLE_POS, ((server, player, handler, buf, responseSender) -> {
            Tardis tardis = ServerTardisManager.getInstance().getTardis(buf.readUuid());
            BlockPos consolePos = tardis.getDesktop().getConsolePos();
            setSendTardisConsoleBlockPosToPlayer(player, consolePos, tardis);
        }));
    }

    public static void setSendTardisConsoleBlockPosToPlayer(ServerPlayerEntity player, BlockPos consolePos, Tardis tardis) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBlockPos(consolePos);
        ServerPlayNetworking.send(player, SEND_TARDIS_CONSOLE_BLOCK_POS, data);
    }
    public static void setSendTardisConsoleBlockPosToSubscribers(BlockPos consolePos, Tardis tardis) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBlockPos(consolePos);
        if (!ServerTardisManager.getInstance().interior_subscribers.containsKey(tardis.getUuid())) return;
        for (UUID uuid : ServerTardisManager.getInstance().interior_subscribers.get(tardis.getUuid())) {
            ServerPlayerEntity player = TardisUtil.getServer().getPlayerManager().getPlayer(uuid);
            if (player == null) continue;
            ServerPlayNetworking.send(player, SEND_TARDIS_CONSOLE_BLOCK_POS, data);
        }
    }

    public static void setSendInitialTardisSync(ServerPlayerEntity player) {
        PacketByteBuf data = PacketByteBufs.create();
        Collection<UUID> tardisUUIDs = ServerTardisManager.getInstance().getLookup().keySet();
        Map<UUID, Identifier> uuidToExteriorVariantSchema = new HashMap<>();
        Map<UUID, Identifier> uuidToExteriorSchema = new HashMap<>();
        data.writeCollection(tardisUUIDs, PacketByteBuf::writeUuid);
        data.writeMap(uuidToExteriorVariantSchema, PacketByteBuf::writeUuid, PacketByteBuf::writeIdentifier);
        data.writeMap(uuidToExteriorSchema, PacketByteBuf::writeUuid, PacketByteBuf::writeIdentifier);
        ServerPlayNetworking.send(player, SEND_INITIAL_TARDIS_SYNC, data);
    }

    public static void setSendExteriorAnimationUpdateSetup(UUID tardisUUID, TardisTravel.State state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeInt(state.ordinal());
        data.writeUuid(tardisUUID);
        sendPacketToExteriorSubscribers(data, SEND_EXTERIOR_ANIMATION_UPDATE_SETUP);
    }

    public static void setSendSyncNewTardis(Tardis tardis) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeIdentifier(tardis.getExterior().getVariant().id());
        data.writeIdentifier(tardis.getExterior().getType().id());
        sendPacketToAllPlayers(data, SEND_SYNC_NEW_TARDIS);
    }

    public static void setSendTardisCorners(UUID tardisUUID, UUID playerUUID, Corners corners) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardisUUID);
        BlockPos firstBlockPos = corners.getFirst();
        BlockPos secondBlockPos = corners.getSecond();
        data.writeLong(firstBlockPos.asLong());
        data.writeLong(secondBlockPos.asLong());
        ServerPlayNetworking.send(Objects.requireNonNull(TardisUtil.getServer().getPlayerManager().getPlayer(playerUUID)), SEND_TARDIS_CORNERS, data);

    }
    public static void setSendTardisSiegeModeUpdate(Tardis tardis, boolean state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(state);
        sendPacketToInteriorSubscribers(data, SEND_TARDIS_SIEGE_MODE_UPDATE);
    }

    public static void setSendTardisTravelSpeedUpdate(Tardis tardis, int speed) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeInt(speed);
        sendPacketToInteriorSubscribers(data, SEND_TARDIS_TRAVEL_SPEED_UPDATE);
    }
    public static void setSendTardisTravelStateUpdate(Tardis tardis, TardisTravel.State state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeInt(state.ordinal());
        sendPacketToInteriorSubscribers(data, SEND_TARDIS_TRAVEL_STATE_UPDATE);
    }

    public static void setSendTardisPoweredUpdate(Tardis tardis, boolean powered) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(powered);
        sendPacketToInteriorSubscribers(data, SEND_TARDIS_POWERED_UPDATE);
    }

    public static void setSendTardisAlarmsUpdate(Tardis tardis, boolean alarms) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(alarms);
        sendPacketToInteriorSubscribers(data, SEND_TARDIS_ALARMS_UPDATE);
    }

    public static void setSendTardisExteriorDoorStateUpdate(Tardis tardis, boolean state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(state);
        sendPacketToExteriorSubscribers(data, SEND_TARDIS_EXTERIOR_DOOR_STATE_UPDATE);
    }

    public static void sendPacketToInteriorSubscribers(PacketByteBuf data, Identifier packetID) {
        if (!ServerTardisManager.getInstance().interior_subscribers.containsKey(data.readUuid())) return;
        for (UUID uuid : ServerTardisManager.getInstance().interior_subscribers.get(data.readUuid())) {
            ServerPlayerEntity player = TardisUtil.getServer().getPlayerManager().getPlayer(uuid);
            if (player == null) continue;
            ServerPlayNetworking.send(player, packetID, data);
        }
    }

    public static void sendPacketToExteriorSubscribers(PacketByteBuf data, Identifier packetID) {
        if (!ServerTardisManager.getInstance().exterior_subscribers.containsKey(data.readUuid())) return;
        for (UUID uuid : ServerTardisManager.getInstance().exterior_subscribers.get(data.readUuid())) {
            ServerPlayerEntity player = TardisUtil.getServer().getPlayerManager().getPlayer(uuid);
            if (player == null) continue;
            ServerPlayNetworking.send(player, packetID, data);
        }
    }

    public static void sendPacketToAllPlayers(PacketByteBuf data, Identifier packetID) {
        for (ServerPlayerEntity player : TardisUtil.getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, packetID, data);
        }
    }
}
