package absolutelyaya.captcha;

import absolutelyaya.captcha.data.*;
import absolutelyaya.captcha.networking.ClientPacketRegistry;
import absolutelyaya.captcha.screen.AbstractCaptchaScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;

public class CAPTCHAClient implements ClientModInitializer
{
	static float difficulty = 5f;
	static int validationTimer;
	
	@Override
	public void onInitializeClient()
	{
		new SingleBoxCaptchaDataManager();
		new MultiBoxCaptchaPoolManager();
		new ImageSearchCaptchaPoolManager();
		new PuzzleSlideDataManager();
		new ComprehensionTestManager();
		new AmongusPoolManager();
		ClientPacketRegistry.register();
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.player == null)
				return;
			if(validationTimer > 0 && (client.currentScreen == null || client.currentScreen instanceof ChatScreen) && !client.isPaused())
				validationTimer--;
			if(validationTimer == 60)
				client.player.sendMessage(Text.translatable("captcha.message.expired"), true);
			if(validationTimer == 0)
				openRandomCaptcha("generic-verify");
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			openRandomCaptcha("generic");
		});
		ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
			if(player != MinecraftClient.getInstance().player || player.isCreative())
				return;
			//boo lazy yaya hard coding shit (I've already spent too long on this joke mod, no one's gonna care about it anyways)
			if((state.isOf(Blocks.DIAMOND_ORE) || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) && world.random.nextFloat() < 0.25f)
				openRandomCaptcha("mine");
			else if((state.isOf(Blocks.DEEPSLATE_EMERALD_ORE) || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE)) && world.random.nextFloat() < 0.2f)
				openRandomCaptcha("mine");
			else if((state.isOf(Blocks.DEEPSLATE_GOLD_ORE) || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)) && world.random.nextFloat() < 0.1f)
				openRandomCaptcha("mine");
			else if((state.isOf(Blocks.DEEPSLATE_IRON_ORE) || state.isOf(Blocks.DEEPSLATE_IRON_ORE)) && world.random.nextFloat() < 0.01f)
				openRandomCaptcha("mine");
			else if((state.isOf(Blocks.OBSIDIAN)) && world.random.nextFloat() < 0.2f)
				openRandomCaptcha("mine");
			else if((state.isOf(Blocks.GOLD_BLOCK)) && world.random.nextFloat() < 0.2f)
				openRandomCaptcha("mine");
		});
	}
	
	public static void openRandomCaptcha(String reason)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.world != null && !(client.currentScreen instanceof AbstractCaptchaScreen))
		{
			AbstractCaptchaScreen.openRandomCaptcha(client, difficulty, reason);
			difficulty++;
		}
		else if(client.world == null)
			CAPTCHA.LOGGER.error("Failed to open captcha! Client must be in a world.");
		else if(client.currentScreen instanceof AbstractCaptchaScreen)
			CAPTCHA.LOGGER.error("Failed to open captcha! There's already an open captcha.");
		validationTimer = 2400 + client.world.random.nextInt(2400); //120s + (up to 120s)
	}
}