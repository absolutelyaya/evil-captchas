package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHAClient;
import absolutelyaya.captcha.data.*;
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
					case "amongus" -> AmongusPoolManager.applySyncData(payload.data().getCompound(key));
					case "comprehension" -> ComprehensionTestManager.applySyncData(payload.data().getCompound(key));
					case "image-search" -> ImageSearchCaptchaPoolManager.applySyncData(payload.data().getCompound(key));
					case "multi-box" -> MultiBoxCaptchaPoolManager.applySyncData(payload.data().getCompound(key));
					case "single-box" -> SingleBoxCaptchaDataManager.applySyncData(payload.data().getCompound(key));
					case "puzzle" -> PuzzleSlideDataManager.applySyncData(payload.data().getCompound(key));
				}
			}
		});
	}
}
