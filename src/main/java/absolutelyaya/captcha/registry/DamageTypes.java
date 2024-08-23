package absolutelyaya.captcha.registry;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class DamageTypes
{
	public static final RegistryKey<DamageType> SKILL_ISSUE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, CAPTCHA.identifier("skill_issue"));
	
	public static DamageSource get(World world, RegistryKey<DamageType> type)
	{
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type));
	}
	
	public static void register()
	{
	
	}
}
