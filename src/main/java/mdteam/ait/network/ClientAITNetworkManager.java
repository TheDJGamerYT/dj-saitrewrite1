package mdteam.ait.network;

import mdteam.ait.AITMod;
import mdteam.ait.client.registry.exterior.ClientExteriorVariantSchema;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.registry.ExteriorRegistry;
import mdteam.ait.registry.ExteriorVariantRegistry;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.handler.DoorHandler;
import mdteam.ait.tardis.util.Corners;
import mdteam.ait.tardis.variant.exterior.ExteriorVariantSchema;
import mdteam.ait.tardis.wrapper.client.ClientTardis;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

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
            ClientTardis clientTardis = ClientTardisManager.getInstance().LOOKUP.get(tardisUUID).get();
            clientTardis.getExterior().setDoorState(DoorHandler.DoorStateEnum.values()[buf.readInt()]);
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
