package absolutelyaya.captcha.data;

import absolutelyaya.captcha.CAPTCHA;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageSearchCaptchaPoolManager extends JsonDataLoader
{
	private static final Random random = Random.create();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static ImmutableMap<Identifier, ImageSearchCaptchaPool> ALL_POOLS = ImmutableMap.of();
	
	public ImageSearchCaptchaPoolManager()
	{
		super(GSON, "captcha/image-search");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return CAPTCHA.identifier("captcha/image-search");
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
		ImmutableMap.Builder<Identifier, ImageSearchCaptchaPool> builder = new ImmutableMap.Builder<>();
		prepared.forEach((id, element) -> {
			JsonObject json = element.getAsJsonObject();
			float difficulty = JsonHelper.getFloat(json, "difficulty");
			List<String> backgrounds = new ArrayList<>();
			JsonHelper.getArray(json, "backgrounds").forEach(i -> {
				String val = i.getAsString();
				if(!backgrounds.contains(val))
					backgrounds.add(val);
			});
			List<String> overlays = new ArrayList<>();
			JsonHelper.getArray(json, "overlays").forEach(i -> {
				String val = i.getAsString();
				if(!overlays.contains(val))
					overlays.add(val);
			});
			Map<String, String> objects = new HashMap<>();
			JsonHelper.getArray(json, "objects").forEach(i -> {
				String prompt = JsonHelper.getString(i.getAsJsonObject(), "prompt");
				String texture = JsonHelper.getString(i.getAsJsonObject(), "texture");
				if(!objects.containsKey(prompt))
					objects.put(prompt, texture);
			});
			builder.put(id, new ImageSearchCaptchaPool(difficulty, backgrounds, overlays, objects));
		});
		ALL_POOLS = builder.build();
	}
	
	public static ImageSearchCaptchaPool getRandom(float difficulty)
	{
		if(ALL_POOLS == null || ALL_POOLS.values().isEmpty())
			return null;
		List<ImageSearchCaptchaPool> candidates = new ArrayList<>();
		for(ImageSearchCaptchaPool pool : ALL_POOLS.values())
			if(Math.abs(pool.difficulty() - difficulty) <= 5f)
				candidates.add(pool);
		if(candidates.isEmpty())
			return ALL_POOLS.values().toArray(ImageSearchCaptchaPool[]::new)[random.nextInt(ALL_POOLS.size())];
		return candidates.get(random.nextInt(candidates.size()));
	}
}
