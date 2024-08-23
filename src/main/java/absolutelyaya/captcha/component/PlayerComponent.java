package absolutelyaya.captcha.component;

import absolutelyaya.captcha.registry.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class PlayerComponent implements IPlayerComponent
{
	final PlayerEntity provider;
	float localDifficulty = 0f;
	int lives = -1;
	
	public PlayerComponent(PlayerEntity provider)
	{
		this.provider = provider;
	}
	
	@Override
	public void startCaptcha()
	{
		IConfigComponent config = CaptchaComponents.CONFIG.get(provider.getWorld().getScoreboard());
		if(config.isLethal())
			lives = config.getLives();
		CaptchaComponents.PLAYER.sync(provider);
	}
	
	@Override
	public void finishCaptcha(boolean result)
	{
		IConfigComponent config = CaptchaComponents.CONFIG.get(provider.getWorld().getScoreboard());
		localDifficulty = Math.max(localDifficulty + (result ? 1f : -1f), 0f);
		
		if(!result)
		{
			if(config.isLethal())
				lives--;
			if(lives <= 0)
			{
				provider.damage(DamageTypes.get(provider.getWorld(), DamageTypes.SKILL_ISSUE), 420);
				if(config.isExplosive())
					provider.getWorld().createExplosion(provider, provider.getX(), provider.getY(), provider.getZ(), 6.9f, World.ExplosionSourceType.MOB);
			}
		}
		CaptchaComponents.PLAYER.sync(provider);
	}
	
	@Override
	public float getLocalDifficulty()
	{
		return localDifficulty;
	}
	
	@Override
	public int getCurLives()
	{
		return lives;
	}
	
	@Override
	public void decrementLives()
	{
		lives--;
		CaptchaComponents.PLAYER.sync(provider);
	}
	
	@Override
	public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		if(nbt.contains("localDifficulty", NbtElement.FLOAT_TYPE))
			localDifficulty = nbt.getFloat("localDifficulty");
		if(lives != -1 && nbt.contains("lives", NbtElement.INT_TYPE))
			lives = nbt.getInt("lives");
		else
			lives = 3;
	}
	
	@Override
	public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		nbt.putFloat("localDifficulty", localDifficulty);
		nbt.putInt("lives", lives);
	}
}
