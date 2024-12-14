package absolutelyaya.captcha.screen.elements;

import absolutelyaya.captcha.CAPTCHAClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class LiveReactionElement
{
	static final TextRenderer tRenderer = MinecraftClient.getInstance().textRenderer;
	static final CreeperEntity creeper;
	static float reactTime;
	static Text reaction;
	
	public static void react()
	{
		reactTime = 6f;
		if(MinecraftClient.getInstance().player instanceof PlayerEntity player)
		{
			if(reaction != null)
				return;
			reaction = Text.translatable("captcha.addon.live-reaction.line" + player.getRandom().nextInt(8));
			player.playSound(SoundEvents.ENTITY_CREEPER_HURT, 1f, 1f);
		}
	}
	
	public static void render(DrawContext context, float delta, int x, int y)
	{
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		context.fill(x, y + 28, x + 128, y + 128, 0xff222034);
		context.drawBorder(x, y + 28, 128, 100, 0xffffffff);
		context.enableScissor(x, y + 28, x + 127, y + 127);
		matrices.translate(x, y, 0);
		matrices.translate(66, 160, 200);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-5f));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-25f));
		matrices.scale(69, -69, 69);
		
		RenderSystem.enableBlend();
		EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
		matrices.push();
		DiffuseLighting.method_34742();
		dispatcher.setRenderShadows(false);
		dispatcher.render(creeper, 0, 0, 0, 0, 1, matrices, context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
		context.draw();
		dispatcher.setRenderShadows(true);
		DiffuseLighting.enableGuiDepthLighting();
		matrices.pop();
		context.disableScissor();
		matrices.pop();
		if(reaction != null)
		{
			matrices.push();
			matrices.translate(x + 64, y, 0);
			matrices.translate(-tRenderer.getWidth(reaction) / 2f, 32, 1000);
			context.drawText(tRenderer, reaction, 0, 0, 0xffffffff, true);
			matrices.pop();
			if((reactTime -= delta / 20) <= 0)
				reaction = null;
		}
		creeper.headYaw = MathHelper.lerp(delta / 20f, creeper.headYaw, reaction == null ? 0f : -20f);
	}
	
	static
	{
		CAPTCHAClient.FAKE_WORLD.spawnEntity(creeper = new CreeperEntity(EntityType.CREEPER, CAPTCHAClient.FAKE_WORLD));
	}
}
