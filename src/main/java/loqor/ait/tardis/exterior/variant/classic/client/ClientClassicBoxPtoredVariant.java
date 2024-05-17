package loqor.ait.tardis.exterior.variant.classic.client;

import loqor.ait.tardis.data.BiomeHandler;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

public class ClientClassicBoxPtoredVariant extends ClientClassicBoxVariant {
	public ClientClassicBoxPtoredVariant() {
		super("ptored");
	}

	@Override
	public Vector3f sonicItemTranslations() {
		return new Vector3f(0.56f, 1.325f, 1.165f);
	}

	@Override
	public Identifier getBiomeTexture(BiomeHandler.BiomeType biomeType) {
		return switch(biomeType) {
			default -> super.getBiomeTexture(biomeType);
			case SNOWY -> BiomeHandler.BiomeType.SNOWY.getTextureFromKey(texture());
			case SCULK -> BiomeHandler.BiomeType.SCULK.getTextureFromKey(texture());
			case CHORUS -> BiomeHandler.BiomeType.CHORUS.getTextureFromKey(texture());
			case CHERRY -> BiomeHandler.BiomeType.CHERRY.getTextureFromKey(texture());
		};
	}
}
