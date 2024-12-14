package absolutelyaya.captcha.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public record InvoluntaryAddon(String type, long activeUntil, float x, float y)
{
	public NbtCompound serialize()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putString("type", type);
		nbt.putLong("activeUntil", activeUntil);
		nbt.putFloat("x", x);
		nbt.putFloat("y", y);
		return nbt;
	}
	
	public static InvoluntaryAddon deserialize(NbtCompound nbt)
	{
		if(!(nbt.contains("type", NbtElement.STRING_TYPE) && nbt.contains("activeUntil", NbtElement.LONG_TYPE) &&
					 nbt.contains("x", NbtElement.FLOAT_TYPE) && nbt.contains("y", NbtElement.FLOAT_TYPE)))
			return null;
		String type = nbt.getString("type");
		long activeUntil = nbt.getLong("activeUntil");
		float x = nbt.getFloat("x"), y = nbt.getFloat("y");
		return new InvoluntaryAddon(type, activeUntil, x, y);
	}
	
	public boolean isAllowsMultiple()
	{
		return type.equals("spinning-pig");
	}
}
