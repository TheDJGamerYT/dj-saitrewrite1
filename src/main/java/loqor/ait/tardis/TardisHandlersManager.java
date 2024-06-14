package loqor.ait.tardis;

import com.google.gson.*;
import loqor.ait.AITMod;
import loqor.ait.core.data.base.Exclude;
import loqor.ait.core.util.LegacyUtil;
import loqor.ait.registry.impl.TardisComponentRegistry;
import loqor.ait.tardis.base.TardisComponent;
import loqor.ait.tardis.base.TardisTickable;
import loqor.ait.tardis.control.sequences.SequenceHandler;
import loqor.ait.tardis.data.*;
import loqor.ait.tardis.data.loyalty.LoyaltyHandler;
import loqor.ait.tardis.data.permissions.PermissionHandler;
import loqor.ait.tardis.data.properties.PropertiesHolder;
import loqor.ait.tardis.util.EnumMap;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.function.Consumer;

public class TardisHandlersManager extends TardisComponent implements TardisTickable {

	@Exclude
	private final EnumMap<IdLike, TardisComponent> handlers = new EnumMap<>(TardisComponentRegistry::values, TardisComponent[]::new);

	public TardisHandlersManager() {
        super(Id.HANDLERS);
    }

	@Override
	public void onCreate() {
		TardisComponentRegistry.getInstance().fill(this::createHandler);
	}

	@Override
	protected void onInit(InitContext ctx) {
		this.forEach(component -> TardisComponent.init(component, this.tardis, ctx));
	}

	private void forEach(Consumer<TardisComponent> consumer) {
		for (TardisComponent component : this.handlers.values()) {
			if (component == null)
				continue;

			consumer.accept(component);
		}
	}

	private void createHandler(TardisComponent component) {
		this.handlers.put(component.getId(), component);
	}

	/**
	 * Called on the END of a servers tick
	 *
	 * @param server the current server
	 */
	public void tick(MinecraftServer server) {
		this.forEach(component -> {
			if (!(component instanceof TardisTickable tickable))
				return;

			tickable.tick(server);
		});
	}

	/**
	 * Called on the START of a servers tick
	 *
	 * @param server the current server
	 */
	public void startTick(MinecraftServer server) {
		this.forEach(component -> {
			if (!(component instanceof TardisTickable tickable))
				return;

			tickable.startTick(server);
		});
	}

	@SuppressWarnings("unchecked")
	public <T extends TardisComponent> T get(IdLike id) {
		return (T) this.handlers.get(id);
	}

	@Override
	public void dispose() {
		super.dispose();

		this.forEach(TardisComponent::dispose);
		this.handlers.clear();
	}

	/**
	 * Do NOT use this setter if you don't know what you're doing. Use {@link loqor.ait.tardis.wrapper.client.ClientTardis#set(TardisComponent)}.
	 */
	@Deprecated
	public <T extends TardisComponent> void set(IdLike id, T t) {
		this.handlers.put(id, t);
	}

	@Deprecated
	public SonicHandler getSonic() {
		return this.tardis().sonic();
	}

	@Deprecated
	public StatsData getStats() {
		return this.tardis().stats();
	}

	@Deprecated
	public DoorData getDoor() {
		return this.tardis().getDoor();
	}

	@Deprecated
	public PropertiesHolder getProperties() {
		return this.tardis().properties();
	}

	@Deprecated
	public CloakData getCloak() {
		return this.get(Id.CLOAK);
	}

	@Deprecated
	public ShieldData getShields() {
		return this.get(Id.SHIELDS);
	}

	@Deprecated
	public ServerAlarmHandler getAlarms() {
		return this.tardis().alarm();
	}

	@Deprecated
	public WaypointHandler getWaypoints() {
		return this.tardis().waypoint();
	}

	@Deprecated
	public SequenceHandler getSequenceHandler() {
		return this.tardis().sequence();
	}

	@Deprecated
	public FlightData getFlight() {
		return this.tardis().flight();
	}

	@Deprecated
	public FuelData getFuel() {
		return this.tardis().fuel();
	}

	@Deprecated
	public TardisCrashData getCrashData() {
		return this.tardis().crash();
	}

	@Deprecated
	public SiegeData getSiege() {
		return this.get(Id.SIEGE);
	}

	@Deprecated
	public LoyaltyHandler getLoyalties() {
		return this.tardis().loyalty();
	}

	@Deprecated
	public OvergrownData getOvergrown() {
		return this.get(Id.OVERGROWN);
	}

	@Deprecated
	public PermissionHandler getPermissions() {
		return this.get(Id.PERMISSIONS);
	}

	@Deprecated
	public InteriorChangingHandler getInteriorChanger() {
		return this.get(Id.INTERIOR);
	}

	@Deprecated
	public EngineHandler getEngine() {
		return this.get(Id.ENGINE);
	}

	public static Object serializer() {
		return new Serializer();
	}

	static class Serializer implements JsonSerializer<TardisHandlersManager>, JsonDeserializer<TardisHandlersManager>  {

		@Override
		public TardisHandlersManager deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
			TardisHandlersManager manager = new TardisHandlersManager();
			Map<String, JsonElement> map = json.getAsJsonObject().asMap();

			boolean legacy = LegacyUtil.isHandlersLegacy(map);
			TardisComponentRegistry registry = TardisComponentRegistry.getInstance();

			for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
				String key = entry.getKey();
				JsonElement element = entry.getValue();

				// Skip legacy entries like "tardisId".
				if (LegacyUtil.isLegacyComponent(element))
					continue;

				IdLike id = legacy ? LegacyUtil.getLegacyId(key) : registry.get(key);

				if (id == null) {
					AITMod.LOGGER.error("Can't find a component id with name '{}'!", key);
					continue;
				}

				manager.set(id, context.deserialize(element, id.clazz()));
			}

			for (int i = 0; i < manager.handlers.size(); i++) {
				if (manager.handlers.get(i) != null)
					continue;

				IdLike id = registry.get(i);
				AITMod.LOGGER.warn("Appending new component {}", id);

				manager.set(id, id.create());
			}

			return manager;
		}

		@Override
		public JsonElement serialize(TardisHandlersManager manager, java.lang.reflect.Type type, JsonSerializationContext context) {
			JsonObject result = new JsonObject();

			manager.forEach(component -> result.add(
                    component.getId().name(),
                    context.serialize(component)
            ));

			return result;
		}
	}
}
