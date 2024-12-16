package absolutelyaya.captcha.registry;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.entity.SlimerEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class EntityRegistry
{
	public static final EntityType<SlimerEntity> SLIMER = Registry.register(Registries.ENTITY_TYPE, CAPTCHA.identifier("slimer"),
			EntityType.Builder.create(SlimerEntity::new, SpawnGroup.MISC).disableSummon().build("slimer"));
	
	public static void register()
	{
		FabricDefaultAttributeRegistry.register(SLIMER, SlimerEntity.createMobAttributes());
	}
}
