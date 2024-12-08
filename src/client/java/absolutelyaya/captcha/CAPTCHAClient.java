package absolutelyaya.captcha;

import absolutelyaya.captcha.component.CaptchaComponents;
import absolutelyaya.captcha.component.IConfigComponent;
import absolutelyaya.captcha.component.IPlayerComponent;
import absolutelyaya.captcha.networking.ClientPacketRegistry;
import absolutelyaya.captcha.networking.RequestCaptchaPayload;
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

import static absolutelyaya.captcha.CAPTCHA.config;

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
			if(!config.validationExpiration.getValue())
				return;
			if(validationTimer > 0 && (client.currentScreen == null || client.currentScreen instanceof ChatScreen) && !client.isPaused())
				validationTimer--;
			if(validationTimer == 60)
				client.player.sendMessage(Text.translatable("captcha.message.expired"), true);
			if(validationTimer == 0)
				requestRandomCaptcha("generic-verify");
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if(config.loginCaptcha.getValue())
				requestRandomCaptcha("generic");
		});
		ClientPlayerBlockBreakEvents.AFTER.register((world, player, pos, state) -> {
			if(!config.miningCaptcha.getValue())
				return;
			if(player != MinecraftClient.getInstance().player || player.isCreative())
				return;
			//boo lazy yaya hard coding shit (I've already spent too long on this joke mod, no one's gonna care about it anyways)
			if((state.isOf(Blocks.DIAMOND_ORE) || state.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) && world.random.nextFloat() < 0.25f)
				requestRandomCaptcha("mine");
			else if((state.isOf(Blocks.DEEPSLATE_EMERALD_ORE) || state.isOf(Blocks.DEEPSLATE_EMERALD_ORE)) && world.random.nextFloat() < 0.2f)
				requestRandomCaptcha("mine");
			else if((state.isOf(Blocks.DEEPSLATE_GOLD_ORE) || state.isOf(Blocks.DEEPSLATE_GOLD_ORE)) && world.random.nextFloat() < 0.1f)
				requestRandomCaptcha("mine");
			else if((state.isOf(Blocks.DEEPSLATE_IRON_ORE) || state.isOf(Blocks.DEEPSLATE_IRON_ORE)) && world.random.nextFloat() < 0.01f)
				requestRandomCaptcha("mine");
			else if((state.isOf(Blocks.OBSIDIAN)) && world.random.nextFloat() < 0.2f)
				requestRandomCaptcha("mine");
			else if((state.isOf(Blocks.GOLD_BLOCK)) && world.random.nextFloat() < 0.2f)
				requestRandomCaptcha("mine");
		});
	}
	
	public static void requestRandomCaptcha(String reason)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		if(client.world == null || client.player == null)
			return;
		IConfigComponent global = CaptchaComponents.CONFIG.get(client.world.getScoreboard());
		IPlayerComponent player = CaptchaComponents.PLAYER.get(client.player);
		requestRandomCaptcha(global.getCurDifficulty() + player.getLocalDifficulty(), reason);
	}
	
	public static void requestRandomCaptcha(float difficulty, String reason)
	{
		ClientPlayNetworking.send(new RequestCaptchaPayload(reason, difficulty));
		if(MinecraftClient.getInstance() instanceof MinecraftClient client && client.world != null)
			validationTimer = config.expirationDelayMin.getValue() * 20 + client.world.random.nextInt(config.expirationDelayMax.getValue() * 20);
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