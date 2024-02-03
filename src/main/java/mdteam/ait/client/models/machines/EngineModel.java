package mdteam.ait.client.models.machines;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class EngineModel extends SinglePartEntityModel {
	private final ModelPart engine;
	public EngineModel(ModelPart root) {
		this.engine = root.getChild("engine");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData engine = modelPartData.addChild("engine", ModelPartBuilder.create().uv(0, 0).cuboid(-2.3015F, -6.1838F, -24.0F, 48.0F, 16.0F, 48.0F, new Dilation(0.05F))
		.uv(0, 65).cuboid(-2.3015F, -22.1838F, -8.0F, 48.0F, 16.0F, 32.0F, new Dilation(0.05F))
		.uv(79, 169).cuboid(13.6985F, -38.1838F, -8.0F, 16.0F, 16.0F, 32.0F, new Dilation(0.05F))
		.uv(145, 0).cuboid(16.6985F, -34.1838F, -7.0F, 8.0F, 8.0F, 30.0F, new Dilation(0.05F)), ModelTransform.pivot(-21.6985F, 14.1838F, 0.0F));

		ModelPartData cube_r1 = engine.addChild("cube_r1", ModelPartBuilder.create().uv(107, 120).cuboid(-6.25F, -48.0F, -8.0F, 23.0F, 16.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		ModelPartData cube_r2 = engine.addChild("cube_r2", ModelPartBuilder.create().uv(0, 152).cuboid(-16.75F, -48.0F, -8.0F, 23.0F, 16.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(43.397F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		ModelPartData cube_r3 = engine.addChild("cube_r3", ModelPartBuilder.create().uv(129, 65).cuboid(-24.0F, -32.1F, -24.0F, 48.0F, 0.0F, 21.0F, new Dilation(0.0F))
		.uv(0, 114).cuboid(-24.0F, -32.0F, -24.0F, 48.0F, 16.0F, 21.0F, new Dilation(0.0F)), ModelTransform.of(21.6985F, 14.196F, 10.419F, 0.3927F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 512, 512);
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		engine.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart getPart() {
		return engine;
	}

	@Override
	public void setAngles(Entity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
	}
}