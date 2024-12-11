package absolutelyaya.captcha.rendering.entity;

import absolutelyaya.captcha.entity.SlimerEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;

// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class SlimeModel extends SinglePartEntityModel<SlimerEntity>
{
	private final ModelPart root;
	private final ModelPart bone;
	private final ModelPart leftEye;
	private final ModelPart rightEye;
	private final ModelPart mouth;
	
	public SlimeModel(ModelPart root)
	{
		this.root = root;
		this.bone = root.getChild("bone");
		this.leftEye = this.bone.getChild("leftEye");
		this.rightEye = this.bone.getChild("rightEye");
		this.mouth = this.bone.getChild("mouth");
	}
	
	public static TexturedModelData getTexturedModelData()
	{
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bone = modelPartData.addChild("bone", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -12.0F, -5.0F, 10.0F, 10.0F, 10.0F, new Dilation(0.0F))
		.uv(0, 20).mirrored().cuboid(-5.0F, -12.0F, -5.0F, 10.0F, 10.0F, 10.0F, new Dilation(2.0F)).mirrored(false), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData leftEye = bone.addChild("leftEye", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(2.5F, -8.5F, -5.0F));

		ModelPartData rightEye = bone.addChild("rightEye", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(-2.5F, -8.5F, -5.0F));

		ModelPartData mouth = bone.addChild("mouth", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-1.5F, -0.5F, -1.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(0.5F, -5.5F, -5.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	
	@Override
	public void setAngles(SlimerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		getPart().traverse().forEach(ModelPart::resetTransform);
		updateAnimation(entity.hopAnimationState, SlimeAnimation.hop, ageInTicks);
	}
	
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color)
	{
		getPart().render(matrices, vertices, light, overlay, color);
	}
	
	@Override
	public ModelPart getPart()
	{
		return root;
	}
}