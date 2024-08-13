package loqor.ait.core.data;

import loqor.ait.AITMod;
import loqor.ait.api.tardis.ArtronHolder;
import loqor.ait.tardis.util.TardisUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class RiftChunkData extends PersistentState {
	private HashMap<RegistryKey<World>, RiftChunkMap> chunks;

	public RiftChunkData() {
		this.chunks = new HashMap<>();
	}

	public RiftChunkMap getMap(RegistryKey<World> key) {
		return this.chunks.computeIfAbsent(key, k -> new RiftChunkMap());
	}

	public RiftChunkMap getMap(World world) {
		return this.getMap(world.getRegistryKey());
	}

	public static boolean isRiftChunk(ChunkPos chunkPos) {
		return ChunkRandom.getSlimeRandom(chunkPos.x, chunkPos.z, TardisUtil.getOverworld().getSeed(), 987234910L)
				.nextInt(8) == 0;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		chunks.keySet().forEach(key -> nbt.put(key.getValue().toString(), chunks.get(key).serialize()));

		return nbt;
	}

	public static RiftChunkData loadNbt(NbtCompound nbt) {
		RiftChunkData created = new RiftChunkData();

		nbt.getKeys().forEach(key -> created.chunks.put(RegistryKey.of(RegistryKeys.WORLD, new Identifier(key)), new RiftChunkMap(nbt.getCompound(key))));

		return created;
	}

	public static RiftChunkData getInstance(MinecraftServer server) {
		PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

		RiftChunkData state = manager.getOrCreate(
				RiftChunkData::loadNbt,
				RiftChunkData::new,
				AITMod.MOD_ID
		);

		return state;
	}

	public static class RiftChunkMap extends HashMap<ChunkPos, RiftChunk> {
		public RiftChunkMap() {
			super();
		}
		public RiftChunkMap(NbtCompound data) {
			this();

			this.deserialize(data);
		}

		/**
		 * Gets the chunk at the given position, or generates one if it doesn't exist and is a rift chunk
		 * Should be used instead of default get()
		 */
		public Optional<RiftChunk> getChunk(ChunkPos pos) {
			if (this.containsKey(pos)) {
				return Optional.of(this.get(pos));
			}

			if (!isRiftChunk(pos)) {
				return Optional.empty();
			}

			this.put(pos, new RiftChunk(pos));
			return Optional.of(this.get(pos));
		}

		public NbtCompound serialize() {
			NbtCompound data = new NbtCompound();

			this.values().forEach(chunk -> data.put(chunk.getUuid().toString(), chunk.serialize()));

			return data;
		}

		private void deserialize(NbtCompound data) {
			data.getKeys().forEach(key -> {
				RiftChunk generated = new RiftChunk(data.getCompound(key));
				this.put(generated.getPos(), generated);
			});
		}
	}

	public static class RiftChunk implements ArtronHolder {
		private final UUID id;
		private final ChunkPos pos;
		private final double maxArtron;
		private double artron;
		private int lastTick;

		public RiftChunk(ChunkPos pos) {
			this.id = UUID.randomUUID();

			Random random = new Random();
			this.maxArtron = random.nextInt(300, 1000);
			this.addFuel(random.nextInt(100, 800));

			this.pos = pos;
		}
		public RiftChunk(NbtCompound nbt) {
			this.id = nbt.getUuid("Id");
			this.pos = new ChunkPos(nbt.getLong("ChunkPos"));
			this.maxArtron = nbt.getDouble("MaxArtron");

			this.deserialize(nbt);
		}

		public UUID getUuid() {
			return this.id;
		}

		@Override
		public double getCurrentFuel() {
			return this.artron;
		}
		public double getCurrentFuel(MinecraftServer server) {
			this.update(server);

			return this.getCurrentFuel();
		}

		public double addFuel(double count, MinecraftServer server) {
			this.update(server);

			return this.addFuel(count);
		}
		public void removeFuel(double count, MinecraftServer server) {
			this.update(server);

			this.removeFuel(count);
		}

		@Override
		public void setCurrentFuel(double var) {
			this.artron = Math.min(this.maxArtron, var);
		}

		@Override
		public double getMaxFuel() {
			return this.maxArtron;
		}

		public ChunkPos getPos() {
			return this.pos;
		}

		/**
		 * Called when this chunk gets updated or queried
		 * @param ticks level tick counter
		 */
		private void update(int ticks) {
			// work out how long its been
			int time = ticks - this.lastTick;
			this.lastTick = ticks;

			if (time < 0) {
				return; // something went wrong :(
			}

			// increase artron levels
			this.addFuel(getArtronChangeFromTime(time));
		}
		public void update(MinecraftServer server) {
			this.update(server.getTicks());
		}
		public void update(ServerWorld world) {
			this.update(world.getServer());
		}

		private int getArtronChangeFromTime(int time) {
			return time / 2; // 20 TPS -> 10 AU per second
		}

		public NbtCompound serialize() {
			NbtCompound data = new NbtCompound();

			data.putLong("ChunkPos", this.pos.toLong());
			data.putUuid("Id", this.id);
			data.putDouble("MaxArtron", this.maxArtron);
			data.putDouble("Artron", this.artron);

			return data;
		}

		private void deserialize(NbtCompound data) {
			this.artron = data.getDouble("Artron");
		}
	}
}
