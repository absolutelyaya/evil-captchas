package absolutelyaya.captcha.loot;

import absolutelyaya.captcha.registry.CaptchaLoot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;

public record CaptchaTypeCondition(String type) implements LootCondition
{
	public static final MapCodec<CaptchaTypeCondition> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(Codec.STRING.fieldOf("type").forGetter(CaptchaTypeCondition::type))
								.apply(instance, CaptchaTypeCondition::new));
	
	@Override
	public LootConditionType getType()
	{
		return CaptchaLoot.TYPE_CONDITION;
	}
	
	@Override
	public boolean test(LootContext lootContext)
	{
		String id = lootContext.get(CaptchaLoot.CAPTCHA_TYPE_PARAMETER);
		return type.equals(id);
	}
}
