package absolutelyaya.captcha.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PacketRegistry
{
	public static void register()
	{
		PayloadTypeRegistry.playS2C().register(OpenCaptchaPayload.ID, OpenCaptchaPayload.CODEC);
	}
}
