package absolutelyaya.captcha.component;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public class ConfigComponent implements IConfigComponent
{
	final Scoreboard provider;
	boolean lethal, explosive, validationExpiration = true, notEasy;
	int lives = 3, constantIncreaseTimer, expirationDelayMin = 120, expirationDelayMax = 120;
	float curDifficulty = 5f, constantIncreaseRate = 0.0005f;
	
	public ConfigComponent(Scoreboard provider, @Nullable MinecraftServer ignored)
	{
		this.provider = provider;
	}
	
	@Override
	public void setLethal(boolean b)
	{
		lethal = b;
	}
	
	@Override
	public boolean isLethal()
	{
		return lethal;
	}
	
	@Override
	public void setExplosive(boolean b)
	{
		explosive = b;
	}
	
	@Override
	public boolean isExplosive()
	{
		return explosive;
	}
	
	@Override
	public void setLives(int val)
	{
		lives = val;
	}
	
	@Override
	public int getLives()
	{
		return lives;
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
	public void setConstantIncreaseRate(float val)
	{
		constantIncreaseRate = val;
	}
	
	@Override
	public float getConstantIncreaseRate()
	{
		return constantIncreaseRate;
	}
	
	@Override
	public void setValidationExpiration(boolean b)
	{
		validationExpiration = b;
	}
	
	@Override
	public boolean isValidationExpiration()
	{
		return validationExpiration;
	}
	
	@Override
	public void setMinExpirationDelay(int val)
	{
		expirationDelayMin = val;
	}
	
	@Override
	public int getMinExpirationDelay()
	{
		return expirationDelayMin;
	}
	
	@Override
	public void setMaxExpirationDelay(int val)
	{
		expirationDelayMax = val;
	}
	
	@Override
	public int getMaxExpirationDelay()
	{
		return expirationDelayMax;
	}
	
	@Override
	public void setNotEasy(boolean b)
	{
		notEasy = b;
	}
	
	@Override
	public boolean isNotEasy()
	{
		return notEasy;
	}
	
	@Override
	public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		if(nbt.contains("lethal", NbtElement.BYTE_TYPE))
			setLethal(nbt.getBoolean("lethal"));
		if(nbt.contains("explosive", NbtElement.BYTE_TYPE))
			setExplosive(nbt.getBoolean("explosive"));
		if(nbt.contains("validationExpiration", NbtElement.BYTE_TYPE))
			setValidationExpiration(nbt.getBoolean("validationExpiration"));
		if(nbt.contains("notEasy", NbtElement.BYTE_TYPE))
			setNotEasy(nbt.getBoolean("notEasy"));
		if(nbt.contains("lives", NbtElement.INT_TYPE))
			setLives(nbt.getInt("lives"));
		if(nbt.contains("minExpirationDelay", NbtElement.INT_TYPE))
			setMinExpirationDelay(nbt.getInt("minExpirationDelay"));
		if(nbt.contains("maxExpirationDelay", NbtElement.INT_TYPE))
			setMaxExpirationDelay(nbt.getInt("maxExpirationDelay"));
		if(nbt.contains("curDifficulty", NbtElement.FLOAT_TYPE))
			setCurDifficulty(nbt.getFloat("curDifficulty"));
		if(nbt.contains("constantIncreaseRate", NbtElement.FLOAT_TYPE))
			setConstantIncreaseRate(nbt.getFloat("constantIncreaseRate"));
	}
	
	@Override
	public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		nbt.putBoolean("lethal", isLethal());
		nbt.putBoolean("explosive", isExplosive());
		nbt.putBoolean("validationExpiration", isValidationExpiration());
		nbt.putBoolean("notEasy", isNotEasy());
		nbt.putInt("lives", getLives());
		nbt.putInt("minExpirationDelay", getMinExpirationDelay());
		nbt.putInt("maxExpirationDelay", getMaxExpirationDelay());
		nbt.putFloat("curDifficulty", getCurDifficulty());
		nbt.putFloat("constantIncrease", getConstantIncreaseRate());
	}
	
	@Override
	public void serverTick()
	{
		if(constantIncreaseTimer-- <= 0)
		{
			curDifficulty += getConstantIncreaseRate();
			CaptchaComponents.CONFIG.sync(provider);
		}
	}
}
