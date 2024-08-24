package absolutelyaya.captcha.data;

import absolutelyaya.captcha.CAPTCHA;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public record SingleBoxCaptchaData(Identifier texture, float difficulty, int subdivisions, List<List<String>> values, List<String> prompts)
{
	public NbtCompound serialize()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putString("texture", texture.toString());
		nbt.putFloat("difficulty", difficulty);
		nbt.putInt("subdivisions", subdivisions);
		NbtList valueListList = new NbtList();
		for (List<String> i : values)
		{
			NbtList valueList = new NbtList();
			for (String ii : i)
				valueList.add(NbtString.of(ii));
			valueListList.add(valueList);
		}
		nbt.put("values", valueListList);
		NbtList promptList = new NbtList();
		for (String i : prompts)
			promptList.add(NbtString.of(i));
		nbt.put("prompts", promptList);
		return nbt;
	}
	
	public static SingleBoxCaptchaData deserialize(NbtCompound nbt)
	{
		Identifier texture = Identifier.tryParse(nbt.getString("texture"));
		float difficulty = nbt.getFloat("difficulty");
		int subdivisions = nbt.getInt("subdivisions");
		List<List<String>> values = new ArrayList<>();
		for (NbtElement i : nbt.getList("values", NbtElement.LIST_TYPE))
		{
			if(!(i instanceof NbtList list))
				continue;
			List<String> subList = new ArrayList<>();
			for (NbtElement ii : list)
				if(ii instanceof NbtString string)
					subList.add(string.asString());
			values.add(subList);
		}
		List<String> prompts = new ArrayList<>();
		nbt.getList("prompts", NbtElement.STRING_TYPE)
				.forEach(element -> {
					if(element instanceof NbtString string)
						prompts.add(string.asString());
				});
		return new SingleBoxCaptchaData(texture, difficulty, subdivisions, values, prompts);
	}
	
	public static SingleBoxCaptchaData deserialize(JsonObject json)
	{
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
		return new SingleBoxCaptchaData(CAPTCHA.texIdentifier(texture), difficulty, subdivisions, valueList, promptList);
	}
}
