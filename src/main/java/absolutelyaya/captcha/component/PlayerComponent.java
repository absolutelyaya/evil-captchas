package absolutelyaya.captcha.component;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.data.InvoluntaryAddon;
import absolutelyaya.captcha.registry.CaptchaLoot;
import absolutelyaya.captcha.registry.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static absolutelyaya.captcha.CAPTCHA.config;

public class PlayerComponent implements IPlayerComponent
{
	static final RegistryKey<LootTable> REWARD_LOOT_TABLE = RegistryKey.of(RegistryKeys.LOOT_TABLE, CAPTCHA.identifier("gameplay/captcha"));
	List<InvoluntaryAddon> involuntaryAddons = new ArrayList<>(), addedAddons = new ArrayList<>(), removedAddons = new ArrayList<>();
	final PlayerEntity provider;
	String currentCaptchaType;
	float localDifficulty = 0f, currentCaptchaDifficulty;
	int lives = -1, lastAddonCount = -1;
	BlockPos storedContainer;
	
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
				storedContainer = null;
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
			LootContextParameterSet lootContextParameterSet = builder.build(CaptchaLoot.CAPTCHA_LOOT_CONTEXT);
			lootTable.generateLoot(lootContextParameterSet, 0L, provider.getInventory()::insertStack);
		}
		currentCaptchaType = null;
		currentCaptchaDifficulty = 0f;
		if(result && storedContainer != null)
		{
			if(provider.getWorld().getBlockEntity(storedContainer) instanceof LootableInventory inv)
			{
				if(inv.getLootTable() != null)
					inv.generateLoot(provider);
				provider.getWorld().getBlockState(storedContainer)
						.onUse(provider.getWorld(), provider, new BlockHitResult(storedContainer.toCenterPos(), Direction.UP, storedContainer, true));
			}
			storedContainer = null;
		}
		CaptchaComponents.PLAYER.sync(provider);
	}
	
	boolean testRewardValidity(String type, float difficulty)
	{
		if(!config.captchaRewards.getValue())
			return false;
		if(currentCaptchaType == null || !currentCaptchaType.equals(type))
			return false;
		return difficulty == currentCaptchaDifficulty;
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
	public void storeLootContainer(BlockPos pos)
	{
		storedContainer = pos;
	}
	
	@Override
	public void addInvoluntaryAddon(InvoluntaryAddon addon)
	{
		addedAddons.add(addon);
	}
	
	@Override
	public void addInvoluntaryAddon(String type, float difficulty)
	{
		Random random = provider.getRandom();
		addInvoluntaryAddon(new InvoluntaryAddon(type,
				System.currentTimeMillis() + (int)(180f + random.nextFloat() * 120f + random.nextFloat() * difficulty / 100f) * 1000,
				random.nextFloat(), random.nextFloat()));
	}
	
	@Override
	public void removeInvoluntaryAddon(InvoluntaryAddon addon)
	{
		removedAddons.add(addon);
	}
	
	@Override
	public List<InvoluntaryAddon> getAddons()
	{
		return involuntaryAddons;
	}
	
	@Override
	public boolean hasAddon(String type)
	{
		for (InvoluntaryAddon addon : involuntaryAddons)
			if(addon.type().equals(type))
				return true;
		return false;
	}
	
	@Override
	public InvoluntaryAddon getAddon(String type)
	{
		for (InvoluntaryAddon addon : involuntaryAddons)
			if(addon.type().equals(type))
				return addon;
		return null;
	}
	
	@Override
	public void tick()
	{
		int lastCount = involuntaryAddons.size();
		for (InvoluntaryAddon addon : involuntaryAddons)
			if(System.currentTimeMillis() > addon.activeUntil())
				removedAddons.add(addon);
		
		for (InvoluntaryAddon addon : removedAddons)
		{
			if(!provider.getWorld().isClient)
				provider.sendMessage(Text.translatable("captcha.message.addon-remove", Text.translatable("captcha.addon." + addon.type())));
			involuntaryAddons.remove(addon);
		}
		removedAddons.clear();
		for (InvoluntaryAddon addon : addedAddons)
		{
			if(!provider.getWorld().isClient)
				provider.sendMessage(Text.translatable("captcha.message.addon-add", Text.translatable("captcha.addon." + addon.type())));
			involuntaryAddons.add(addon);
		}
		addedAddons.clear();
		if(lastCount != involuntaryAddons.size())
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
		if(nbt.contains("involuntaryAddons", NbtElement.LIST_TYPE))
		{
			involuntaryAddons.clear();
			for (NbtElement element : nbt.getList("involuntaryAddons", NbtElement.COMPOUND_TYPE))
				if(element instanceof NbtCompound compound)
					involuntaryAddons.add(InvoluntaryAddon.deserialize(compound));
		}
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
		if(lastAddonCount != involuntaryAddons.size())
		{
			NbtList addons = new NbtList();
			for (InvoluntaryAddon addon : involuntaryAddons)
				addons.add(addon.serialize());
			nbt.put("involuntaryAddons", addons);
			lastAddonCount = involuntaryAddons.size();
		}
	}
}
