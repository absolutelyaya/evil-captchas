package absolutelyaya.captcha.registry;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.loot.CaptchaDifficultyCondition;
import absolutelyaya.captcha.loot.CaptchaDifficultyProvider;
import absolutelyaya.captcha.loot.CaptchaTypeCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class CaptchaLoot
{
	public static final LootConditionType DIFFICULTY_CONDITION =
			Registry.register(Registries.LOOT_CONDITION_TYPE, CAPTCHA.identifier("difficulty"),
					new LootConditionType(CaptchaDifficultyCondition.CODEC));
	public static final LootConditionType TYPE_CONDITION =
			Registry.register(Registries.LOOT_CONDITION_TYPE, CAPTCHA.identifier("type"),
					new LootConditionType(CaptchaTypeCondition.CODEC));
	
	public static final LootNumberProviderType CAPTCHA_DIFFICULTY_PROVIDER = new LootNumberProviderType(CaptchaDifficultyProvider.CODEC);
	
	public static final LootContextParameter<Float> CAPTCHA_DIFFICULTY_PARAMETER = new LootContextParameter<>(CAPTCHA.identifier("difficulty"));
	public static final LootContextParameter<String> CAPTCHA_TYPE_PARAMETER = new LootContextParameter<>(CAPTCHA.identifier("type"));
	
	public static final LootContextType CAPTCHA_LOOT_CONTEXT = new LootContextType.Builder()
																	   .require(CAPTCHA_DIFFICULTY_PARAMETER)
																	   .require(CAPTCHA_TYPE_PARAMETER).build();
	
	public static void register()
	{
	
	}
}
