package absolutelyaya.captcha.component;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import static absolutelyaya.captcha.CAPTCHA.config;

public class ConfigComponent implements IConfigComponent
{
	final Scoreboard provider;
	int constantIncreaseTimer;
	float curDifficulty = 5f;
	
	public ConfigComponent(Scoreboard provider, @Nullable MinecraftServer ignored)
	{
		this.provider = provider;
	}
	
	@Override
	public void setCurDifficulty(float val)
	{
		curDifficulty = val;
	}
	
	@Override
	public float getCurDifficulty()
	{
		return curDifficulty;
	}
	
	@Override
	public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		if(!(nbt.contains("yayConfig", NbtElement.BYTE_TYPE) && nbt.getBoolean("yayConfig")))
		{
			if(nbt.contains("lethal", NbtElement.BYTE_TYPE))
				config.lethal.setValue(nbt.getBoolean("lethal"));
			if(nbt.contains("explosive", NbtElement.BYTE_TYPE))
				config.explosive.setValue(nbt.getBoolean("explosive"));
			if(nbt.contains("validationExpiration", NbtElement.BYTE_TYPE))
				config.validationExpiration.setValue(nbt.getBoolean("validationExpiration"));
			if(nbt.contains("notEasy", NbtElement.BYTE_TYPE))
				config.notEasy.setValue(nbt.getBoolean("notEasy"));
			if(nbt.contains("lives", NbtElement.INT_TYPE))
				config.lives.setValue(nbt.getInt("lives"));
			if(nbt.contains("minExpirationDelay", NbtElement.INT_TYPE))
				config.expirationDelayMin.setValue(nbt.getInt("minExpirationDelay"));
			if(nbt.contains("maxExpirationDelay", NbtElement.INT_TYPE))
				config.expirationDelayMax.setValue(nbt.getInt("maxExpirationDelay"));
			if(nbt.contains("constantIncreaseRate", NbtElement.FLOAT_TYPE))
				config.constantIncreaseRate.setValue(nbt.getFloat("constantIncreaseRate"));
		}
		if(nbt.contains("curDifficulty", NbtElement.FLOAT_TYPE))
			setCurDifficulty(nbt.getFloat("curDifficulty"));
	}
	
	@Override
	public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		nbt.putBoolean("yayConfig", true);
		//nbt.putBoolean("lethal", isLethal());
		//nbt.putBoolean("explosive", isExplosive());
		//nbt.putBoolean("validationExpiration", isValidationExpiration());
		//nbt.putBoolean("notEasy", isNotEasy());
		//nbt.putInt("lives", getLives());
		//nbt.putInt("minExpirationDelay", getMinExpirationDelay());
		//nbt.putInt("maxExpirationDelay", getMaxExpirationDelay());
		//nbt.putFloat("constantIncrease", getConstantIncreaseRate());
		
		nbt.putFloat("curDifficulty", getCurDifficulty());
	}
	
	@Override
	public void serverTick()
	{
		if(constantIncreaseTimer-- <= 0)
		{
			curDifficulty += config.constantIncreaseRate.getValue();
			constantIncreaseTimer = config.constantIncreaseInterval.getValue();
			CaptchaComponents.CONFIG.sync(provider);
		}
	}
}
