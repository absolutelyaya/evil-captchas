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
		prepared.forEach((id, element) -> {
			JsonObject json = element.getAsJsonObject();
			float difficulty = JsonHelper.getFloat(json, "difficulty");
			List<Identifier> crewmates = new ArrayList<>();
			JsonHelper.getArray(json, "crewmates").forEach(i -> crewmates.add(CAPTCHA.texIdentifier(i.getAsString())));
			List<Identifier> impostors = new ArrayList<>();
			JsonHelper.getArray(json, "impostors").forEach(i -> impostors.add(CAPTCHA.texIdentifier(i.getAsString())));
			builder.put(id, new AmongusPool(difficulty, crewmates, impostors));
		});
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
}
