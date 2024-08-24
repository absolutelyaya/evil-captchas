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

public record AmongusPool(float difficulty, List<Identifier> crewmates, List<Identifier> impostors)
{
	public NbtCompound serialize()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putFloat("difficulty", difficulty);
		NbtList list = new NbtList();
		for (Identifier id : crewmates)
			list.add(NbtString.of(id.toString()));
		nbt.put("crewmates", list);
		list = new NbtList();
		for (Identifier id : impostors)
			list.add(NbtString.of(id.toString()));
		nbt.put("impostors", list);
		return nbt;
	}
	
	public static AmongusPool deserialize(NbtCompound nbt)
	{
		float difficulty = nbt.getFloat("difficulty");
		List<Identifier> crewmates = new ArrayList<>(), impostors = new ArrayList<>();
		nbt.getList("crewmates", NbtElement.STRING_TYPE)
				.forEach(element -> {
					if(element instanceof NbtString string)
						crewmates.add(Identifier.tryParse(string.toString()));
				});
		nbt.getList("impostors", NbtElement.STRING_TYPE)
				.forEach(element -> {
					if(element instanceof NbtString string)
						impostors.add(Identifier.tryParse(string.toString()));
				});
		return new AmongusPool(difficulty, crewmates, impostors);
	}
	
	public static AmongusPool deserialize(JsonObject json)
	{
		float difficulty = JsonHelper.getFloat(json, "difficulty");
		List<Identifier> crewmates = new ArrayList<>();
		JsonHelper.getArray(json, "crewmates").forEach(i -> crewmates.add(CAPTCHA.texIdentifier(i.getAsString())));
		List<Identifier> impostors = new ArrayList<>();
		JsonHelper.getArray(json, "impostors").forEach(i -> impostors.add(CAPTCHA.texIdentifier(i.getAsString())));
		return new AmongusPool(difficulty, crewmates, impostors);
	}
}
