package absolutelyaya.captcha.screen;

public class WimmelbildCaptchaScreen extends ImageSearchCaptchaScreen
{
	static final String TRANSLATION_KEY = "screen.captcha.wimmelbild.";
	
	protected WimmelbildCaptchaScreen(float difficulty)
	{
		super(difficulty);
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
