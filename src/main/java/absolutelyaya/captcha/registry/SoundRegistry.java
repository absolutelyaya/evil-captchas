package absolutelyaya.captcha.registry;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundRegistry
{
	public static final SoundEvent WRONG_BUZZER = register("wrong_buzzer");
	
	public static void register()
	{
	
	}
	
	private static SoundEvent register(String id)
	{
		Identifier identifier = CAPTCHA.identifier(id);
		return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
	}
}
