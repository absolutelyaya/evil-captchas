package absolutelyaya.captcha;

import absolutelyaya.captcha.component.CaptchaComponents;
import absolutelyaya.captcha.data.*;
import absolutelyaya.captcha.networking.CaptchaDataSyncPayload;
import absolutelyaya.captcha.networking.OpenRandomCaptchaPayload;
import absolutelyaya.captcha.networking.PacketRegistry;
import absolutelyaya.captcha.registry.Commands;
import absolutelyaya.captcha.registry.DamageTypes;
import absolutelyaya.captcha.registry.SoundRegistry;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CAPTCHA implements ModInitializer
{
	public static final String MOD_ID = "captcha";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
	{
		SoundRegistry.register();
		PacketRegistry.register();
		DamageTypes.register();
		
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
		ServerPlayNetworking.send(player, new OpenRandomCaptchaPayload(reason));
		CaptchaComponents.PLAYER.get(player).startCaptcha();
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
}