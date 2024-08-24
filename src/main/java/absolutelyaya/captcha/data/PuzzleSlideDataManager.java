package absolutelyaya.captcha.data;

import absolutelyaya.captcha.CAPTCHA;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
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

public class PuzzleSlideDataManager extends JsonDataLoader
{
	private static final Random random = Random.create();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static List<Identifier> TEXTURES = new ArrayList<>();
	
	public PuzzleSlideDataManager()
	{
		super(GSON, "captcha/puzzle");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return CAPTCHA.identifier("captcha/puzzle");
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
		ImmutableList.Builder<Identifier> builder = new ImmutableList.Builder<>();
		prepared.forEach((id, element) -> {
			JsonObject json = element.getAsJsonObject();
			JsonHelper.getArray(json, "textures").forEach(i -> builder.add(CAPTCHA.texIdentifier(i.getAsString())));
		});
		TEXTURES = builder.build();
	}
	
	public static Identifier getRandomTexture()
	{
		return TEXTURES.get(random.nextInt(TEXTURES.size()));
	}
	
	public static NbtCompound compileToSyncData()
	{
		NbtCompound nbt = new NbtCompound();
		NbtList list = new NbtList();
		for (Identifier id : TEXTURES)
			list.add(NbtString.of(id.toString()));
		nbt.put("textures", list);
		return nbt;
	}
	
	public static void applySyncData(NbtCompound nbt)
	{
		ImmutableList.Builder<Identifier> builder = new ImmutableList.Builder<>();
		nbt.getList("textures", NbtElement.STRING_TYPE).forEach(i -> {
			if(i instanceof NbtString string)
			{
				Identifier id = Identifier.tryParse(string.asString());
				if(id != null)
					builder.add(id);
			}
		});
		TEXTURES = builder.build();
		CAPTCHA.LOGGER.info("received {} puzzle slide texture paths", TEXTURES.size());
	}
}
