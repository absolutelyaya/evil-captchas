package absolutelyaya.captcha.data;

import absolutelyaya.captcha.CAPTCHA;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SingleBoxCaptchaDataManager extends JsonDataLoader
{
	private static final Random random = Random.create();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static ImmutableMap<Identifier, SingleBoxCaptchaData> ALL_BOXES = ImmutableMap.of();
	
	public SingleBoxCaptchaDataManager()
	{
		super(GSON, "captcha/boxes/single");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return CAPTCHA.identifier("captcha/boxes/single");
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
		ImmutableMap.Builder<Identifier, SingleBoxCaptchaData> builder = new ImmutableMap.Builder<>();
		prepared.forEach((id, element) -> {
			builder.put(id, SingleBoxCaptchaData.deserialize(element.getAsJsonObject()));
		});
		ALL_BOXES = builder.build();
	}
	
	public static SingleBoxCaptchaData getRandom(float difficulty)
	{
		if(ALL_BOXES == null || ALL_BOXES.values().isEmpty())
			return null;
		List<SingleBoxCaptchaData> candidates = new ArrayList<>();
		for(SingleBoxCaptchaData box : ALL_BOXES.values())
			if(difficulty >= box.difficulty())
				candidates.add(box);
		return candidates.get(random.nextInt(candidates.size()));
	}
	
	public static NbtCompound compileToSyncData()
	{
		NbtCompound nbt = new NbtCompound();
		for (Map.Entry<Identifier, SingleBoxCaptchaData> entry : ALL_BOXES.entrySet())
			nbt.put(entry.getKey().toString(), entry.getValue().serialize());
		return nbt;
	}
	
	public static void applySyncData(NbtCompound nbt)
	{
		ImmutableMap.Builder<Identifier, SingleBoxCaptchaData> builder = new ImmutableMap.Builder<>();
		nbt.getKeys().forEach(key -> builder.put(Identifier.tryParse(key), SingleBoxCaptchaData.deserialize(nbt.getCompound(key))));
		ALL_BOXES = builder.build();
		CAPTCHA.LOGGER.info("received {} single-box captcha pools", ALL_BOXES.size());
	}
}
