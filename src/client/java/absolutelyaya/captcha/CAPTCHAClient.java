package absolutelyaya.captcha;

import absolutelyaya.captcha.data.BoxCaptchaDataManager;
import absolutelyaya.captcha.screen.AbstractCaptchaScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class CAPTCHAClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		new BoxCaptchaDataManager();
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.world != null && !(client.currentScreen instanceof AbstractCaptchaScreen))
				AbstractCaptchaScreen.openRandomCaptcha(client, 5f);
		});
	}
}