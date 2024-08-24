package absolutelyaya.captcha.data;

import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ImageSearchCaptchaPool(float difficulty, List<String> backgrounds, List<String> overlays, Map<String, String> objects)
{
	public NbtCompound serialize()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putFloat("difficulty", difficulty);
		NbtList backgroundList = new NbtList(), overlayList = new NbtList();
		for (String i : backgrounds)
			backgroundList.add(NbtString.of(i));
		nbt.put("backgrounds", backgroundList);
		for (String i : overlays)
			overlayList.add(NbtString.of(i));
		nbt.put("overlays", overlayList);
		NbtCompound objectMap = new NbtCompound();
		for (Map.Entry<String, String> object : objects.entrySet())
			objectMap.putString(object.getKey(), object.getValue());
		nbt.put("objects", objectMap);
		return nbt;
	}
	
	public static ImageSearchCaptchaPool deserialize(NbtCompound nbt)
	{
		float difficulty = nbt.getFloat("difficulty");
		List<String> backgrounds = new ArrayList<>(), overlays = new ArrayList<>();
		nbt.getList("backgrounds", NbtElement.STRING_TYPE)
				.forEach(element -> {
					if(element instanceof NbtString string)
						backgrounds.add(string.asString());
				});
		nbt.getList("overlays", NbtElement.STRING_TYPE)
				.forEach(element -> {
					if(element instanceof NbtString string)
						overlays.add(string.asString());
				});
		Map<String, String> objects = new HashMap<>();
		NbtCompound objectMap = nbt.getCompound("objects");
		objectMap.getKeys().forEach(key -> objects.put(key, objectMap.getString(key)));
		return new ImageSearchCaptchaPool(difficulty, backgrounds, overlays, objects);
	}
	
	public static ImageSearchCaptchaPool deserialize(JsonObject json)
	{
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
		return new ImageSearchCaptchaPool(difficulty, backgrounds, overlays, objects);
	}
}
