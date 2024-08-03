package absolutelyaya.captcha.screen;

import absolutelyaya.captcha.data.BoxCaptchaData;
import absolutelyaya.captcha.data.BoxCaptchaDataManager;
import net.minecraft.text.Text;

public class SingleBoxCaptchaScreen extends AbstractBoxCaptchaScreen
{
	final static String TRANSLATION_KEY = "screen.captcha.boxes.single.";
	final BoxCaptchaData data;
	
	protected SingleBoxCaptchaScreen(float difficulty)
	{
		super(Text.translatable(TRANSLATION_KEY + "title"), difficulty);
		data = BoxCaptchaDataManager.getRandom(difficulty);
	}
	
	@Override
	protected void init()
	{
		super.init();
	}
	
	@Override
	void addBoxButton(int x, int y)
	{
		//boxButtons.add();
	}
	
	@Override
	String getTranslationKey()
	{
		return TRANSLATION_KEY;
	}
}
