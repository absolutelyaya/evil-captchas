package absolutelyaya.captcha.data;

import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public record MultiBoxCaptchaPool(String prompt, float difficulty, List<String> textures)
{
	public NbtCompound serialize()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putString("prompt", prompt);
		nbt.putFloat("difficulty", difficulty);
		NbtList textureList = new NbtList();
		for (String i : textures)
			textureList.add(NbtString.of(i));
		nbt.put("values", textureList);
		return nbt;
	}
	
	public static MultiBoxCaptchaPool deserialize(NbtCompound nbt)
	{
		String prompt = nbt.getString("prompt");
		float difficulty = nbt.getFloat("difficulty");
		List<String> textures = new ArrayList<>();
		nbt.getList("values", NbtElement.STRING_TYPE)
				.forEach(element -> {
					if(element instanceof NbtString string && !textures.contains(string.asString()))
						textures.add(string.asString());
				});
		return new MultiBoxCaptchaPool(prompt, difficulty, textures);
	}
	
	public static MultiBoxCaptchaPool deserialize(JsonObject json)
	{
		String prompt = JsonHelper.getString(json, "promptKey");
		float difficulty = JsonHelper.getFloat(json, "difficulty");
		List<String> textureList = new ArrayList<>();
		JsonHelper.getArray(json, "values").forEach(i -> {
			String val = i.getAsString();
			if(!textureList.contains(val))
				textureList.add(val);
		});
		return new MultiBoxCaptchaPool(prompt, difficulty, textureList);
		
	}
}
