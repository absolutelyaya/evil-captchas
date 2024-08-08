package absolutelyaya.captcha;

import absolutelyaya.captcha.data.ImageSearchCaptchaPoolManager;
import absolutelyaya.captcha.data.MultiBoxCaptchaPoolManager;
import absolutelyaya.captcha.data.PuzzleSlideDataManager;
import absolutelyaya.captcha.data.SingleBoxCaptchaDataManager;
import absolutelyaya.captcha.screen.AbstractCaptchaScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class CAPTCHAClient implements ClientModInitializer
{
	float difficulty = 5f;
	
	@Override
	public void onInitializeClient()
	{
		new SingleBoxCaptchaDataManager();
		new MultiBoxCaptchaPoolManager();
		new ImageSearchCaptchaPoolManager();
		new PuzzleSlideDataManager();
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.world != null && !(client.currentScreen instanceof AbstractCaptchaScreen))
			{
				AbstractCaptchaScreen.openRandomCaptcha(client, difficulty);
				difficulty++;
			}
		});
	}
}