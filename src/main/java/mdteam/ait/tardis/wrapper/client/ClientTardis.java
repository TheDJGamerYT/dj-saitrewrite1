package mdteam.ait.tardis.wrapper.client;

import mdteam.ait.core.blockentities.ConsoleBlockEntity;
import mdteam.ait.core.blockentities.DoorBlockEntity;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.network.ClientAITNetworkManager;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.util.Corners;
import mdteam.ait.tardis.variant.exterior.ExteriorVariantSchema;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientTardis {
    private final UUID tardisID;
    private ExteriorVariantSchema exteriorVariantSchema;
    private ExteriorSchema exteriorSchema;

    private final ClientTardisTravel travel;

    private final ClientTardisDesktop desktop;
    private final ClientTardisLoadedCache loadedCache;

    private Corners corners;
    private boolean subscribedToInterior = false;
    private boolean subscribedToExterior = false;

    public ClientTardis(UUID tardisID, ExteriorVariantSchema exteriorVariantSchema, ExteriorSchema exteriorSchema) {
        this.tardisID = tardisID;
        this.travel = new ClientTardisTravel(this);
        this.desktop = new ClientTardisDesktop(this);
        this.loadedCache = new ClientTardisLoadedCache(this);
        this.exteriorSchema = exteriorSchema;
        this.exteriorVariantSchema = exteriorVariantSchema;
    }

    public ClientTardisTravel getTravel() {
        return travel;
    }

    public ClientTardisDesktop getDesktop() {
        return desktop;
    }

    public ClientTardisLoadedCache getLoadedCache() {
        return loadedCache;
    }

    public void subscribeToInterior() {
        ClientAITNetworkManager.ask_for_interior_subscriber(getTardisID());
        this.subscribedToInterior = true;
    }

    public void subscribeToExterior() {
        ClientAITNetworkManager.ask_for_exterior_subscriber(getTardisID());
        this.subscribedToExterior = true;
    }

    public void unsubscribeToInterior() {
        ClientAITNetworkManager.send_interior_unloaded(getTardisID());
        this.subscribedToInterior = false;
    }

    public void unsubscribeToExterior() {
        ClientAITNetworkManager.send_exterior_unloaded(getTardisID());
        this.subscribedToExterior = false;
    }

    public boolean isSubscribedToInterior() {
        return subscribedToInterior;
    }

    public boolean isSubscribedToExterior() {
        return subscribedToExterior;
    }

    /**
     * Returns the Tardis ID.
     *
     * @return the Tardis ID
     */
    public UUID getTardisID() {
        return tardisID;
    }

    /**
     * Retrieves the exterior variant schema.
     *
     * @return  the exterior variant schema
     */
    public ExteriorVariantSchema getExteriorVariant() {
        return exteriorVariantSchema;
    }

    /**
     * Retrieves the exterior type of the object.
     *
     * @return the exterior schema of the object
     */
    public ExteriorSchema getExteriorType() {
        return exteriorSchema;
    }

    /**
     * Sets the exterior variant of the object.
     *
     * @param  exteriorVariantSchema  the exterior variant to set
     */
    public void setExteriorVariant(ExteriorVariantSchema exteriorVariantSchema) {
        if (exteriorVariantSchema == null) return;
        this.exteriorVariantSchema = exteriorVariantSchema;
    }

    /**
     * Sets the exterior type for the object.
     *
     * @param  exteriorSchema    the exterior schema to set
     */
    public void setExteriorType(ExteriorSchema exteriorSchema) {
        if (exteriorSchema == null) return;
        this.exteriorSchema = exteriorSchema;
    }

    public class ClientTardisTravel {
        private final ClientTardis tardis;
        private int speed = 0;

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
    }

    public class ClientTardisDesktop {

        private final ClientTardis tardis;

        private Corners corners = null;
        private BlockPos consolePos = null;

        public ClientTardisDesktop(ClientTardis tardis) {
            this.tardis = tardis;
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
        private final List<ExteriorBlockEntity> loadedExteriorBlockEntities = new ArrayList<>();
        private final List<DoorBlockEntity> loadedDoorBlockEntities = new ArrayList<>();

        public ClientTardisLoadedCache(ClientTardis tardis) {
            this.tardis = tardis;
        }

        public void loadConsoleBlock(ConsoleBlockEntity consoleBlockEntity) {
            loadedConsoleBlockEntities.add(consoleBlockEntity);
        }
        public void loadExteriorBlock(ExteriorBlockEntity exteriorBlockEntity) {
            loadedExteriorBlockEntities.add(exteriorBlockEntity);
        }
        public void loadDoorBlock(DoorBlockEntity doorBlockEntity) {
            loadedDoorBlockEntities.add(doorBlockEntity);
        }

        public void unloadConsoleBlock(ConsoleBlockEntity consoleBlockEntity) {
            if (!loadedConsoleBlockEntities.contains(consoleBlockEntity)) return;
            loadedConsoleBlockEntities.remove(consoleBlockEntity);
        }
        public void unloadExteriorBlock(ExteriorBlockEntity exteriorBlockEntity) {
            if (!loadedExteriorBlockEntities.contains(exteriorBlockEntity)) return;
            loadedExteriorBlockEntities.remove(exteriorBlockEntity);
        }
        public void unloadDoorBlock(DoorBlockEntity doorBlockEntity) {
            if (!loadedDoorBlockEntities.contains(doorBlockEntity)) return;
            loadedDoorBlockEntities.remove(doorBlockEntity);
        }

        public boolean isConsoleBlockLoaded(ConsoleBlockEntity consoleBlockEntity) {
            return loadedConsoleBlockEntities.contains(consoleBlockEntity);
        }

        public boolean isExteriorBlockLoaded(ExteriorBlockEntity exteriorBlockEntity) {
            return loadedExteriorBlockEntities.contains(exteriorBlockEntity);
        }

        public boolean isDoorBlockLoaded(DoorBlockEntity doorBlockEntity) {
            return loadedDoorBlockEntities.contains(doorBlockEntity);
        }

        public boolean hasAnyConsolesLoaded() {
            return !loadedConsoleBlockEntities.isEmpty();
        }

        public boolean hasAnyExteriorsLoaded() {
            return !loadedExteriorBlockEntities.isEmpty();
        }

        public boolean hasAnyDoorsLoaded() {
            return !loadedDoorBlockEntities.isEmpty();
        }

        public void reset() {
            loadedConsoleBlockEntities.clear();
            loadedExteriorBlockEntities.clear();
            loadedDoorBlockEntities.clear();
        }
    }
}