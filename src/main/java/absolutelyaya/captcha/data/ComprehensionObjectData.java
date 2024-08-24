package absolutelyaya.captcha.data;

import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.JsonHelper;

public record ComprehensionObjectData(String texture, String name, float difficulty)
{
	public NbtCompound serialize()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putString("texture", texture);
		nbt.putString("name", name);
		nbt.putFloat("difficulty", difficulty);
		return nbt;
	}
	
	public static ComprehensionObjectData deserialize(NbtCompound nbt)
	{
		String texture = nbt.getString("texture");
		String name = nbt.getString("name");
		float difficulty = nbt.getFloat("difficulty");
		return new ComprehensionObjectData(texture, name, difficulty);
	}
	
	public static ComprehensionObjectData deserialize(JsonObject json)
	{
		String texture = JsonHelper.getString(json, "texture");
		String name = JsonHelper.getString(json, "name");
		float difficulty = JsonHelper.getFloat(json, "difficulty");
		return new ComprehensionObjectData(texture, name, difficulty);
	}
}
