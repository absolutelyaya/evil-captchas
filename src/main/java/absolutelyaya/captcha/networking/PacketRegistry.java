package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.component.CaptchaComponents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PacketRegistry
{
	public static void register()
	{
		PayloadTypeRegistry.playS2C().register(OpenCaptcha.ID, OpenCaptcha.CODEC);
		PayloadTypeRegistry.playS2C().register(CaptchaDataSyncPayload.ID, CaptchaDataSyncPayload.CODEC);
		
		PayloadTypeRegistry.playC2S().register(RequestCaptchaPayload.ID, RequestCaptchaPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(CaptchaResultPayload.ID, CaptchaResultPayload.CODEC);
		
		ServerPlayNetworking.registerGlobalReceiver(RequestCaptchaPayload.ID, ((payload, context) -> {
			CAPTCHA.openCaptcha(context.player(), payload.reason(), payload.difficulty());
		}));
		ServerPlayNetworking.registerGlobalReceiver(CaptchaResultPayload.ID, ((payload, context) -> {
			CaptchaComponents.PLAYER.get(context.player()).finishCaptcha(payload.result(), payload.type(), payload.difficulty());
		}));
	}
}
