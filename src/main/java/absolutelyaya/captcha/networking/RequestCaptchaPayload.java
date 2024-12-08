package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record RequestCaptchaPayload(String reason, float difficulty) implements CustomPayload
{
	public static final Id<RequestCaptchaPayload> ID = new Id<>(CAPTCHA.identifier("request"));
	public static final PacketCodec<RegistryByteBuf, RequestCaptchaPayload> CODEC =
			PacketCodec.tuple(PacketCodecs.STRING, RequestCaptchaPayload::reason,
					PacketCodecs.FLOAT, RequestCaptchaPayload::difficulty, RequestCaptchaPayload::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
