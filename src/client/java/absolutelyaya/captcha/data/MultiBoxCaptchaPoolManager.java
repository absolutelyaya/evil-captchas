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
		prepared.forEach((id, element) -> {
			JsonObject json = element.getAsJsonObject();
			String promptKey = JsonHelper.getString(json, "promptKey");
			float difficulty = JsonHelper.getFloat(json, "difficulty");
			List<String> textureList = new ArrayList<>();
			JsonHelper.getArray(json, "values").forEach(i -> {
				String val = i.getAsString();
				if(!textureList.contains(val))
					textureList.add(val);
			});
			builder.put(id, new MultiBoxCaptchaPool(promptKey, difficulty, textureList));
		});
		ALL_POOLS = builder.build();
	}
	
	public static MultiBoxCaptchaPool getRandom(float difficulty)
	{
		if(ALL_POOLS == null || ALL_POOLS.values().isEmpty())
			return null;
		List<MultiBoxCaptchaPool> candidates = new ArrayList<>();
		for(MultiBoxCaptchaPool pool : ALL_POOLS.values())
			if(Math.abs(pool.difficulty() - difficulty) <= 5f)
				candidates.add(pool);
		return candidates.get(random.nextInt(candidates.size()));
	}
}
