package mdteam.ait.network;

import io.wispforest.owo.ops.WorldOps;
import mdteam.ait.AITMod;
import mdteam.ait.compat.DependencyChecker;
import mdteam.ait.compat.immersive.PortalsHandler;
import mdteam.ait.core.AITSounds;
import mdteam.ait.core.item.TardisItemBuilder;
import mdteam.ait.registry.DesktopRegistry;
import mdteam.ait.registry.ExteriorRegistry;
import mdteam.ait.registry.ExteriorVariantRegistry;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisDesktopSchema;
import mdteam.ait.tardis.TardisExterior;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.handler.DoorHandler;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import mdteam.ait.tardis.sound.HumSound;
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
    public static final Identifier SEND_EXTERIOR_SCHEMA_UPDATE = new Identifier(AITMod.MOD_ID, "send_exterior_schema_update");
    public static final Identifier SEND_TARDIS_FALLING_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_falling_update");
    public static final Identifier SEND_TARDIS_OVERGROWN_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_overgrown_update");
    public static final Identifier SEND_TARDIS_CLOAKED_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_cloaked_update");
    public static final Identifier SEND_TARDIS_CRASHING_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_crashing_update");
    public static final Identifier SEND_TARDIS_HANDBRAKE_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_handbreak_update");
    public static final Identifier SEND_TARDIS_DOOR_LOCKED_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_door_locked_update");
    public static final Identifier SEND_TARDIS_GROUND_SEARCHING_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_ground_searching_update");
    public static final Identifier SEND_TARDIS_POS_INCREMENT_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_pos_increment_update");
    public static final Identifier SEND_UNLOCKED_INTERIORS = new Identifier(AITMod.MOD_ID, "send_unlocked_interiors");
    public static final Identifier SEND_TARDIS_FUEL_LEVEL = new Identifier(AITMod.MOD_ID, "send_tardis_fuel_level");
    public static final Identifier SEND_TARDIS_FLIGHT_TIME = new Identifier(AITMod.MOD_ID, "send_tardis_flight_time");
    public static final Identifier SEND_TARDIS_EXTERIOR_POSITION_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_exterior_position_update");
    public static final Identifier SEND_TARDIS_DESTINATION_POSITION_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_destination_position_update");
    public static final Identifier SEND_TARDIS_DESKTOP_SCHEMA = new Identifier(AITMod.MOD_ID, "send_tardis_desktop_schema");
    public static final Identifier SEND_TARDIS_DESKTOP_HUM = new Identifier(AITMod.MOD_ID, "send_tardis_desktop_hum");

    public static final Identifier SEND_TARDIS_AUTOLAND_STATE_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_autoland_state_update");
    public static final Identifier SEND_TARDIS_REFUELER_STATE_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_refueler_state_update");
    public static final Identifier SEND_TARDIS_HAIL_MARY_STATE_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_hail_mary_state_update");
    public static final Identifier SEND_TARDIS_ANTIGRAVS_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_antigravs_update");
    public static final Identifier SEND_TARDIS_CARTRIDGE_STATE_UPDATE = new Identifier(AITMod.MOD_ID, "send_tardis_cartridge_state_update");

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
            if(DependencyChecker.hasPortals()) {
                PortalsHandler.removePortals(tardis);
            }
            tardisExterior.setType(ExteriorRegistry.REGISTRY.get(exteriorIdentifier));
            if (variantChanged) {
                tardis.getExterior().setVariant(ExteriorVariantRegistry.REGISTRY.get(variantIdentifier));
            }
            if(DependencyChecker.hasPortals()) {
                PortalsHandler.removePortals(tardis);
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
            sendInitialTardisSync(player);
        }));
        ServerPlayNetworking.registerGlobalReceiver(ClientAITNetworkManager.SEND_REQUEST_TARDIS_CONSOLE_POS, ((server, player, handler, buf, responseSender) -> {
            Tardis tardis = ServerTardisManager.getInstance().getTardis(buf.readUuid());
            if (tardis == null || player == null) return;
            sendTardisConsoleBlockPosToPlayer(tardis, player, tardis.getDesktop().getConsolePos());
        }));
    }

    private static void __sendPacketToInteriorSubscribers(PacketByteBuf data, Identifier packetID) {
        if (!ServerTardisManager.getInstance().interior_subscribers.containsKey(data.readUuid())) return;
        for (UUID uuid : ServerTardisManager.getInstance().interior_subscribers.get(data.readUuid())) {
            ServerPlayerEntity player = TardisUtil.getServer().getPlayerManager().getPlayer(uuid);
            if (player == null) continue;
            ServerPlayNetworking.send(player, packetID, data);
        }
    }

    private static void __sendPacketToExteriorSubscribers(PacketByteBuf data, Identifier packetID) {
        if (!ServerTardisManager.getInstance().exterior_subscribers.containsKey(data.readUuid())) return;
        for (UUID uuid : ServerTardisManager.getInstance().exterior_subscribers.get(data.readUuid())) {
            ServerPlayerEntity player = TardisUtil.getServer().getPlayerManager().getPlayer(uuid);
            if (player == null) continue;
            ServerPlayNetworking.send(player, packetID, data);
        }
    }

    private static void __sendPacketToAllPlayers(PacketByteBuf data, Identifier packetID) {
        for (ServerPlayerEntity player : TardisUtil.getServer().getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, packetID, data);
        }
    }
    public static void sendTardisConsoleBlockPosToPlayer(Tardis tardis, ServerPlayerEntity player, BlockPos consolePos) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBlockPos(consolePos);
        ServerPlayNetworking.send(player, SEND_TARDIS_CONSOLE_BLOCK_POS, data);
    }
    public static void sendTardisConsoleBlockPosToSubscribers(Tardis tardis, BlockPos consolePos) {
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

    public static void sendInitialTardisSync(ServerPlayerEntity player) {
        PacketByteBuf data = PacketByteBufs.create();
        Collection<UUID> tardisUUIDs = ServerTardisManager.getInstance().getLookup().keySet();
        Map<UUID, Identifier> uuidToExteriorVariantSchema = new HashMap<>();
        Map<UUID, Identifier> uuidToExteriorSchema = new HashMap<>();
        data.writeCollection(tardisUUIDs, PacketByteBuf::writeUuid);
        data.writeMap(uuidToExteriorVariantSchema, PacketByteBuf::writeUuid, PacketByteBuf::writeIdentifier);
        data.writeMap(uuidToExteriorSchema, PacketByteBuf::writeUuid, PacketByteBuf::writeIdentifier);
        ServerPlayNetworking.send(player, SEND_INITIAL_TARDIS_SYNC, data);
    }

    public static void sendExteriorAnimationUpdateSetup(Tardis tardis, TardisTravel.State state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeInt(state.ordinal());
        data.writeUuid(tardis.getUuid());
        __sendPacketToExteriorSubscribers(data, SEND_EXTERIOR_ANIMATION_UPDATE_SETUP);
    }

    public static void sendSyncNewTardis(Tardis tardis) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeIdentifier(tardis.getExterior().getVariant().id());
        data.writeIdentifier(tardis.getExterior().getType().id());
        __sendPacketToAllPlayers(data, SEND_SYNC_NEW_TARDIS);
    }

    public static void sendTardisCorners(Tardis tardis, Corners corners) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        BlockPos firstBlockPos = corners.getFirst();
        BlockPos secondBlockPos = corners.getSecond();
        data.writeLong(firstBlockPos.asLong());
        data.writeLong(secondBlockPos.asLong());
        __sendPacketToAllPlayers(data, SEND_TARDIS_CORNERS);

    }
    public static void sendTardisSiegeModeUpdate(Tardis tardis, boolean state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(state);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_SIEGE_MODE_UPDATE);
    }

    public static void sendTardisTravelSpeedUpdate(Tardis tardis, int speed) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeInt(speed);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_TRAVEL_SPEED_UPDATE);
    }
    public static void sendTardisTravelStateUpdate(Tardis tardis, TardisTravel.State state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeInt(state.ordinal());
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_TRAVEL_STATE_UPDATE);
    }

    public static void sendTardisPoweredUpdate(Tardis tardis, boolean powered) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(powered);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_POWERED_UPDATE);
    }

    public static void sendTardisAlarmsUpdate(Tardis tardis, boolean alarms) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(alarms);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_ALARMS_UPDATE);
    }

    public static void sendTardisExteriorDoorStateUpdate(Tardis tardis, DoorHandler.DoorStateEnum state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeInt(state.ordinal());
        __sendPacketToExteriorSubscribers(data, SEND_TARDIS_EXTERIOR_DOOR_STATE_UPDATE);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_EXTERIOR_DOOR_STATE_UPDATE);
    }

    public static void sendExteriorSchemaUpdate(Tardis tardis, ExteriorVariantSchema exteriorVariantSchema, ExteriorSchema exteriorSchema) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeIdentifier(exteriorVariantSchema.id());
        data.writeIdentifier(exteriorSchema.id());
        __sendPacketToExteriorSubscribers(data, SEND_EXTERIOR_SCHEMA_UPDATE);
        __sendPacketToInteriorSubscribers(data, SEND_EXTERIOR_SCHEMA_UPDATE);
    }

    public static void sendTardisOvergrownUpdate(Tardis tardis, boolean overgrown) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(overgrown);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_OVERGROWN_UPDATE);
        __sendPacketToExteriorSubscribers(data, SEND_TARDIS_OVERGROWN_UPDATE);
    }

    public static void sendTardisCloakedUpdate(Tardis tardis, boolean cloaked) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(cloaked);
        __sendPacketToExteriorSubscribers(data, SEND_TARDIS_CLOAKED_UPDATE);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_CLOAKED_UPDATE);
    }

    public static void sendTardisFallingUpdate(Tardis tardis, boolean falling) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(falling);
        __sendPacketToExteriorSubscribers(data, SEND_TARDIS_FALLING_UPDATE);
    }

    public static void sendTardisCrashingUpdate(Tardis tardis, boolean is_crashing) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(is_crashing);
        __sendPacketToExteriorSubscribers(data, SEND_TARDIS_CRASHING_UPDATE);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_CRASHING_UPDATE);
    }

    public static void sendTardisHandbrakeUpdate(Tardis tardis, boolean handbreak) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(handbreak);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_HANDBRAKE_UPDATE);
    }

    public static void sendTardisDoorLockedUpdate(Tardis tardis, boolean locked) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(locked);
        __sendPacketToExteriorSubscribers(data, SEND_TARDIS_DOOR_LOCKED_UPDATE);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_DOOR_LOCKED_UPDATE);
    }

    public static void sendTardisGroundSearchingUpdate(Tardis tardis, boolean state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(state);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_GROUND_SEARCHING_UPDATE);
    }

    public static void sendTardisPosIncrementUpdate(Tardis tardis, int increment) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeInt(increment);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_POS_INCREMENT_UPDATE);
    }

    public static void sendTardisUnlockedInteriors(Tardis tardis, List<TardisDesktopSchema> interiors) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        List<Identifier> interiorUUIDs = interiors.stream().map(TardisDesktopSchema::id).toList();
        data.writeCollection(interiorUUIDs, PacketByteBuf::writeIdentifier);
        __sendPacketToInteriorSubscribers(data, SEND_UNLOCKED_INTERIORS);
    }

    public static void sendTardisFuelLevel(Tardis tardis, double fuelLevel) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeDouble(fuelLevel);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_FUEL_LEVEL);
    }

    public static void sendTardisFlightTime(Tardis tardis, int flightTime) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeInt(flightTime);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_FLIGHT_TIME);
    }

    public static void sendTardisExteriorPositionUpdate(Tardis tardis, AbsoluteBlockPos.Directed directed) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBlockPos(directed);
        data.writeInt(directed.getDirection().getId());
        data.writeString(directed.getDimension().getValue());
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_EXTERIOR_POSITION_UPDATE);
        __sendPacketToExteriorSubscribers(data, SEND_TARDIS_EXTERIOR_POSITION_UPDATE);
    }

    public static void setSendTardisDestinationPositionUpdate(Tardis tardis, AbsoluteBlockPos.Directed directed) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBlockPos(directed);
        data.writeInt(directed.getDirection().getId());
        data.writeString(directed.getDimension().getValue());
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_DESTINATION_POSITION_UPDATE);
    }

    public static void sendTardisDesktopHum(Tardis tardis, HumSound sound) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeIdentifier(sound.id());
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_DESKTOP_HUM);
    }

    public static void sendTardisDesktopSchema(Tardis tardis, TardisDesktopSchema schema) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeIdentifier(schema.id());
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_DESKTOP_SCHEMA);
    }

    public static void sendTardisAutolandStateUpdate(Tardis tardis, boolean state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(state);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_AUTOLAND_STATE_UPDATE);
    }

    public static void sendTardisHailMaryStateUpdate(Tardis tardis, boolean state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(state);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_HAIL_MARY_STATE_UPDATE);
    }

    public static void sendTardisAntigravsUpdate(Tardis tardis, boolean state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(state);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_ANTIGRAVS_UPDATE);
    }

    public static void sendTardisRefuelerStateUpdate(Tardis tardis, boolean state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(state);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_REFUELER_STATE_UPDATE);
    }

    public static void sendTardisCartridgeStateUpdate(Tardis tardis, boolean state) {
        PacketByteBuf data = PacketByteBufs.create();
        data.writeUuid(tardis.getUuid());
        data.writeBoolean(state);
        __sendPacketToInteriorSubscribers(data, SEND_TARDIS_CARTRIDGE_STATE_UPDATE);
    }
}
