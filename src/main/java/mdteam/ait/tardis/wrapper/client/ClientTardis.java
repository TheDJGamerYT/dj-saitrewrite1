package mdteam.ait.tardis.wrapper.client;

import mdteam.ait.AITMod;
import mdteam.ait.client.util.ClientShakeUtil;
import mdteam.ait.client.util.ClientTardisUtil;
import mdteam.ait.core.blockentities.ConsoleBlockEntity;
import mdteam.ait.core.blockentities.DoorBlockEntity;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.network.ClientAITNetworkManager;
import mdteam.ait.registry.ExteriorVariantRegistry;
import mdteam.ait.tardis.TardisDesktopSchema;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.handler.DoorHandler;
import mdteam.ait.tardis.sound.HumSound;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.util.Corners;
import mdteam.ait.tardis.variant.exterior.ExteriorVariantSchema;
import net.minecraft.util.Identifier;
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
    private List<TardisDesktopSchema> UNLOCKED_DESKTOPS = new ArrayList<>(); // Unlocked Interiors

    private Corners corners;
    private boolean subscribed_to_interior = false;
    private boolean subscribe_to_exterior = false;
    private boolean siege_mode = false;
    private boolean powered = false;
    private boolean alarms_enabled = false;
    private boolean falling = false;
    private boolean is_crashing = false;
    private boolean handbrake_active = false;
    private boolean ground_searching = false;
    private boolean is_refueling = false;
    private boolean hail_mary = false;
    private boolean anti_gravs = false;
    private boolean auto_land = false;
    private boolean has_cartridge = false;
    private double fuel = 0;

    public ClientTardis(UUID tardisID, ExteriorVariantSchema exteriorVariantSchema, ExteriorSchema exteriorSchema) {
        this.tardis_ID = tardisID;
        this.travel = new ClientTardisTravel(this);
        this.desktop = new ClientTardisDesktop(this);
        this.load_cache = new ClientTardisLoadedCache(this);
        this.exterior = new ClientTardisExterior(this, exteriorVariantSchema, exteriorSchema);
    }

    public void setHandbrakeState(boolean handbrake_active) {
        this.handbrake_active = handbrake_active;
    }

    public boolean isHandbrakeActive() {
        return handbrake_active;
    }

    public void setGroundSearchingMode(boolean ground_searching) {
        this.ground_searching = ground_searching;
    }

    public boolean isGroundSearchingMode() {
        return ground_searching;
    }

    public ClientTardisExterior getExterior() {
        return this.exterior;
    }

    public void setFalling(boolean falling) {
        this.falling = falling;
    }

    public boolean isFalling() {
        return falling;
    }
    public boolean isGrowth() {
        return this.exterior.getExteriorVariantSchema().equals(ExteriorVariantRegistry.CORAL_GROWTH);
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

    public void setCrashingState(boolean is_crashing) {
        this.is_crashing = is_crashing;
    }

    public boolean isCrashing() {
        return is_crashing;
    }

    public void setFuel(double fuel) {
        this.fuel = fuel;
    }

    public double getFuel() {
        return fuel;
    }

    public void setIsRefueling(boolean isRefueling) {
        this.is_refueling = isRefueling;
    }

    public boolean isRefueling() {
        return is_refueling;
    }

    public void setHailMaryActive(boolean hailMary) {
        this.hail_mary = hailMary;
    }

    public boolean hailMaryActive() {
        return hail_mary;
    }

    public void setAntiGravsActive(boolean antiGravs) {
        this.anti_gravs = antiGravs;
    }

    public boolean antiGravsActive() {
        return anti_gravs;
    }

    public void setAutoLandActive(boolean autoLand) {
        this.auto_land = autoLand;
    }

    public boolean autoLandActive() {
        return auto_land;
    }

    public void setHasCartridge(boolean hasCartridge) {
        this.has_cartridge = hasCartridge;
    }

    public boolean hasCartridge() {
        return has_cartridge;
    }

    public void addUnlockedDesktop(TardisDesktopSchema schema) {
        UNLOCKED_DESKTOPS.add(schema);
    }

    public void removeUnlockedDesktops(TardisDesktopSchema schema) {
        UNLOCKED_DESKTOPS.remove(schema);
    }

    public List<TardisDesktopSchema> setUnlockedDesktops(List<TardisDesktopSchema> desktops) {
        UNLOCKED_DESKTOPS = desktops;
        return UNLOCKED_DESKTOPS;
    }

    public List<TardisDesktopSchema> getUnlockedDesktops() {
        return UNLOCKED_DESKTOPS;
    }

    public boolean isDesktopUnlocked(TardisDesktopSchema unlockedInterior) {
        return UNLOCKED_DESKTOPS.contains(unlockedInterior);
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
        private AbsoluteBlockPos.Directed position;
        private AbsoluteBlockPos.Directed destination;
        private AbsoluteBlockPos.Directed last_position;
        private int increment;
        private int flight_time;

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

        public void setPosition(AbsoluteBlockPos.Directed position) {
            this.position = position;
        }

        public AbsoluteBlockPos.Directed getPosition() {
            return position;
        }

        public void setDestination(AbsoluteBlockPos.Directed destination) {
            this.destination = destination;
        }

        public AbsoluteBlockPos.Directed getDestination() {
            return destination;
        }

        public void setLastPosition(AbsoluteBlockPos.Directed last_position) {
            this.last_position = last_position;
        }

        public AbsoluteBlockPos.Directed getLastPosition() {
            return last_position;
        }

        public void setIncrement(int increment) {
            this.increment = increment;
        }

        public int getIncrement() {
            return increment;
        }

        public void setFlightTime(int flight_time) {
            this.flight_time = flight_time;
        }

        public int getFlightTime() {
            return flight_time;
        }
    }

    public class ClientTardisDesktop {
        private final ClientTardis tardis;
        private Corners corners = null;
        private BlockPos consolePos = null;
        private DoorHandler.DoorStateEnum tempInteriorDoorState = null;
        private TardisDesktopSchema desktopSchema;
        private HumSound hum;

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

        public DoorHandler.DoorStateEnum getAnimationInteriorState() {
            return this.tempInteriorDoorState;
        }

        public void syncInteriorAnimationState() {
            this.tempInteriorDoorState = this.getTardis().getExterior().doorState;
        }

        public void setDesktopSchema(TardisDesktopSchema desktopSchema) {
            this.desktopSchema = desktopSchema;
        }

        public TardisDesktopSchema getDesktopSchema() {
            return desktopSchema;
        }

        public void setHumSound(HumSound hum) {
            this.hum = hum;
        }

        public HumSound getHumSound() {
            return hum;
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

        public ExteriorBlockEntity __getExteriorBlockEntity() {
            return loadedExteriorBlockEntity;
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
            getTardis().getExterior().tempExteriorDoorState = null;
        }
        public void unloadDoorBlock(DoorBlockEntity doorBlockEntity) {
            if (!loadedDoorBlockEntities.contains(doorBlockEntity)) return;
            loadedDoorBlockEntities.remove(doorBlockEntity);
            if (!hasAnyDoorsLoaded()) getTardis().getDesktop().tempInteriorDoorState = null;
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
        private boolean cloaked = false;

        public static String TEXTURE_PATH = "textures/blockentities/exteriors/";
        private boolean overgrown = false;
        private boolean locked = false;
        private DoorHandler.DoorStateEnum tempExteriorDoorState = null;
        private String positionDimensionValue = "";
        private String destinationDimensionValue = "";

        public void setDoorLocked(boolean locked) {
            this.locked = locked;
        }

        public boolean getDoorLocked() {
            return locked;
        }

        public ClientTardisExterior(ClientTardis tardis, ExteriorVariantSchema exterior_variant_schema, ExteriorSchema exterior_schema) {
            this.tardis = tardis;
            this.exterior_variant_schema = exterior_variant_schema;
            this.exterior_schema = exterior_schema;
        }

        public ClientTardis getTardis() {
            return tardis;
        }

        public void setCloakedState(boolean cloaked) {
            this.cloaked = cloaked;
        }

        public boolean isCloaked() {
            return cloaked;
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
            this.tempExteriorDoorState = state;
            this.getTardis().getDesktop().tempInteriorDoorState = state;
        }

        public DoorHandler.DoorStateEnum getDoorState() {
            return doorState;
        }
        public void setOvergrown(boolean overgrown) {
            this.overgrown = overgrown;
        }

        public boolean isOvergrown() {
            return this.overgrown;
        }

        public Identifier getOvergrownTexture() {
            ExteriorSchema exterior = getExterior().getExteriorSchema();
            return new Identifier(AITMod.MOD_ID, TEXTURE_PATH + exterior.toString().toLowerCase() + "/" + exterior.toString().toLowerCase() + "_" + "overgrown" + ".png");
        }

        public String getPositionDimensionValue() {
            return positionDimensionValue;
        }

        public void setPositionDimensionValue(String dimensionValue) {
            this.positionDimensionValue = dimensionValue;
        }

        public void setDestinationDimensionValue(String dimensionValue) {
            this.destinationDimensionValue = dimensionValue;
        }
        public String getDestinationDimensionValue() {
            return destinationDimensionValue;
        }

        public DoorHandler.DoorStateEnum getAnimationExteriorState() {
            return this.tempExteriorDoorState;
        }

        public void syncExteriorAnimationState() {
            this.tempExteriorDoorState = this.doorState;
        }
    }
}