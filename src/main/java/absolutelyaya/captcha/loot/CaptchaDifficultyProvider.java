package absolutelyaya.captcha.loot;

import absolutelyaya.captcha.registry.CaptchaLoot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;

import java.util.Optional;

public record CaptchaDifficultyProvider(float difficulty, Optional<Float> factor) implements LootNumberProvider
{
	public static final MapCodec<CaptchaDifficultyProvider> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					Codec.FLOAT.fieldOf("diffulty").forGetter(CaptchaDifficultyProvider::difficulty),
					Codec.FLOAT.optionalFieldOf("diffulty").forGetter(CaptchaDifficultyProvider::factor)
					).apply(instance, CaptchaDifficultyProvider::new));
	
	@Override
	public float nextFloat(LootContext context)
	{
		Float difficulty = context.get(CaptchaLoot.CAPTCHA_DIFFICULTY_PARAMETER);
		if(difficulty == null)
			return 0;
		if(factor.isPresent())
			difficulty /= factor.get();
		return difficulty;
	}
	
	@Override
	public LootNumberProviderType getType()
	{
		return CaptchaLoot.CAPTCHA_DIFFICULTY_PROVIDER;
	}
}
