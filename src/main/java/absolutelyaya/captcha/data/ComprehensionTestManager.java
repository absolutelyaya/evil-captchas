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

public class ComprehensionTestManager extends JsonDataLoader
{
	private static final Random random = Random.create();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static ImmutableMap<Identifier, ComprehensionObjectData> ALL_OBJECTS = ImmutableMap.of();
	private static ImmutableMap<Identifier, ComprehensionAdjectiveData> ALL_ADJECTIVES = ImmutableMap.of();
	
	public ComprehensionTestManager()
	{
		super(GSON, "captcha/comprehension");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return CAPTCHA.identifier("captcha/comprehension/objects");
			}
			
			@Override
			public void reload(ResourceManager manager)
			{
				applyObjects(prepare("captcha/comprehension/objects", manager, DummyProfiler.INSTANCE), manager, DummyProfiler.INSTANCE);
			}
		});
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return CAPTCHA.identifier("captcha/comprehension/adjectives");
			}
			
			@Override
			public void reload(ResourceManager manager)
			{
				applyAdjectives(prepare("captcha/comprehension/adjectives", manager, DummyProfiler.INSTANCE), manager, DummyProfiler.INSTANCE);
			}
		});
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler)
	{
	}
	
	
	protected Map<Identifier, JsonElement> prepare(String dataType, ResourceManager resourceManager, Profiler profiler)
	{
		Map<Identifier, JsonElement> prepared = new HashMap<>();
		load(resourceManager, dataType, GSON, prepared);
		return prepared;
	}
	
	protected void applyObjects(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler)
	{
		ImmutableMap.Builder<Identifier, ComprehensionObjectData> builder = new ImmutableMap.Builder<>();
		prepared.forEach((id, element) -> {
			JsonObject json = element.getAsJsonObject();
			String texture = JsonHelper.getString(json, "texture");
			String name = JsonHelper.getString(json, "name");
			float difficulty = JsonHelper.getFloat(json, "difficulty");
			builder.put(id, new ComprehensionObjectData(texture, name, difficulty));
		});
		ALL_OBJECTS = builder.build();
	}
	
	protected void applyAdjectives(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler)
	{
		ImmutableMap.Builder<Identifier, ComprehensionAdjectiveData> builder = new ImmutableMap.Builder<>();
		prepared.forEach((id, element) -> {
			JsonObject json = element.getAsJsonObject();
			String name = JsonHelper.getString(json, "name");
			float difficulty = JsonHelper.getFloat(json, "difficulty");
			int color = -1;
			if(json.has("color"))
				color = JsonHelper.getInt(json, "color");
			boolean shaking = false;
			if(json.has("shaking"))
				shaking = JsonHelper.getBoolean(json, "shaking");
			float scale = 1f;
			if(json.has("scale"))
				scale = JsonHelper.getFloat(json, "scale");
			int glowColor = -1;
			if(json.has("glow-color"))
				glowColor = JsonHelper.getInt(json, "glow-color");
			builder.put(id, new ComprehensionAdjectiveData(name, difficulty, color, scale, shaking, glowColor));
		});
		ALL_ADJECTIVES = builder.build();
	}
	
	public static ComprehensionObjectData getRandomObject(float difficulty)
	{
		List<ComprehensionObjectData> candidates = new ArrayList<>();
		for (ComprehensionObjectData object : ALL_OBJECTS.values())
			if(difficulty >= object.difficulty())
				candidates.add(object);
		return candidates.get(random.nextInt(candidates.size()));
	}
	
	public static ComprehensionAdjectiveData getRandomAdjective(float difficulty)
	{
		List<ComprehensionAdjectiveData> candidates = new ArrayList<>();
		for (ComprehensionAdjectiveData adjective : ALL_ADJECTIVES.values())
			if(difficulty >= adjective.difficulty())
				candidates.add(adjective);
		return candidates.get(random.nextInt(candidates.size()));
	}
	
	public static ComprehensionAdjectiveData getRandomNonColorAdjective(float difficulty)
	{
		List<ComprehensionAdjectiveData> candidates = new ArrayList<>();
		for (ComprehensionAdjectiveData adjective : ALL_ADJECTIVES.values())
			if(!adjective.isColor() && difficulty >= adjective.difficulty())
				candidates.add(adjective);
		return candidates.get(random.nextInt(candidates.size()));
	}
	
	public static ComprehensionAdjectiveData getRandomColor(float difficulty)
	{
		List<ComprehensionAdjectiveData> candidates = new ArrayList<>();
		for (ComprehensionAdjectiveData adjective : ALL_ADJECTIVES.values())
			if(adjective.isColor() && difficulty >= adjective.difficulty())
				candidates.add(adjective);
		return candidates.get(random.nextInt(candidates.size()));
	}
}
