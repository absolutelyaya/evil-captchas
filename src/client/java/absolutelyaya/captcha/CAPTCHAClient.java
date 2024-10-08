package absolutelyaya.captcha;

import absolutelyaya.captcha.component.CaptchaComponents;
import absolutelyaya.captcha.component.IConfigComponent;
import absolutelyaya.captcha.data.*;
import absolutelyaya.captcha.networking.ClientPacketRegistry;
import absolutelyaya.captcha.networking.OpenCaptchaC2SPayload;
import absolutelyaya.captcha.screen.AbstractCaptchaScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;

public class CAPTCHAClient implements ClientModInitializer
{
	static int validationTimer;
	
	@Override
	public void onInitializeClient()
	{
		ClientPacketRegistry.register();
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.player == null)
				return;
			if(!CaptchaComponents.CONFIG.get(client.world.getScoreboard()).isValidationExpiration())
				return;
			if(validationTimer > 0 && (client.currentScreen == null || client.currentScreen instanceof ChatScreen) && !client.isPaused())
				validationTimer--;
			if(validationTimer == 60)
				client.player.sendMessage(Text.translatable("captcha.message.expired"), true);
			if(validationTimer == 0)
				openRandomCaptchaClient("generic-verify");
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			openRandomCaptchaClient("generic");
		});
		ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
			if(player != MinecraftClient.getInstance().player || player.isCreative())
				return;
			//boo lazy yaya hard coding shit (I've already spent too long on this joke mod, no one's gonna care about it anyways)
			if((state.isOf(Blocks.DIAMOND_ORE) || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) && world.random.nextFloat() < 0.25f)
				openRandomCaptchaClient("mine");
			else if((state.isOf(Blocks.DEEPSLATE_EMERALD_ORE) || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE)) && world.random.nextFloat() < 0.2f)
				openRandomCaptchaClient("mine");
			else if((state.isOf(Blocks.DEEPSLATE_GOLD_ORE) || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)) && world.random.nextFloat() < 0.1f)
				openRandomCaptchaClient("mine");
			else if((state.isOf(Blocks.DEEPSLATE_IRON_ORE) || state.isOf(Blocks.DEEPSLATE_IRON_ORE)) && world.random.nextFloat() < 0.01f)
				openRandomCaptchaClient("mine");
			else if((state.isOf(Blocks.OBSIDIAN)) && world.random.nextFloat() < 0.2f)
				openRandomCaptchaClient("mine");
			else if((state.isOf(Blocks.GOLD_BLOCK)) && world.random.nextFloat() < 0.2f)
				openRandomCaptchaClient("mine");
		});
	}
	
	static void openRandomCaptchaClient(String reason)
	{
		ClientPlayNetworking.send(new OpenCaptchaC2SPayload());
		CaptchaComponents.PLAYER.get(MinecraftClient.getInstance().player).startCaptcha();
		openRandomCaptcha(reason);
	}
	
	public static void openRandomCaptcha(String reason)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.world == null)
		{
			CAPTCHA.LOGGER.error("Failed to open captcha! Client must be in a world.");
			return;
		}
		if(client.player == null)
		{
			CAPTCHA.LOGGER.error("Failed to open captcha! Client doesn't have a valid main player.");
			return;
		}
		if(client.player.isSpectator())
			return;
		
		IConfigComponent config = CaptchaComponents.CONFIG.get(client.world.getScoreboard());
		if(client.world != null && !(client.currentScreen instanceof AbstractCaptchaScreen))
		{
			float local = CaptchaComponents.PLAYER.get(client.player).getLocalDifficulty();
			AbstractCaptchaScreen.openRandomCaptcha(client, config.getCurDifficulty() + local, reason);
		}
		else if(client.currentScreen instanceof AbstractCaptchaScreen)
			CAPTCHA.LOGGER.error("Failed to open captcha! There's already an open captcha.");
		validationTimer = config.getMinExpirationDelay() * 20 + client.world.random.nextInt(config.getMaxExpirationDelay() * 20);
	}
	
	public static void openSpecificCaptcha(String type, String reason, float difficulty)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.world != null)
			AbstractCaptchaScreen.openSpecificCaptcha(client, type, difficulty, reason);
		else
			CAPTCHA.LOGGER.error("Failed to open captcha! Client must be in a world.");
	}
}