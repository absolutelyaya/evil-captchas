package absolutelyaya.captcha.screen;

import net.minecraft.text.Text;

public class WimmelbildCaptchaScreen extends ImageSearchCaptchaScreen
{
	public static final String TYPE = "wimmelbild";
	static final String TRANSLATION_KEY = "screen.captcha.wimmelbild.";
	
	protected WimmelbildCaptchaScreen(float difficulty, String reason)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty, reason);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
	
	@Override
	protected int getImageCount()
	{
		return 32 + (int)difficulty;
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
}
