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
				applyObjects(prepare("captcha/comprehension/objects", manager));
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
				applyAdjectives(prepare("captcha/comprehension/adjectives", manager));
			}
		});
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {}
	
	protected Map<Identifier, JsonElement> prepare(String dataType, ResourceManager resourceManager)
	{
		Map<Identifier, JsonElement> prepared = new HashMap<>();
		load(resourceManager, dataType, GSON, prepared);
		return prepared;
	}
	
	protected void applyObjects(Map<Identifier, JsonElement> prepared)
	{
		ImmutableMap.Builder<Identifier, ComprehensionObjectData> builder = new ImmutableMap.Builder<>();
		prepared.forEach((id, element) -> builder.put(id, ComprehensionObjectData.deserialize(element.getAsJsonObject())));
		ALL_OBJECTS = builder.build();
	}
	
	protected void applyAdjectives(Map<Identifier, JsonElement> prepared)
	{
		ImmutableMap.Builder<Identifier, ComprehensionAdjectiveData> builder = new ImmutableMap.Builder<>();
		prepared.forEach((id, element) -> builder.put(id, ComprehensionAdjectiveData.deserialize(element.getAsJsonObject())));
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
	
	public static NbtCompound compileToSyncData()
	{
		NbtCompound nbt = new NbtCompound();
		NbtCompound objects = new NbtCompound();
		for (Map.Entry<Identifier, ComprehensionObjectData> entry : ALL_OBJECTS.entrySet())
			objects.put(entry.getKey().toString(), entry.getValue().serialize());
		nbt.put("objects", objects);
		NbtCompound adjectives = new NbtCompound();
		for (Map.Entry<Identifier, ComprehensionAdjectiveData> entry : ALL_ADJECTIVES.entrySet())
			adjectives.put(entry.getKey().toString(), entry.getValue().serialize());
		nbt.put("adjectives", adjectives);
		return nbt;
	}
	
	public static void applySyncData(NbtCompound nbt)
	{
		ImmutableMap.Builder<Identifier, ComprehensionObjectData> objectBuilder = new ImmutableMap.Builder<>();
		NbtCompound objects = nbt.getCompound("objects");
		objects.getKeys().forEach(key -> objectBuilder.put(Identifier.tryParse(key), ComprehensionObjectData.deserialize(objects.getCompound(key))));
		ALL_OBJECTS = objectBuilder.build();
		ImmutableMap.Builder<Identifier, ComprehensionAdjectiveData> adjectiveBuilder = new ImmutableMap.Builder<>();
		NbtCompound adjectives = nbt.getCompound("adjectives");
		adjectives.getKeys().forEach(key -> adjectiveBuilder.put(Identifier.tryParse(key), ComprehensionAdjectiveData.deserialize(adjectives.getCompound(key))));
		ALL_ADJECTIVES = adjectiveBuilder.build();
		CAPTCHA.LOGGER.info("received {} comprehension objects and {} comprehension adjectives", ALL_OBJECTS.size(), ALL_ADJECTIVES.size());
	}
}
