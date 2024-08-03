package absolutelyaya.captcha;

import absolutelyaya.captcha.registry.SoundRegistry;
import net.fabricmc.api.ModInitializer;

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
	}
	
	public static Identifier identifier(String path)
	{
		return Identifier.of(MOD_ID, path);
	}
	
	public static Identifier texIdentifier(String path)
	{
		return Identifier.of(MOD_ID, "textures/" + path + ".png");
	}
}