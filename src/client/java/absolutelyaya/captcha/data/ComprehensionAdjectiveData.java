package absolutelyaya.captcha.data;

public record ComprehensionAdjectiveData(String name, float difficulty, int tint, float scale, boolean shaking, int glowColor)
{
	public boolean isColor()
	{
		return tint != -1;
	}
}
