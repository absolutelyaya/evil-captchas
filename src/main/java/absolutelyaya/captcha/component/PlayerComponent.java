package absolutelyaya.captcha.component;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.registry.CaptchaLoot;
import absolutelyaya.captcha.registry.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import static absolutelyaya.captcha.CAPTCHA.config;

public class PlayerComponent implements IPlayerComponent
{
	static final RegistryKey<LootTable> REWARD_LOOT_TABLE = RegistryKey.of(RegistryKeys.LOOT_TABLE, CAPTCHA.identifier("reward"));
	final PlayerEntity provider;
	String currentCaptchaType;
	float localDifficulty = 0f, currentCaptchaDifficulty;
	int lives = -1;
	
	public PlayerComponent(PlayerEntity provider)
	{
		this.provider = provider;
	}
	
	@Override
	public void startCaptcha(String type, float difficulty)
	{
		if(provider.getWorld().isClient)
			return;
		if(config.lethal.getValue())
			lives = config.lives.getValue();
		currentCaptchaType = type;
		currentCaptchaDifficulty = difficulty;
		CaptchaComponents.PLAYER.sync(provider);
	}
	
	@Override
	public void finishCaptcha(boolean result, String type, float difficulty)
	{
		if(provider.getWorld().isClient)
			return;
		localDifficulty = Math.max(localDifficulty + (result ? 1f : -1f), 0f);
		
		if(!result)
		{
			if(config.lethal.getValue())
				lives--;
			if(lives <= 0)
			{
				provider.damage(DamageTypes.get(provider.getWorld(), DamageTypes.SKILL_ISSUE), 420);
				if(config.explosive.getValue())
					provider.getWorld().createExplosion(provider, provider.getX(), provider.getY(), provider.getZ(), 6.9f, World.ExplosionSourceType.MOB);
			}
		}
		else if(testRewardValidity(type, difficulty) && provider.getWorld().getServer() instanceof MinecraftServer server)
		{
			LootTable lootTable = server.getReloadableRegistries().getLootTable(REWARD_LOOT_TABLE);
			LootContextParameterSet.Builder builder =
					new LootContextParameterSet.Builder((ServerWorld)provider.getWorld())
							.add(CaptchaLoot.CAPTCHA_TYPE_PARAMETER, type)
							.add(CaptchaLoot.CAPTCHA_DIFFICULTY_PARAMETER, difficulty);
			LootContextParameterSet lootContextParameterSet = builder.build(LootContextTypes.ENTITY);
			lootTable.generateLoot(lootContextParameterSet, 0L, provider.getInventory()::insertStack);
		}
		currentCaptchaType = null;
		currentCaptchaDifficulty = 0f;
		CaptchaComponents.PLAYER.sync(provider);
	}
	
	boolean testRewardValidity(String type, float difficulty)
	{
		if(currentCaptchaType == null || !currentCaptchaType.equals(type))
			return false;
		return difficulty != currentCaptchaDifficulty;
	}
	
	@Override
	public float getLocalDifficulty()
	{
		return localDifficulty;
	}
	
	@Override
	public void setLocalDifficulty(float val)
	{
		localDifficulty = val;
		CaptchaComponents.PLAYER.sync(provider);
	}
	
	@Override
	public void resetLocalDifficulty()
	{
		setLocalDifficulty(0f);
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
		if(nbt.contains("currentType", NbtElement.STRING_TYPE))
			currentCaptchaType = nbt.getString("currentType");
		else
			currentCaptchaType = null;
		if(nbt.contains("currentDifficulty", NbtElement.FLOAT_TYPE))
			currentCaptchaDifficulty = nbt.getFloat("currentDifficulty");
		else
			currentCaptchaDifficulty = 0f;
	}
	
	@Override
	public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup)
	{
		nbt.putFloat("localDifficulty", localDifficulty);
		nbt.putInt("lives", lives);
		if(currentCaptchaType != null)
			nbt.putString("currentType", currentCaptchaType);
		if(currentCaptchaDifficulty != 0f)
			nbt.putFloat("currentDifficulty", currentCaptchaDifficulty);
	}
}
