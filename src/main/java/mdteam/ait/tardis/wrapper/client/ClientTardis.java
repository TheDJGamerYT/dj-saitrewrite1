package mdteam.ait.tardis.wrapper.client;

import mdteam.ait.client.util.ClientShakeUtil;
import mdteam.ait.client.util.ClientTardisUtil;
import mdteam.ait.core.blockentities.ConsoleBlockEntity;
import mdteam.ait.core.blockentities.DoorBlockEntity;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.network.ClientAITNetworkManager;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.util.Corners;
import mdteam.ait.tardis.variant.exterior.ExteriorVariantSchema;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClientTardis {
    private final UUID tardis_ID;
    private ExteriorVariantSchema exterior_variant_schema;
    private ExteriorSchema exterior_schema;

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
        this.exterior = new ClientTardisExterior(this);
        this.exterior_schema = exteriorSchema;
        this.exterior_variant_schema = exteriorVariantSchema;
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

    /**
     * Retrieves the exterior variant schema.
     *
     * @return  the exterior variant schema
     */
    public ExteriorVariantSchema getExteriorVariant() {
        return exterior_variant_schema;
    }

    /**
     * Retrieves the exterior type of the object.
     *
     * @return the exterior schema of the object
     */
    public ExteriorSchema getExteriorType() {
        return exterior_schema;
    }

    /**
     * Sets the exterior variant of the object.
     *
     * @param  exteriorVariantSchema  the exterior variant to set
     */
    public void setExteriorVariant(ExteriorVariantSchema exteriorVariantSchema) {
        if (exteriorVariantSchema == null) return;
        this.exterior_variant_schema = exteriorVariantSchema;
    }

    /**
     * Sets the exterior type for the object.
     *
     * @param  exteriorSchema    the exterior schema to set
     */
    public void setExteriorType(ExteriorSchema exteriorSchema) {
        if (exteriorSchema == null) return;
        this.exterior_schema = exteriorSchema;
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
        private boolean isDoorOpen = false;

        public ClientTardisExterior(ClientTardis tardis) {
            this.tardis = tardis;
        }

        public ClientTardis getTardis() {
            return tardis;
        }

        public void setExteriorBlockPos(BlockPos exteriorBlockPos) {
            this.exteriorBlockPos = exteriorBlockPos;
        }

        public BlockPos getExteriorBlockPos() {
            return exteriorBlockPos;
        }

        public boolean isDoorOpen() {
            return isDoorOpen;
        }

        public void setDoorOpen(boolean isDoorOpen) {
            this.isDoorOpen = isDoorOpen;
        }
    }
}