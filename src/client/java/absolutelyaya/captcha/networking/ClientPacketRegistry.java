package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHAClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientPacketRegistry
{
	public static void register()
	{
		ClientPlayNetworking.registerGlobalReceiver(OpenCaptchaPayload.ID, (payload, context) -> {
			CAPTCHAClient.openRandomCaptcha(payload.reason());
		});
	}
}
