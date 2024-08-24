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

public class MultiBoxCaptchaPoolManager extends JsonDataLoader
{
	private static final Random random = Random.create();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static ImmutableMap<Identifier, MultiBoxCaptchaPool> ALL_POOLS = ImmutableMap.of();
	
	public MultiBoxCaptchaPoolManager()
	{
		super(GSON, "captcha/boxes/multi");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return CAPTCHA.identifier("captcha/boxes/multi");
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
		ImmutableMap.Builder<Identifier, MultiBoxCaptchaPool> builder = new ImmutableMap.Builder<>();
		prepared.forEach((id, element) -> builder.put(id, MultiBoxCaptchaPool.deserialize(element.getAsJsonObject())));
		ALL_POOLS = builder.build();
	}
	
	public static MultiBoxCaptchaPool getRandom(float difficulty)
	{
		if(ALL_POOLS == null || ALL_POOLS.values().isEmpty())
			return null;
		List<MultiBoxCaptchaPool> candidates = new ArrayList<>();
		for(MultiBoxCaptchaPool pool : ALL_POOLS.values())
			if(difficulty >= pool.difficulty())
				candidates.add(pool);
		return candidates.get(random.nextInt(candidates.size()));
	}
	
	public static NbtCompound compileToSyncData()
	{
		NbtCompound nbt = new NbtCompound();
		for (Map.Entry<Identifier, MultiBoxCaptchaPool> entry : ALL_POOLS.entrySet())
			nbt.put(entry.getKey().toString(), entry.getValue().serialize());
		return nbt;
	}
	
	public static void applySyncData(NbtCompound nbt)
	{
		ImmutableMap.Builder<Identifier, MultiBoxCaptchaPool> builder = new ImmutableMap.Builder<>();
		nbt.getKeys().forEach(key -> builder.put(Identifier.tryParse(key), MultiBoxCaptchaPool.deserialize(nbt.getCompound(key))));
		ALL_POOLS = builder.build();
		CAPTCHA.LOGGER.info("received {} multi-box captcha pools", ALL_POOLS.size());
	}
}
