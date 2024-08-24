package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHAClient;
import absolutelyaya.captcha.data.AmongusPoolManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientPacketRegistry
{
	public static void register()
	{
		ClientPlayNetworking.registerGlobalReceiver(OpenRandomCaptchaPayload.ID, (payload, context) -> {
			CAPTCHAClient.openRandomCaptcha(payload.reason());
		});
		ClientPlayNetworking.registerGlobalReceiver(OpenSpecificCaptchaPayload.ID, (payload, context) -> {
			CAPTCHAClient.openSpecificCaptcha(payload.type(), payload.reason(), payload.difficulty());
		});
		ClientPlayNetworking.registerGlobalReceiver(CaptchaDataSyncPayload.ID, (payload, context) -> {
			for (String key : payload.data().getKeys())
			{
				switch(key)
				{
					case "amongus" -> AmongusPoolManager.applySyncData(payload.data());
				}
			}
		});
	}
}
