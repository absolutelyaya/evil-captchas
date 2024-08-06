package absolutelyaya.captcha.screen;

import net.minecraft.text.Text;

public class WimmelbildCaptchaScreen extends ImageSearchCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.wimmelbild.";
	
	protected WimmelbildCaptchaScreen(float difficulty)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty);
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
