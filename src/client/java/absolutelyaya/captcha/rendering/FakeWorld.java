package absolutelyaya.captcha.rendering;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.dimension.DimensionType;

import java.util.OptionalLong;

public class FakeWorld extends ClientWorld
{
	public FakeWorld(MinecraftClient client)
	{
		super(client.getNetworkHandler(), new Properties(Difficulty.EASY, false, true), null,
				RegistryEntry.of(new DimensionType(OptionalLong.of(0), true, false ,false ,false,
						1, false, true, 0, 32, 0, BlockTags.INFINIBURN_OVERWORLD,
						CAPTCHA.identifier("fakeworld"), 1, new DimensionType.MonsterSettings(false, false, ConstantIntProvider.create(1), 0))),
				1, 1, null, client.worldRenderer, false, 0);
	}
}
