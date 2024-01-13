package mdteam.ait.tardis.wrapper.client;

import mdteam.ait.client.util.ClientShakeUtil;
import mdteam.ait.client.util.ClientTardisUtil;
import mdteam.ait.core.blockentities.ConsoleBlockEntity;
import mdteam.ait.core.blockentities.DoorBlockEntity;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.network.ClientAITNetworkManager;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.handler.DoorHandler;
import mdteam.ait.tardis.util.Corners;
import mdteam.ait.tardis.variant.exterior.ExteriorVariantSchema;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClientTardis {
    private final UUID tardis_ID;
    private final ClientTardisTravel travel;

    private final ClientTardisDesktop desktop;
    private final ClientTardisLoadedCache load_cache;
    private final ClientTardisExterior exterior;

    private Corners corners;
    private boolean subscribed_to_interior = false;
    private boolean subscribe_to_exterior = false;
    private boolean siege_mode = false;
    private boolean powered = false;
    private boolean alarms_enabled = false;

    public ClientTardis(UUID tardisID, ExteriorVariantSchema exteriorVariantSchema, ExteriorSchema exteriorSchema) {
        this.tardis_ID = tardisID;
        this.travel = new ClientTardisTravel(this);
        this.desktop = new ClientTardisDesktop(this);
        this.load_cache = new ClientTardisLoadedCache(this);
        this.exterior = new ClientTardisExterior(this, exteriorVariantSchema, exteriorSchema);
    }

    public ClientTardisExterior getExterior() {
        return this.exterior;
    }

    public void setSiegeMode(boolean siege_mode) {
        this.siege_mode = siege_mode;
    }
    public void setPowered(boolean powered) {
        this.powered = powered;
    }
    public boolean isPowered() {
        return powered;
    }

    public boolean isAlarmsEnabled() {
        return alarms_enabled;
    }
    public void setAlarmsState(boolean alarms_enabled) {
        this.alarms_enabled = alarms_enabled;
    }

    public void tick() {
        ClientTardisUtil.tickPowerDelta();
        ClientTardisUtil.tickAlarmDelta();
        if (!ClientTardisUtil.isPlayerInATardis() || this.getTravel().state != TardisTravel.State.FLIGHT || !Objects.equals(ClientTardisUtil.getCurrentClientTardis(), this)) return;
        ClientShakeUtil.shakeFromConsole();
    }

    public boolean isInSiegeMode() {
        return this.siege_mode;
    }

    public ClientTardisTravel getTravel() {
        return travel;
    }

    public ClientTardisDesktop getDesktop() {
        return desktop;
    }

    public ClientTardisLoadedCache getLoadCache() {
        return load_cache;
    }

    public void subscribeToInterior() {
        ClientAITNetworkManager.ask_for_interior_subscriber(getTardisID());
        this.subscribed_to_interior = true;
    }

    public void subscribeToExterior() {
        ClientAITNetworkManager.ask_for_exterior_subscriber(getTardisID());
        this.subscribe_to_exterior = true;
    }

    public void unsubscribeToInterior() {
        ClientAITNetworkManager.send_interior_unloaded(getTardisID());
        this.subscribed_to_interior = false;
    }

    public void unsubscribeToExterior() {
        ClientAITNetworkManager.send_exterior_unloaded(getTardisID());
        this.subscribe_to_exterior = false;
    }

    public boolean isSubscribedToInterior() {
        return subscribed_to_interior;
    }

    public boolean isSubscribedToExterior() {
        return subscribe_to_exterior;
    }

    /**
     * Returns the Tardis ID.
     *
     * @return the Tardis ID
     */
    public UUID getTardisID() {
        return tardis_ID;
    }

    public class ClientTardisTravel {
        private final ClientTardis tardis;
        private int speed = 0;

        private TardisTravel.State state = TardisTravel.State.LANDED;

        public ClientTardisTravel(ClientTardis tardis) {
            this.tardis = tardis;
        }

        public ClientTardis getTardis() {
            return tardis;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public int getSpeed() {
            return speed;
        }

        public void setState(TardisTravel.State state) {
            this.state = state;
        }

        public TardisTravel.State getState() {
            return state;
        }
    }

    public class ClientTardisDesktop {

        private final ClientTardis tardis;

        private Corners corners = null;
        private BlockPos consolePos = null;

        public ClientTardisDesktop(ClientTardis tardis) {
            this.tardis = tardis;
        }

        public ClientTardis getTardis() {
            return tardis;
        }

        public void setCorners(Corners corners) {
            this.corners = corners;
        }

        public Corners getCorners() {
            return corners;
        }

        public void setConsolePos(BlockPos consolePos) {
            this.consolePos = consolePos;
        }

        public BlockPos getConsolePos() {
            return consolePos;
        }

        public boolean isCornersSynced() {
            return corners != null;
        }

        public boolean isConsolePosSynced() {
            return consolePos != null;
        }
    }

    public class ClientTardisLoadedCache {
        private final ClientTardis tardis;
        private final List<ConsoleBlockEntity> loadedConsoleBlockEntities = new ArrayList<>();
        private ExteriorBlockEntity loadedExteriorBlockEntity = null;
        private final List<DoorBlockEntity> loadedDoorBlockEntities = new ArrayList<>();

        public ClientTardisLoadedCache(ClientTardis tardis) {
            this.tardis = tardis;
        }

        public ClientTardis getTardis() {
            return tardis;
        }

        public void loadConsoleBlock(ConsoleBlockEntity consoleBlockEntity) {
            loadedConsoleBlockEntities.add(consoleBlockEntity);
        }
        public void loadExteriorBlock(ExteriorBlockEntity exteriorBlockEntity) {
            this.loadedExteriorBlockEntity = exteriorBlockEntity;
            this.getTardis().getExterior().setExteriorBlockPos(this.loadedExteriorBlockEntity.getPos());
        }
        public void loadDoorBlock(DoorBlockEntity doorBlockEntity) {
            loadedDoorBlockEntities.add(doorBlockEntity);
        }

        public void unloadConsoleBlock(ConsoleBlockEntity consoleBlockEntity) {
            if (!loadedConsoleBlockEntities.contains(consoleBlockEntity)) return;
            loadedConsoleBlockEntities.remove(consoleBlockEntity);
        }
        public void unloadExteriorBlock() {
            this.loadedExteriorBlockEntity = null;
            this.getTardis().getExterior().setExteriorBlockPos(null);
        }
        public void unloadDoorBlock(DoorBlockEntity doorBlockEntity) {
            if (!loadedDoorBlockEntities.contains(doorBlockEntity)) return;
            loadedDoorBlockEntities.remove(doorBlockEntity);
        }

        public boolean isConsoleBlockLoaded(ConsoleBlockEntity consoleBlockEntity) {
            return loadedConsoleBlockEntities.contains(consoleBlockEntity);
        }

        public boolean isExteriorBlockLoaded(ExteriorBlockEntity exteriorBlockEntity) {
            return this.loadedExteriorBlockEntity != null;
        }

        public boolean isDoorBlockLoaded(DoorBlockEntity doorBlockEntity) {
            return loadedDoorBlockEntities.contains(doorBlockEntity);
        }

        public boolean hasAnyConsolesLoaded() {
            return !loadedConsoleBlockEntities.isEmpty();
        }


        public boolean hasAnyDoorsLoaded() {
            return !loadedDoorBlockEntities.isEmpty();
        }

        public void reset() {
            loadedConsoleBlockEntities.clear();
            this.loadedExteriorBlockEntity = null;
            loadedDoorBlockEntities.clear();
        }
    }

    public class ClientTardisExterior {
        private final ClientTardis tardis;
        private BlockPos exteriorBlockPos = null;
        private DoorHandler.DoorStateEnum doorState = DoorHandler.DoorStateEnum.CLOSED;
        private ExteriorVariantSchema exterior_variant_schema;
        private ExteriorSchema exterior_schema;

        public ClientTardisExterior(ClientTardis tardis, ExteriorVariantSchema exterior_variant_schema, ExteriorSchema exterior_schema) {
            this.tardis = tardis;
            this.exterior_variant_schema = exterior_variant_schema;
            this.exterior_schema = exterior_schema;
        }

        public ClientTardis getTardis() {
            return tardis;
        }

        public void setExteriorBlockPos(BlockPos exteriorBlockPos) {
            this.exteriorBlockPos = exteriorBlockPos;
        }

        public ExteriorVariantSchema getExteriorVariantSchema() {
            return exterior_variant_schema;
        }

        public ExteriorSchema getExteriorSchema() {
            return exterior_schema;
        }

        public void setExteriorVariantSchema(ExteriorVariantSchema exteriorVariantSchema) {
            this.exterior_variant_schema = exteriorVariantSchema;
        }

        public void setExteriorSchema(ExteriorSchema exteriorSchema) {
            this.exterior_schema = exteriorSchema;
        }

        public BlockPos getExteriorBlockPos() {
            return exteriorBlockPos;
        }

        public boolean isDoorOpen() {
            return doorState != DoorHandler.DoorStateEnum.CLOSED;
        }
        public boolean isLeftDoorOpen() {
            return doorState == DoorHandler.DoorStateEnum.FIRST || doorState == DoorHandler.DoorStateEnum.BOTH;
        }

        public boolean isRightDoorOpen() {
            return doorState == DoorHandler.DoorStateEnum.SECOND || doorState == DoorHandler.DoorStateEnum.BOTH;
        }
        public boolean isBothDoorsOpen() {
            return doorState == DoorHandler.DoorStateEnum.BOTH;
        }

        public void setDoorState(DoorHandler.DoorStateEnum state) {
            this.doorState = state;
        }

        public DoorHandler.DoorStateEnum getDoorState() {
            return doorState;
        }
    }
}