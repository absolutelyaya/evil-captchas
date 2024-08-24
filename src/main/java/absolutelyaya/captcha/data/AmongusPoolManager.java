package absolutelyaya.captcha.data;

import absolutelyaya.captcha.CAPTCHA;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AmongusPoolManager extends JsonDataLoader
{
	private static final Random random = Random.create();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static ImmutableMap<Identifier, AmongusPool> ALL_POOLS = ImmutableMap.of();
	
	public AmongusPoolManager()
	{
		super(GSON, "captcha/amongus");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return CAPTCHA.identifier("captcha/amongus");
			}
			
			@Override
			public void reload(ResourceManager manager)
			{
				apply(prepare(manager, DummyProfiler.INSTANCE), manager, DummyProfiler.INSTANCE);
			}
		});
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler)
	{
		ImmutableMap.Builder<Identifier, AmongusPool> builder = new ImmutableMap.Builder<>();
		prepared.forEach((id, element) -> builder.put(id, AmongusPool.deserialize(element.getAsJsonObject())));
		ALL_POOLS = builder.build();
	}
	
	public static AmongusPool getRandomPool(float difficulty)
	{
		List<AmongusPool> candidates = new ArrayList<>();
		for (AmongusPool pool : ALL_POOLS.values())
			if(difficulty >= pool.difficulty())
				candidates.add(pool);
		return candidates.get(random.nextInt(candidates.size()));
	}
	
	public static NbtCompound compileToSyncData()
	{
		NbtCompound nbt = new NbtCompound();
		for (Map.Entry<Identifier, AmongusPool> entry : ALL_POOLS.entrySet())
			nbt.put(entry.getKey().toString(), entry.getValue().serialize());
		return nbt;
	}
	
	public static void applySyncData(NbtCompound nbt)
	{
		ImmutableMap.Builder<Identifier, AmongusPool> builder = new ImmutableMap.Builder<>();
		nbt.getKeys().forEach(key -> builder.put(Identifier.tryParse(key), AmongusPool.deserialize(nbt.getCompound(key))));
		ALL_POOLS = builder.build();
	}
}
