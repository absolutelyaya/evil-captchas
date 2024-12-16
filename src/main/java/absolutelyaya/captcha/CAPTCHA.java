package absolutelyaya.captcha;

import absolutelyaya.captcha.component.CaptchaComponents;
import absolutelyaya.captcha.component.IConfigComponent;
import absolutelyaya.captcha.component.IPlayerComponent;
import absolutelyaya.captcha.config.ServerConfig;
import absolutelyaya.captcha.data.*;
import absolutelyaya.captcha.networking.CaptchaDataSyncPayload;
import absolutelyaya.captcha.networking.openCaptcha;
import absolutelyaya.captcha.networking.PacketRegistry;
import absolutelyaya.captcha.registry.Commands;
import absolutelyaya.captcha.registry.DamageTypes;
import absolutelyaya.captcha.registry.EntityRegistry;
import absolutelyaya.captcha.registry.SoundRegistry;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CAPTCHA implements ModInitializer
{
	public static final String MOD_ID = "captcha";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Map<String, Integer> captchas = new HashMap<>();
	static final List<String> easy = List.of("butterflies", "puzzle-slide", "rorschach", "sponsor");
	public static ServerConfig config;

	@Override
	public void onInitialize()
	{
		new SingleBoxCaptchaDataManager();
		new MultiBoxCaptchaPoolManager();
		new ImageSearchCaptchaPoolManager();
		new PuzzleSlideDataManager();
		new ComprehensionTestManager();
		new AmongusPoolManager();
		SoundRegistry.register();
		PacketRegistry.register();
		DamageTypes.register();
		EntityRegistry.register();
		
		config = new ServerConfig();
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.register(dispatcher));
		
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, b) -> sendCaptchaData(player));
	}
	
	public static Identifier identifier(String path)
	{
		if(path.contains(":"))
			return Identifier.tryParse(path);
		return Identifier.of(MOD_ID, path);
	}
	
	public static Identifier texIdentifier(String path)
	{
		String[] segments = path.split(":");
		if(segments.length == 2)
			return Identifier.of(segments[0], "textures/" + segments[1] + ".png");
		return Identifier.of(MOD_ID, "textures/" + path + ".png");
	}
	
	public static void openRandomCaptcha(ServerPlayerEntity player, String reason)
	{
		IConfigComponent global = CaptchaComponents.CONFIG.get(player.getWorld().getScoreboard());
		IPlayerComponent playerComp = CaptchaComponents.PLAYER.get(player);
		float difficulty = global.getCurDifficulty() + playerComp.getLocalDifficulty();
		openRandomCaptcha(player, reason, difficulty);
	}
	
	public static void openRandomCaptcha(ServerPlayerEntity player, String reason, float difficulty)
	{
		String type = getRandomCaptchaType(difficulty, player.getRandom());
		openCaptcha(player, reason, type, difficulty);
	}
	
	public static void openCaptcha(ServerPlayerEntity player, String reason, String type, float difficulty)
	{
		IPlayerComponent playerComp = CaptchaComponents.PLAYER.get(player);
		ServerPlayNetworking.send(player, new openCaptcha(type, reason, difficulty));
		playerComp.startCaptcha(type, difficulty);
	}
	
	public static void sendCaptchaData(ServerPlayerEntity player)
	{
		NbtCompound data = new NbtCompound();
		
		data.put("amongus", AmongusPoolManager.compileToSyncData());
		data.put("comprehension", ComprehensionTestManager.compileToSyncData());
		data.put("image-search", ImageSearchCaptchaPoolManager.compileToSyncData());
		data.put("multi-box", MultiBoxCaptchaPoolManager.compileToSyncData());
		data.put("single-box", SingleBoxCaptchaDataManager.compileToSyncData());
		data.put("puzzle", PuzzleSlideDataManager.compileToSyncData());
		
		ServerPlayNetworking.send(player, new CaptchaDataSyncPayload(data));
	}
	
	public static String getRandomCaptchaType(float difficulty, Random random)
	{
		List<String> candidates = new ArrayList<>();
		for (Map.Entry<String, Integer> i : captchas.entrySet())
			if(difficulty >= i.getValue() && !(config.notEasy.getValue() && easy.contains(i.getKey())))
				candidates.add(i.getKey());
		if(candidates.isEmpty())
			return "single-boxes";
		return candidates.get(random.nextInt(candidates.size()));
	}
	
	static
	{
		captchas.put("single-boxes", 0);
		captchas.put("multi-boxes", 0);
		captchas.put("wonky-text", 0);
		captchas.put("puzzle-slide", 3);
		captchas.put("simple-comprehension", 5);
		captchas.put("image-search", 5);
		captchas.put("math", 5);
		captchas.put("rorschach", 10);
		captchas.put("wimmelbild", 10);
		captchas.put("sponsor", 10);
		captchas.put("wizard", 15);
		captchas.put("amongus", 15);
		captchas.put("advanced-comprehension", 20);
		captchas.put("gambling", 20);
		captchas.put("butterflies", 20);
		captchas.put("slimer", 20);
	}
}