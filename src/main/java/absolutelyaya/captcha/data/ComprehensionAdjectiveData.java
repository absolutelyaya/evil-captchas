package absolutelyaya.captcha.data;

import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.JsonHelper;

public record ComprehensionAdjectiveData(String name, float difficulty, int tint, float scale, boolean shaking, int glowColor)
{
	public boolean isColor()
	{
		return tint != -1;
	}
	
	public NbtCompound serialize()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putString("name", name);
		nbt.putFloat("difficulty", difficulty);
		if(tint != -1)
			nbt.putInt("tint", tint);
		if(scale != 1f)
			nbt.putFloat("scale", scale);
		if(shaking)
			nbt.putBoolean("shaking", true);
		if(glowColor != -1)
			nbt.putInt("glow", glowColor);
		return nbt;
	}
	
	public static ComprehensionAdjectiveData deserialize(NbtCompound nbt)
	{
		String name = nbt.getString("name");
		float difficulty = nbt.getFloat("difficulty");
		int color = -1, glow = -1;
		float scale = 1f;
		if(nbt.contains("tint", NbtElement.INT_TYPE))
			color = nbt.getInt("tint");
		if(nbt.contains("scale", NbtElement.FLOAT_TYPE))
			scale = nbt.getFloat("scale");
		boolean shaking = nbt.contains("shaking", NbtElement.BYTE_TYPE);
		if(nbt.contains("glow", NbtElement.INT_TYPE))
			glow = nbt.getInt("glow");
		return new ComprehensionAdjectiveData(name, difficulty, color, scale, shaking, glow);
	}
	
	public static ComprehensionAdjectiveData deserialize(JsonObject json)
	{
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
		return new ComprehensionAdjectiveData(name, difficulty, color, scale, shaking, glowColor);
	}
}
