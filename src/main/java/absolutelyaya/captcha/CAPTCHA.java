package absolutelyaya.captcha;

import absolutelyaya.captcha.networking.PacketRegistry;
import absolutelyaya.captcha.registry.Commands;
import absolutelyaya.captcha.registry.SoundRegistry;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			Commands.register(dispatcher);
		});
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
}