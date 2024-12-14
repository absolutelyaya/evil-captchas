package absolutelyaya.captcha.screen.elements;

import absolutelyaya.captcha.CAPTCHAClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.math.RotationAxis;

public class SpinningPigElement
{
	static final PigEntity pig;
	
	public static void render(DrawContext context, float time)
	{
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(0, 0, 200);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10f));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 8f));
		matrices.scale(45, -45, 45);
		
		RenderSystem.enableBlend();
		EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
		matrices.push();
		DiffuseLighting.method_34742();
		dispatcher.setRenderShadows(false);
		dispatcher.render(pig, 0, 0, 0, 0, 0, matrices, context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
		context.draw();
		dispatcher.setRenderShadows(true);
		DiffuseLighting.enableGuiDepthLighting();
		matrices.pop();
		matrices.pop();
	}
	
	static
	{
		CAPTCHAClient.FAKE_WORLD.addEntity(pig = new PigEntity(EntityType.PIG, CAPTCHAClient.FAKE_WORLD));
	}
}
