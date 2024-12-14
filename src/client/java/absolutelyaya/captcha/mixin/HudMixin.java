package absolutelyaya.captcha.mixin;

import absolutelyaya.captcha.component.CaptchaComponents;
import absolutelyaya.captcha.component.IPlayerComponent;
import absolutelyaya.captcha.screen.elements.LiveReactionElement;
import absolutelyaya.captcha.screen.elements.SpinningPigElement;
import absolutelyaya.captcha.screen.elements.WinterWonderlandElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class HudMixin
{
	@Shadow @Final private MinecraftClient client;
	
	@Shadow private int ticks;
	
	@Shadow @Final private Random random;
	
	@Unique float lastHealth;
	
	@Inject(method = "render", at = @At("HEAD"))
	void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
	{
		if(!(client.player instanceof PlayerEntity player))
			return;
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		IPlayerComponent comp = CaptchaComponents.PLAYER.get(player);
		float delta = tickCounter.getTickDelta(true);
		float time = ticks + delta;
		comp.getAddons().forEach(addon -> {
			matrices.push();
			switch(addon.type())
			{
				case "spinning-pig" -> {
					matrices.translate(context.getScaledWindowWidth() * addon.x(), context.getScaledWindowHeight() * addon.y(), 0);
					SpinningPigElement.render(context, time);
				}
				case "winter-wonderland" -> WinterWonderlandElement.render(context, delta);
				case "live-reaction" -> {
					LiveReactionElement.render(context, delta, context.getScaledWindowWidth() - 132, context.getScaledWindowHeight() - 132);
					if(random.nextFloat() < 0.001f || lastHealth > player.getHealth())
						LiveReactionElement.react();
					lastHealth = player.getHealth();
				}
			}
			matrices.pop();
		});
		matrices.pop();
	}
	
	@WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0))
	void onGetCrossHair(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original)
	{
		if(client.player instanceof PlayerEntity player && CaptchaComponents.PLAYER.get(player).hasAddon("custom-cursor"))
		{
			RenderSystem.defaultBlendFunc();
			instance.drawTexture(Identifier.of("textures/item/iron_sword.png"),
					instance.getScaledWindowWidth() / 2 - 16, instance.getScaledWindowHeight() / 2,
					0, 0, 16, 16, 16, 16);
			RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR,
					GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
			return;
		}
		original.call(instance, texture, x, y, width, height);
	}
}
