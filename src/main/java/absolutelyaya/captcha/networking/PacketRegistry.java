package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.component.CaptchaComponents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PacketRegistry
{
	public static void register()
	{
		PayloadTypeRegistry.playS2C().register(OpenRandomCaptchaPayload.ID, OpenRandomCaptchaPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenSpecificCaptchaPayload.ID, OpenSpecificCaptchaPayload.CODEC);
		
		PayloadTypeRegistry.playC2S().register(CaptchaResultPayload.ID, CaptchaResultPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(OpenCaptchaC2SPayload.ID, OpenCaptchaC2SPayload.CODEC);
		
		ServerPlayNetworking.registerGlobalReceiver(CaptchaResultPayload.ID, ((payload, context) -> {
			CaptchaComponents.PLAYER.get(context.player()).finishCaptcha(payload.result());
		}));
		ServerPlayNetworking.registerGlobalReceiver(OpenCaptchaC2SPayload.ID, ((payload, context) -> {
			CaptchaComponents.PLAYER.get(context.player()).startCaptcha();
		}));
	}
}
