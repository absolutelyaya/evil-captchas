package absolutelyaya.captcha.loot;

import absolutelyaya.captcha.registry.CaptchaLoot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;

public record CaptchaDifficultyCondition(float difficulty) implements LootCondition
{
	public static final MapCodec<CaptchaDifficultyCondition> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(Codec.FLOAT.fieldOf("difficulty").forGetter(CaptchaDifficultyCondition::difficulty))
								.apply(instance, CaptchaDifficultyCondition::new));
	
	@Override
	public LootConditionType getType()
	{
		return CaptchaLoot.DIFFICULTY_CONDITION;
	}
	
	@Override
	public boolean test(LootContext lootContext)
	{
		Float difficulty = lootContext.get(CaptchaLoot.CAPTCHA_DIFFICULTY_PARAMETER);
		if(difficulty == null)
			return false;
		return difficulty >= this.difficulty;
	}
}
