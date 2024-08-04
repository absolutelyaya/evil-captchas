package absolutelyaya.captcha.data;

import absolutelyaya.captcha.CAPTCHA;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
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

public class BoxCaptchaDataManager extends JsonDataLoader
{
	private static final Random random = Random.create();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static ImmutableMap<Identifier, BoxCaptchaData> ALL_BOXES = ImmutableMap.of();
	
	public BoxCaptchaDataManager()
	{
		super(GSON, "captcha/boxes");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return CAPTCHA.identifier("captcha/boxes");
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
		ImmutableMap.Builder<Identifier, BoxCaptchaData> builder = new ImmutableMap.Builder<>();
		prepared.forEach((id, element) -> {
			JsonObject json = element.getAsJsonObject();
			String texture = JsonHelper.getString(json, "texture");
			float difficulty = JsonHelper.getFloat(json, "difficulty");
			int subdivisions = JsonHelper.getInt(json, "subdivisions");
			List<List<String>> valueList = new ArrayList<>();
			List<String> promptList = new ArrayList<>();
			JsonHelper.getArray(json, "values").forEach(i -> {
				List<String> subList = new ArrayList<>();
				i.getAsJsonArray().forEach(ii -> {
					String val = ii.getAsString();
					if(!promptList.contains(val))
						promptList.add(val);
					subList.add(val);
				});
				valueList.add(subList);
			});
			builder.put(id, new BoxCaptchaData(CAPTCHA.texIdentifier(texture), difficulty, subdivisions, valueList, promptList));
		});
		ALL_BOXES = builder.build();
	}
	
	public static BoxCaptchaData getRandom(float difficulty)
	{
		if(ALL_BOXES == null || ALL_BOXES.values().isEmpty())
			return null;
		List<BoxCaptchaData> candidates = new ArrayList<>();
		for(BoxCaptchaData box : ALL_BOXES.values())
			if(Math.abs(box.difficulty() - difficulty) <= 5f)
				candidates.add(box);
		return candidates.get(random.nextInt(candidates.size()));
	}
}
