package mdteam.ait.network;

import mdteam.ait.AITMod;
import mdteam.ait.client.registry.exterior.ClientExteriorVariantSchema;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.registry.DesktopRegistry;
import mdteam.ait.registry.ExteriorRegistry;
import mdteam.ait.registry.ExteriorVariantRegistry;
import mdteam.ait.registry.HumsRegistry;
import mdteam.ait.tardis.TardisDesktopSchema;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.handler.DoorHandler;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.util.Corners;
import mdteam.ait.tardis.util.SerialDimension;
import mdteam.ait.tardis.variant.exterior.ExteriorVariantSchema;
import mdteam.ait.tardis.wrapper.client.ClientTardis;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.io.Serial;
import java.util.*;

public class ClientAITNetworkManager {
    public static final Identifier SEND_REQUEST_ADD_TO_INTERIOR_SUBSCRIBERS = new Identifier(AITMod.MOD_ID, "send_request_add_to_interior_subscribers");
    public static final Identifier SEND_REQUEST_ADD_TO_EXTERIOR_SUBSCRIBERS = new Identifier(AITMod.MOD_ID, "send_request_add_to_exterior_subscribers");
    public static final Identifier SEND_EXTERIOR_UNLOADED = new Identifier(AITMod.MOD_ID, "send_exterior_unloaded");
    public static final Identifier SEND_INTERIOR_UNLOADED = new Identifier(AITMod.MOD_ID, "send_interior_unloaded");
    public static final Identifier SEND_REQUEST_EXTERIOR_CHANGE_FROM_MONITOR = new Identifier(AITMod.MOD_ID, "send_request_exterior_change_from_monitor");
    public static final Identifier SEND_SNAP_TO_OPEN_DOORS = new Identifier(AITMod.MOD_ID, "send_snap_to_open_doors");
    public static final Identifier SEND_REQUEST_FIND_PLAYER_FROM_MONITOR = new Identifier(AITMod.MOD_ID, "send_request_find_player_from_monitor");
    public static final Identifier SEND_REQUEST_INTERIOR_CHANGE_FROM_MONITOR = new Identifier(AITMod.MOD_ID, "send_request_interior_change_from_monitor");
    public static final Identifier SEND_REQUEST_INITIAL_TARDIS_SYNC = new Identifier(AITMod.MOD_ID, "send_request_initial_tardis_sync");
    public static final Identifier SEND_REQUEST_TARDIS_CORNERS = new Identifier(AITMod.MOD_ID, "send_request_tardis_corners");
    public static final Identifier SEND_REQUEST_TARDIS_CONSOLE_POS = new Identifier(AITMod.MOD_ID, "send_request_tardis_console_pos");

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> send_request_initial_tardis_sync()));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_EXTERIOR_ANIMATION_UPDATE_SETUP, ((client, handler, buf, responseSender) -> {
            int p = buf.readInt();
            UUID uuid = buf.readUuid();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(uuid).get();
            ClientWorld clientWorld = MinecraftClient.getInstance().world;
            if (clientTardis == null || clientWorld == null) return;
            if (!(clientWorld.getBlockEntity(clientTardis.getExterior().getExteriorBlockPos()) instanceof ExteriorBlockEntity exteriorBlockEntity)) return;
            // @TODO: replace getExteriorBlockPos
            exteriorBlockEntity.getAnimation().setupAnimation(TardisTravel.State.values()[p]);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_INITIAL_TARDIS_SYNC, ((client, handler, buf, responseSender) -> {
            Collection<UUID> tardisUUIDs = buf.readCollection(ArrayList::new, PacketByteBuf::readUuid);
            Map<UUID, Identifier> uuidToExteriorVariantSchema = buf.readMap(PacketByteBuf::readUuid, PacketByteBuf::readIdentifier);
            Map<UUID, Identifier> uuidToExteriorSchema = buf.readMap(PacketByteBuf::readUuid, PacketByteBuf::readIdentifier);
            for (UUID uuid : tardisUUIDs) {
                ClientTardis clientTardis = new ClientTardis(uuid, ExteriorVariantRegistry.REGISTRY.get(uuidToExteriorVariantSchema.get(uuid)), ExteriorRegistry.REGISTRY.get(uuidToExteriorSchema.get(uuid)));
                ClientTardisManager.getInstance().LOOKUP.put(uuid, () -> clientTardis);
            }
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_CORNERS, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            BlockPos firstBlockPos = BlockPos.fromLong(buf.readLong());
            BlockPos secondBlockPos = BlockPos.fromLong(buf.readLong());
            Corners corners = new Corners(firstBlockPos, secondBlockPos);
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getDesktop().setCorners(corners);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_CONSOLE_BLOCK_POS, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            BlockPos consoleBlockPos = buf.readBlockPos();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getDesktop().setConsolePos(consoleBlockPos);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_SIEGE_MODE_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean siegeMode = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setSiegeMode(siegeMode);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_TRAVEL_SPEED_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            int speed = buf.readInt();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getTravel().setSpeed(speed);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_TRAVEL_STATE_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            TardisTravel.State state = TardisTravel.State.values()[buf.readInt()];
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getTravel().setState(state);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_POWERED_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean powered = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setPowered(powered);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_ALARMS_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean alarms = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setAlarmsState(alarms);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_EXTERIOR_DOOR_STATE_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            int doorState = buf.readInt();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getExterior().setDoorState(DoorHandler.DoorStateEnum.values()[doorState]);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_EXTERIOR_SCHEMA_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            ExteriorVariantSchema exteriorVariantSchema = ExteriorVariantRegistry.REGISTRY.get(buf.readIdentifier());
            ExteriorSchema exteriorSchema = ExteriorRegistry.REGISTRY.get(buf.readIdentifier());
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getExterior().setExteriorSchema(exteriorSchema);
            clientTardis.getExterior().setExteriorVariantSchema(exteriorVariantSchema);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_OVERGROWN_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean overgrown = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getExterior().setOvergrown(overgrown);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_CLOAKED_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean cloaked = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getExterior().setCloakedState(cloaked);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_FALLING_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean falling = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setFalling(falling);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_CRASHING_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean crashing = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setCrashingState(crashing);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_DOOR_LOCKED_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean locked = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getExterior().setDoorLocked(locked);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_POS_INCREMENT_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            int increment = buf.readInt();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getTravel().setIncrement(increment);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_UNLOCKED_INTERIORS, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            List<Identifier> interiorUUIDS = buf.readCollection(ArrayList::new, PacketByteBuf::readIdentifier);
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            List<TardisDesktopSchema> unlocked_desktops = new ArrayList<>();
            for (Identifier id : interiorUUIDS) {
                TardisDesktopSchema tardisDesktopSchema = DesktopRegistry.get(id);
                if (tardisDesktopSchema != null) {
                    unlocked_desktops.add(tardisDesktopSchema);
                }
            }
            clientTardis.setUnlockedDesktops(unlocked_desktops);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_FUEL_LEVEL, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            double fuel_level = buf.readDouble();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setFuel(fuel_level);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_FLIGHT_TIME, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            int flight_time = buf.readInt();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getTravel().setFlightTime(flight_time);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_EXTERIOR_POSITION_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            BlockPos pos = buf.readBlockPos();
            Direction direction = Direction.byId(buf.readInt());
            String dimension_name = buf.readString();
            if (ClientTardisManager.getInstance().LOOKUP.get(tardisUUID) == null) return;
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            if (clientTardis == null) return;
            AbsoluteBlockPos.Directed absoluteBlockPos = new AbsoluteBlockPos.Directed(new AbsoluteBlockPos(pos, (SerialDimension) null), direction);
            clientTardis.getExterior().setExteriorBlockPos(pos);
            clientTardis.getTravel().setPosition(absoluteBlockPos);
            clientTardis.getExterior().setPositionDimensionValue(dimension_name);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_DESTINATION_POSITION_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            BlockPos pos = buf.readBlockPos();
            Direction direction = Direction.byId(buf.readInt());
            String dimension_name = buf.readString();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            AbsoluteBlockPos.Directed absoluteBlockPos = new AbsoluteBlockPos.Directed((AbsoluteBlockPos) pos, direction);
            clientTardis.getTravel().setDestination(absoluteBlockPos);
            clientTardis.getExterior().setDestinationDimensionValue(dimension_name);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_DESKTOP_HUM, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            Identifier hum_id = buf.readIdentifier();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getDesktop().setHumSound(HumsRegistry.REGISTRY.get(hum_id));
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_DESKTOP_SCHEMA, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            Identifier schema_id = buf.readIdentifier();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getDesktop().setDesktopSchema(DesktopRegistry.get(schema_id));
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_AUTOLAND_STATE_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean autoland_state = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setAutoLandActive(autoland_state);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_HAIL_MARY_STATE_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean hail_mary_state = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setHailMaryActive(hail_mary_state);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_ANTIGRAVS_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean antigravs = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setAntiGravsActive(antigravs);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_CARTRIDGE_STATE_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean carriage_state = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setHasCartridge(carriage_state);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_TARDIS_REFUELER_STATE_UPDATE, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            boolean refueller_state = buf.readBoolean();
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.setIsRefueling(refueller_state);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ServerAITNetworkManager.SEND_SYNC_NEW_TARDIS, ((client, handler, buf, responseSender) -> {
            UUID tardisUUID = buf.readUuid();
            ExteriorVariantSchema exteriorVariantSchema = ExteriorVariantRegistry.REGISTRY.get(buf.readIdentifier());
            ExteriorSchema exteriorSchema = ExteriorRegistry.REGISTRY.get(buf.readIdentifier());
            ClientTardis clientTardis = new ClientTardis(tardisUUID, exteriorVariantSchema, exteriorSchema);
            ClientTardisManager.getInstance().LOOKUP.put(tardisUUID, () -> clientTardis);
        }));
    }

    public static void send_request_interior_change_from_monitor(UUID uuid, Identifier selected_interior) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        buf.writeIdentifier(selected_interior);
        ClientPlayNetworking.send(SEND_REQUEST_INTERIOR_CHANGE_FROM_MONITOR, buf);
    }

    public static void ask_for_interior_subscriber(UUID uuid) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);

        ClientPlayNetworking.send(SEND_REQUEST_ADD_TO_INTERIOR_SUBSCRIBERS, buf);
        ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(uuid).get();
        if (clientTardis == null) return;
        send_request_tardis_console_pos(uuid); // Request new console pos every time the TARDIS is loaded as we don't know if the console pos has changed
        if (clientTardis.getDesktop().isCornersSynced()) return;
        send_request_tardis_corners(uuid);

    }

    public static void ask_for_exterior_subscriber(UUID uuid) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);

        ClientPlayNetworking.send(SEND_REQUEST_ADD_TO_EXTERIOR_SUBSCRIBERS, buf);
    }

    public static void send_exterior_unloaded(UUID uuid) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);

        ClientPlayNetworking.send(SEND_EXTERIOR_UNLOADED, buf);
    }

    public static void send_interior_unloaded(UUID uuid) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);

        ClientPlayNetworking.send(SEND_INTERIOR_UNLOADED, buf);
    }

    public static void send_request_exterior_change_from_monitor(UUID uuid, ExteriorSchema exteriorSchema, ClientExteriorVariantSchema clientExteriorVariantSchema, boolean variantChange) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        buf.writeString(exteriorSchema.id().toString());
        buf.writeString(clientExteriorVariantSchema.id().toString());
        buf.writeBoolean(variantChange);
        ClientPlayNetworking.send(SEND_REQUEST_EXTERIOR_CHANGE_FROM_MONITOR, buf);
    }

    public static void send_snap_to_open_doors(UUID uuid) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);

        ClientPlayNetworking.send(SEND_SNAP_TO_OPEN_DOORS, buf);
    }

    public static void send_request_find_player_from_monitor(UUID uuid, UUID playerUUID) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        buf.writeUuid(playerUUID);
    }
    public static void send_request_initial_tardis_sync() {
        PacketByteBuf buf = PacketByteBufs.create(); // Empty packet
        ClientPlayNetworking.send(SEND_REQUEST_INITIAL_TARDIS_SYNC, buf);
    }
    public static void send_request_tardis_corners(UUID uuid) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        ClientPlayNetworking.send(SEND_REQUEST_TARDIS_CORNERS, buf);
    }

    public static void send_request_tardis_console_pos(UUID uuid) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        ClientPlayNetworking.send(SEND_REQUEST_TARDIS_CONSOLE_POS, buf);
    }

}
